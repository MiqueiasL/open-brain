package com.nextwave.openbrain.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.nextwave.openbrain.accessibility.OpenBrainAccessibilityService;
import com.nextwave.openbrain.brain.BrainDispatcher;
import com.nextwave.openbrain.control.DeviceControlManager;
import com.nextwave.openbrain.control.ShellExecutor;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AiOrchestrator - Orquestrador Central de Inteligência do OpenBrain
 * Recebe o comando do usuário, consulta a LLM ativa (Gemini, Groq, Claude, OpenAI, etc.),
 * intercepta Function Calls da IA e aciona os Braços Motores no Android.
 */
public class AiOrchestrator {
    private static final String TAG = "AiOrchestrator";

    private final Context context;
    private final LlmConfig config;
    private final DeviceControlManager deviceControl;
    private final BrainDispatcher fallbackDispatcher;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String SYSTEM_PROMPT =
            "Você é o Jarvis (OpenBrain), um assistente de inteligência artificial de elite e soberano rodando em um smartphone Android.\n" +
            "Seu criador e mestre é o Miquéias (Fundador da Next Wave).\n" +
            "Diretrizes:\n" +
            "1. Fale sempre em português brasileiro de forma elegante, confiante, direta e respeitosa (trate o usuário por 'senhor').\n" +
            "2. Você possui ferramentas (Tools) reais conectadas ao celular: lanterna, bateria, volume, alarmes, timers, WhatsApp, Waze/GPS, leitura de tela, cliques na tela e terminal shell.\n" +
            "3. Quando o usuário pedir qualquer ação que você tem ferramenta para executar, NUNCA dê instruções de como fazer manualmente; CHAME A FERRAMENTA correspondente imediatamente!\n" +
            "4. Se for uma conversa comum, dúvida, raciocínio, piada ou pergunta geral, responda de forma brilhante e concisa.";

    public interface Callback {
        void onResult(BrainDispatcher.ActionResult result);
    }

    public AiOrchestrator(Context context, DeviceControlManager deviceControl) {
        this.context = context;
        this.config = new LlmConfig(context);
        this.deviceControl = deviceControl;
        this.fallbackDispatcher = new BrainDispatcher(context, deviceControl);
    }

    public LlmConfig getConfig() {
        return config;
    }

    public void processPrompt(String prompt, Callback callback) {
        executor.execute(() -> {
            BrainDispatcher.ActionResult computedResult;
            String apiKey = config.getApiKey();

            // Se for Gemini e tiver API Key configurada
            if (config.getProvider() == LlmConfig.Provider.GEMINI && apiKey != null && !apiKey.isEmpty()) {
                try {
                    Log.i(TAG, "Enviando para o Gemini: " + prompt);
                    GeminiClient.Response geminiResp = GeminiClient.generate(
                            apiKey,
                            config.getModel(),
                            SYSTEM_PROMPT,
                            prompt
                    );

                    if (geminiResp.isFunctionCall) {
                        computedResult = executeToolCall(geminiResp.functionName, geminiResp.functionArgs);
                    } else {
                        computedResult = new BrainDispatcher.ActionResult(
                                geminiResp.text,
                                "Resposta processada pelo modelo neural " + config.getModel() + ".",
                                true
                        );
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Falha na chamada Gemini (" + e.getMessage() + "), usando fallback local.");
                    BrainDispatcher.ActionResult localRes = fallbackDispatcher.dispatch(prompt);
                    computedResult = new BrainDispatcher.ActionResult(
                            localRes.spokenText,
                            "⚠️ Erro de API (" + e.getMessage() + "). Resposta via motor local:\n" + localRes.detailedLog,
                            localRes.success
                    );
                }
            } else {
                // Fallback para o motor local se não tiver chave configurada
                Log.i(TAG, "Sem chave de API configurada para " + config.getProvider().displayName + ". Usando motor local.");
                BrainDispatcher.ActionResult localRes = fallbackDispatcher.dispatch(prompt);
                String aviso = apiKey.isEmpty()
                        ? "\n\n💡 Dica: Toque em [⚙️ IA] no topo para inserir sua chave do " + config.getProvider().displayName + " e ativar o raciocínio completo!"
                        : "";
                computedResult = new BrainDispatcher.ActionResult(
                        localRes.spokenText,
                        localRes.detailedLog + aviso,
                        localRes.success
                );
            }

            final BrainDispatcher.ActionResult finalResult = computedResult;
            mainHandler.post(() -> callback.onResult(finalResult));
        });
    }

    private BrainDispatcher.ActionResult executeToolCall(String functionName, JSONObject args) {
        Log.i(TAG, "Executando Braço Motor para Tool: " + functionName);

        switch (functionName) {
            case "set_flashlight": {
                boolean enable = args.optBoolean("enable", true);
                boolean ok = deviceControl.setFlashlight(enable);
                String msg = enable ? "Lanterna ligada, senhor." : "Lanterna desligada, senhor.";
                return new BrainDispatcher.ActionResult(msg, "Tool 'set_flashlight(" + enable + ")' executada com sucesso.", ok);
            }

            case "get_battery": {
                int level = deviceControl.getBatteryPercentage();
                return new BrainDispatcher.ActionResult(
                        "Sua bateria está em " + level + " por cento, senhor.",
                        "Tool 'get_battery()' retornou " + level + "%.",
                        true
                );
            }

            case "set_volume": {
                int percent = args.optInt("percentage", 50);
                deviceControl.setMediaVolume(percent);
                return new BrainDispatcher.ActionResult(
                        "Volume de mídia ajustado para " + percent + " por cento, senhor.",
                        "Tool 'set_volume(" + percent + ")' executada.",
                        true
                );
            }

            case "set_alarm": {
                int hour = args.optInt("hour", 8);
                int minute = args.optInt("minute", 0);
                String label = args.optString("label", "OpenBrain Alarme");
                deviceControl.setAlarm(hour, minute, label);
                return new BrainDispatcher.ActionResult(
                        "Alarme definido para as " + hour + " horas e " + minute + " minutos, senhor.",
                        "Tool 'set_alarm(" + hour + ":" + minute + ")' agendada no AlarmClock.",
                        true
                );
            }

            case "set_timer": {
                int seconds = args.optInt("seconds", 60);
                deviceControl.setTimer(seconds, "Timer OpenBrain");
                String tempo = (seconds >= 60) ? (seconds / 60) + " minutos" : seconds + " segundos";
                return new BrainDispatcher.ActionResult(
                        "Timer de " + tempo + " iniciado, senhor.",
                        "Tool 'set_timer(" + seconds + "s)' iniciada.",
                        true
                );
            }

            case "send_whatsapp": {
                String phone = args.optString("phone", "");
                String message = args.optString("message", "");
                deviceControl.openWhatsAppMessage(phone, message);
                return new BrainDispatcher.ActionResult(
                        "Abrindo o WhatsApp com sua mensagem pronta, senhor.",
                        "Tool 'send_whatsapp' disparada via Intent.",
                        true
                );
            }

            case "navigate_gps": {
                String dest = args.optString("destination", "Destino");
                deviceControl.navigateTo(dest);
                return new BrainDispatcher.ActionResult(
                        "Traçando rota para " + dest + ", senhor.",
                        "Tool 'navigate_gps(" + dest + ")' aberta no Waze/Maps.",
                        true
                );
            }

            case "read_screen": {
                OpenBrainAccessibilityService a11y = OpenBrainAccessibilityService.getInstance();
                if (a11y != null) {
                    List<String> nodes = a11y.getVisibleScreenText();
                    return new BrainDispatcher.ActionResult(
                            "Li a tela atual, senhor. Foram encontrados " + nodes.size() + " elementos visíveis.",
                            "Elementos da Tela:\n" + String.join("\n", nodes),
                            true
                    );
                }
                return new BrainDispatcher.ActionResult("Serviço de acessibilidade desligado, senhor.", "Acessibilidade não conectada.", false);
            }

            case "click_screen": {
                String target = args.optString("target_text", "");
                OpenBrainAccessibilityService a11y = OpenBrainAccessibilityService.getInstance();
                if (a11y != null) {
                    boolean ok = a11y.clickByText(target);
                    String msg = ok ? "Clique efetuado em " + target + ", senhor." : "Não encontrei o botão " + target + " na tela, senhor.";
                    return new BrainDispatcher.ActionResult(msg, "Tool 'click_screen(" + target + ")' status: " + ok, ok);
                }
                return new BrainDispatcher.ActionResult("Acessibilidade desligada, senhor.", "Serviço inativo.", false);
            }

            case "run_shell": {
                String cmd = args.optString("command", "");
                String out = ShellExecutor.execute(cmd);
                return new BrainDispatcher.ActionResult(
                        "Comando de terminal executado, senhor.",
                        "Comando: " + cmd + "\nSaída:\n" + out,
                        true
                );
            }

            default:
                return new BrainDispatcher.ActionResult(
                        "Ferramenta " + functionName + " não reconhecida no sistema, senhor.",
                        "Tool desconhecida: " + functionName,
                        false
                );
        }
    }
}
