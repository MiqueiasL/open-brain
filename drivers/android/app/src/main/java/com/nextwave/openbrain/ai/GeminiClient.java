package com.nextwave.openbrain.ai;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * GeminiClient - Conexão Direta com a API do Google Gemini
 * Suporta Raciocínio, Conversação Natural e Function Calling (Tool Calling)
 * para acionar os braços motores do celular.
 */
public class GeminiClient {
    private static final String TAG = "GeminiClient";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    public static class Response {
        public final String text;
        public final String functionName;
        public final JSONObject functionArgs;
        public final boolean isFunctionCall;

        public Response(String text, String functionName, JSONObject functionArgs, boolean isFunctionCall) {
            this.text = text;
            this.functionName = functionName;
            this.functionArgs = functionArgs;
            this.isFunctionCall = isFunctionCall;
        }
    }

    public static Response generate(String apiKey, String model, String systemPrompt, String userMessage) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Chave de API do Gemini não configurada!");
        }

        String endpoint = BASE_URL + (model != null && !model.isEmpty() ? model : "gemini-1.5-flash") + ":generateContent?key=" + apiKey;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        JSONObject requestJson = new JSONObject();

        // System Instruction
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject sysObj = new JSONObject();
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", systemPrompt));
            sysObj.put("parts", parts);
            requestJson.put("systemInstruction", sysObj);
        }

        // Contents
        JSONArray contents = new JSONArray();
        JSONObject userTurn = new JSONObject();
        userTurn.put("role", "user");
        JSONArray userParts = new JSONArray();
        userParts.put(new JSONObject().put("text", userMessage));
        userTurn.put("parts", userParts);
        contents.put(userTurn);
        requestJson.put("contents", contents);

        // Tool Definitions (Function Calling)
        JSONArray functionDeclarations = new JSONArray();

        // 1. Lanterna
        JSONObject funcFlashlight = new JSONObject()
                .put("name", "set_flashlight")
                .put("description", "Liga ou desliga a lanterna do smartphone.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("enable", new JSONObject().put("type", "BOOLEAN").put("description", "true para ligar, false para desligar")))
                        .put("required", new JSONArray().put("enable")));
        functionDeclarations.put(funcFlashlight);

        // 2. Bateria
        JSONObject funcBattery = new JSONObject()
                .put("name", "get_battery")
                .put("description", "Retorna a porcentagem e nível atual da bateria do celular.");
        functionDeclarations.put(funcBattery);

        // 3. Volume
        JSONObject funcVolume = new JSONObject()
                .put("name", "set_volume")
                .put("description", "Ajusta o volume de mídia do smartphone de 0 a 100%.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("percentage", new JSONObject().put("type", "INTEGER").put("description", "Nível de volume entre 0 e 100")))
                        .put("required", new JSONArray().put("percentage")));
        functionDeclarations.put(funcVolume);

        // 4. Alarme
        JSONObject funcAlarm = new JSONObject()
                .put("name", "set_alarm")
                .put("description", "Cria um novo alarme no despertador do Android.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("hour", new JSONObject().put("type", "INTEGER").put("description", "Hora do alarme (0-23)"))
                                .put("minute", new JSONObject().put("type", "INTEGER").put("description", "Minutos do alarme (0-59)"))
                                .put("label", new JSONObject().put("type", "STRING").put("description", "Nome ou descrição do alarme")))
                        .put("required", new JSONArray().put("hour").put("minute")));
        functionDeclarations.put(funcAlarm);

        // 5. Timer
        JSONObject funcTimer = new JSONObject()
                .put("name", "set_timer")
                .put("description", "Inicia um timer ou cronômetro em segundos.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("seconds", new JSONObject().put("type", "INTEGER").put("description", "Duração em segundos")))
                        .put("required", new JSONArray().put("seconds")));
        functionDeclarations.put(funcTimer);

        // 6. WhatsApp
        JSONObject funcWhatsapp = new JSONObject()
                .put("name", "send_whatsapp")
                .put("description", "Abre o WhatsApp com uma mensagem e número pré-preenchidos.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("phone", new JSONObject().put("type", "STRING").put("description", "Número com código do país (ex: 5551999999999) ou vazio"))
                                .put("message", new JSONObject().put("type", "STRING").put("description", "Texto da mensagem")))
                        .put("required", new JSONArray().put("message")));
        functionDeclarations.put(funcWhatsapp);

        // 7. Navegação GPS / Waze
        JSONObject funcNavigate = new JSONObject()
                .put("name", "navigate_gps")
                .put("description", "Traça uma rota de navegação para um destino usando Waze ou Google Maps.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("destination", new JSONObject().put("type", "STRING").put("description", "Endereço ou nome do local de destino")))
                        .put("required", new JSONArray().put("destination")));
        functionDeclarations.put(funcNavigate);

        // 8. Ler Tela (Acessibilidade)
        JSONObject funcReadScreen = new JSONObject()
                .put("name", "read_screen")
                .put("description", "Lê e extrai todos os textos, botões e elementos visíveis da tela atual do celular.");
        functionDeclarations.put(funcReadScreen);

        // 9. Clicar na Tela (Acessibilidade)
        JSONObject funcClickScreen = new JSONObject()
                .put("name", "click_screen")
                .put("description", "Localiza e clica em um botão na tela do celular pelo texto visível.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("target_text", new JSONObject().put("type", "STRING").put("description", "Texto exato do botão a ser clicado")))
                        .put("required", new JSONArray().put("target_text")));
        functionDeclarations.put(funcClickScreen);

        // 10. Shell Local (Terminal)
        JSONObject funcShell = new JSONObject()
                .put("name", "run_shell")
                .put("description", "Executa um comando no terminal Linux local do Android.")
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", new JSONObject()
                                .put("command", new JSONObject().put("type", "STRING").put("description", "Comando shell a ser executado")))
                        .put("required", new JSONArray().put("command")));
        functionDeclarations.put(funcShell);

        JSONArray toolsArray = new JSONArray();
        toolsArray.put(new JSONObject().put("functionDeclarations", functionDeclarations));
        requestJson.put("tools", toolsArray);

        // Enviar requisição
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseStr.append(line);
            }
        }

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Erro Gemini API [" + code + "]: " + responseStr);
        }

        JSONObject respJson = new JSONObject(responseStr.toString());
        JSONArray candidates = respJson.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return new Response("Não recebi resposta do Gemini, senhor.", null, null, false);
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        JSONObject contentObj = firstCandidate.optJSONObject("content");
        if (contentObj == null) {
            return new Response("Resposta vazia da IA, senhor.", null, null, false);
        }

        JSONArray resParts = contentObj.optJSONArray("parts");
        if (resParts == null || resParts.length() == 0) {
            return new Response("Sem partes na resposta, senhor.", null, null, false);
        }

        JSONObject firstPart = resParts.getJSONObject(0);
        if (firstPart.has("functionCall")) {
            JSONObject call = firstPart.getJSONObject("functionCall");
            String name = call.getString("name");
            JSONObject args = call.optJSONObject("args");
            if (args == null) args = new JSONObject();
            Log.i(TAG, "Gemini acionou Tool: " + name + " com args: " + args);
            return new Response(null, name, args, true);
        }

        String text = firstPart.optString("text", "");
        return new Response(text.trim(), null, null, false);
    }
}
