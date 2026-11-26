package com.nextwave.openbrain.brain;

import android.content.Context;
import android.util.Log;
import com.nextwave.openbrain.accessibility.OpenBrainAccessibilityService;
import com.nextwave.openbrain.control.DeviceControlManager;
import com.nextwave.openbrain.control.ShellExecutor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenBrain Brain Dispatcher
 * O Cérebro Orquestrador que recebe ordens em linguagem natural (voz ou texto),
 * extrai a intenção do usuário e dispara os Braços Motores correspondentes
 * (DeviceControl, Acessibilidade, Apps e Shell Nativo).
 */
public class BrainDispatcher {
    private static final String TAG = "BrainDispatcher";
    private final Context context;
    private final DeviceControlManager deviceControl;

    public static class ActionResult {
        public final String spokenText;
        public final String detailedLog;
        public final boolean success;

        public ActionResult(String spokenText, String detailedLog, boolean success) {
            this.spokenText = spokenText;
            this.detailedLog = detailedLog;
            this.success = success;
        }
    }

    public BrainDispatcher(Context context, DeviceControlManager deviceControl) {
        this.context = context;
        this.deviceControl = deviceControl;
    }

    /**
     * Processa qualquer comando do usuário em linguagem natural
     */
    public ActionResult dispatch(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return new ActionResult("Comando vazio, senhor.", "Entrada vazia.", false);
        }

        String lower = prompt.toLowerCase().trim();
        Log.i(TAG, "Processando comando: " + prompt);

        // 1. LANTERNA
        if (lower.contains("lanterna") || lower.contains("luz") || lower.contains("flash")) {
            boolean turnOn = lower.contains("liga") || lower.contains("acende") || lower.contains("ativ");
            boolean turnOff = lower.contains("desliga") || lower.contains("apaga") || lower.contains("desativ");

            if (turnOn) {
                boolean ok = deviceControl.setFlashlight(true);
                return new ActionResult("Lanterna ligada, senhor.", "Lanterna ativada via CameraManager.", ok);
            } else if (turnOff) {
                boolean ok = deviceControl.setFlashlight(false);
                return new ActionResult("Lanterna desligada, senhor.", "Lanterna desativada via CameraManager.", ok);
            }
        }

        // 2. BATERIA
        if (lower.contains("bateria") || lower.contains("carga") || lower.contains("porcentagem")) {
            int level = deviceControl.getBatteryPercentage();
            return new ActionResult(
                    "Sua bateria está em " + level + " por cento, senhor.",
                    "Leitura de bateria: " + level + "% via BatteryManager.",
                    true
            );
        }

        // 3. VOLUME & MÍDIA
        if (lower.contains("volume") || lower.contains("som") || lower.contains("silencioso")) {
            if (lower.contains("mudo") || lower.contains("silencioso") || lower.contains("zero")) {
                deviceControl.setMediaVolume(0);
                return new ActionResult("Volume mutado, senhor.", "Volume de mídia ajustado para 0%.", true);
            } else if (lower.contains("máximo") || lower.contains("100")) {
                deviceControl.setMediaVolume(100);
                return new ActionResult("Volume no máximo, senhor.", "Volume de mídia ajustado para 100%.", true);
            } else {
                Matcher m = Pattern.compile("(\\d+)").matcher(lower);
                if (m.find()) {
                    int percent = Integer.parseInt(m.group(1));
                    deviceControl.setMediaVolume(percent);
                    return new ActionResult("Volume ajustado para " + percent + " por cento, senhor.", "Volume alterado para " + percent + "%.", true);
                }
            }
        }

        if (lower.contains("pausar") || lower.contains("tocar música") || lower.contains("play") || lower.contains("pause")) {
            deviceControl.playPauseMedia();
            return new ActionResult("Comando de mídia enviado, senhor.", "Play/Pause disparado via MediaKey.", true);
        }

        if (lower.contains("próxima música") || lower.contains("pular música") || lower.contains("avançar")) {
            deviceControl.nextTrack();
            return new ActionResult("Avançando para a próxima faixa, senhor.", "Next track disparado via MediaKey.", true);
        }

        // 4. TEMPO (ALARMES E TIMERS)
        if (lower.contains("alarme") || lower.contains("despertador")) {
            Matcher m = Pattern.compile("(\\d{1,2})(?:\\s*e\\s*|:|h)?(\\d{2})?").matcher(lower);
            int hour = 8;
            int min = 0;
            if (m.find()) {
                hour = Integer.parseInt(m.group(1));
                if (m.group(2) != null) {
                    min = Integer.parseInt(m.group(2));
                }
            }
            deviceControl.setAlarm(hour, min, "OpenBrain Jarvis Alarme");
            return new ActionResult(
                    "Alarme definido para as " + hour + " horas e " + min + " minutos, senhor.",
                    "Alarme agendado via AlarmClock: " + hour + ":" + min,
                    true
            );
        }

        if (lower.contains("timer") || lower.contains("cronômetro") || lower.contains("cronometro")) {
            Matcher m = Pattern.compile("(\\d+)\\s*(minuto|segundo)?").matcher(lower);
            int seconds = 60;
            if (m.find()) {
                int val = Integer.parseInt(m.group(1));
                String unit = m.group(2);
                if (unit != null && unit.startsWith("seg")) {
                    seconds = val;
                } else {
                    seconds = val * 60;
                }
            }
            deviceControl.setTimer(seconds, "Timer OpenBrain");
            return new ActionResult(
                    "Timer de " + (seconds >= 60 ? (seconds / 60) + " minutos" : seconds + " segundos") + " iniciado, senhor.",
                    "Timer configurado via AlarmClock para " + seconds + "s.",
                    true
            );
        }

        // 5. NAVEGAÇÃO & ROTAS (WAZE / GOOGLE MAPS)
        if (lower.contains("rota") || lower.contains("waze") || lower.contains("como ir") || lower.contains("navegar para")) {
            String dest = prompt.replaceAll("(?i)(rota para|navegar para|como ir para|no waze|waze|levar até)", "").trim();
            if (dest.isEmpty()) dest = "Casa";
            deviceControl.navigateTo(dest);
            return new ActionResult("Traçando rota para " + dest + ", senhor.", "Navegação disparada para: " + dest, true);
        }

        // 6. WHATSAPP & COMUNICAÇÃO
        if (lower.contains("whatsapp") || lower.contains("zap") || lower.contains("mensagem")) {
            deviceControl.openWhatsAppMessage("", prompt);
            return new ActionResult("Abrindo WhatsApp para enviar sua mensagem, senhor.", "Intent do WhatsApp disparado com o texto.", true);
        }

        // 7. TERMINAL SHELL NATIVO (MÉTODO 3)
        if (lower.startsWith("shell ") || lower.startsWith("comando ") || lower.startsWith("terminal ") || lower.startsWith("sh ")) {
            String cmd = prompt.replaceFirst("(?i)^(shell|comando|terminal|sh)\\s+", "").trim();
            String output = ShellExecutor.execute(cmd);
            return new ActionResult("Comando executado no terminal, senhor.", "Executado: " + cmd + "\nResultado:\n" + output, true);
        }

        // 8. ACESSIBILIDADE: CONTROLE E LEITURA DE TELA
        OpenBrainAccessibilityService a11y = OpenBrainAccessibilityService.getInstance();
        if (lower.contains("ler tela") || lower.contains("o que tem na tela") || lower.contains("leia a tela") || lower.contains("ver tela")) {
            if (a11y != null) {
                List<String> nodes = a11y.getVisibleScreenText();
                if (nodes.isEmpty()) {
                    return new ActionResult("Não encontrei textos legíveis na tela atual, senhor.", "Nenhum nó de texto detectado na UI ativa.", false);
                }
                String summary = String.join(", ", nodes.subList(0, Math.min(6, nodes.size())));
                return new ActionResult(
                        "Identifiquei " + nodes.size() + " elementos na tela. Entre eles: " + summary + ".",
                        "Elementos extraídos (" + nodes.size() + "):\n" + String.join("\n", nodes),
                        true
                );
            } else {
                return new ActionResult(
                        "O serviço de acessibilidade precisa estar ativado para eu enxergar a tela, senhor.",
                        "Acessibilidade não conectada no momento.",
                        false
                );
            }
        }

        if (lower.startsWith("clique em ") || lower.startsWith("clicar em ") || lower.startsWith("toque em ")) {
            String target = prompt.replaceFirst("(?i)^(clique em|clicar em|toque em)\\s+", "").trim();
            if (a11y != null) {
                boolean clicked = a11y.clickByText(target);
                if (clicked) {
                    return new ActionResult("Clique efetuado em " + target + ", senhor.", "Elemento localizado e clicado via Acessibilidade: " + target, true);
                } else {
                    return new ActionResult("Não encontrei nenhum elemento com o nome " + target + " na tela, senhor.", "Falha ao localizar nó com texto: " + target, false);
                }
            } else {
                return new ActionResult("Serviço de acessibilidade inativo, senhor.", "Ative a acessibilidade para injeção de toques.", false);
            }
        }

        if (lower.contains("rolar para baixo") || lower.contains("role para baixo") || lower.contains("rola")) {
            if (a11y != null) {
                boolean scrolled = a11y.scrollForward();
                return new ActionResult("Rolando a tela para baixo, senhor.", "Scroll forward executado via Acessibilidade.", scrolled);
            }
        }

        if (lower.equals("voltar") || lower.contains("botão voltar")) {
            if (a11y != null) {
                a11y.performBack();
                return new ActionResult("Voltando tela, senhor.", "Ação global Voltar disparada.", true);
            }
        }

        // 9. RESPOSTA INTELIGENTE PADRÃO (JARVIS)
        return new ActionResult(
                "Entendido, senhor. Estou pronto para executar suas ordens de hardware, mensagens, rotas, leitura de tela ou terminal.",
                "Ordem registrada: \"" + prompt + "\". Aguardando comandos específicos ou ferramentas conectadas.",
                true
        );
    }
}
