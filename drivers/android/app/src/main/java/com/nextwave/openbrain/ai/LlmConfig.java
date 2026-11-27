package com.nextwave.openbrain.ai;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * LlmConfig - Gerenciador de Provedores de IA do OpenBrain
 * Permite alternar entre Gemini, Groq, Claude, OpenAI, OpenRouter e Ollama.
 */
public class LlmConfig {
    private static final String PREF_NAME = "openbrain_llm_prefs";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_CUSTOM_ENDPOINT = "custom_endpoint";

    public enum Provider {
        GEMINI("Google Gemini", "gemini-1.5-flash"),
        GROQ("Groq (Llama 3.3)", "llama-3.3-70b-versatile"),
        OPENROUTER("OpenRouter", "google/gemini-2.0-flash-exp:free"),
        OPENAI("OpenAI (GPT-4o)", "gpt-4o-mini"),
        CLAUDE("Claude (Anthropic)", "claude-3-5-sonnet-20241022"),
        OLLAMA("Ollama (Local)", "llama3.2");

        public final String displayName;
        public final String defaultModel;

        Provider(String displayName, String defaultModel) {
            this.displayName = displayName;
            this.defaultModel = defaultModel;
        }
    }

    private final SharedPreferences prefs;

    public LlmConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Provider getProvider() {
        String name = prefs.getString(KEY_PROVIDER, Provider.GEMINI.name());
        try {
            return Provider.valueOf(name);
        } catch (Exception e) {
            return Provider.GEMINI;
        }
    }

    public void setProvider(Provider provider) {
        prefs.edit().putString(KEY_PROVIDER, provider.name()).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }

    public void setApiKey(String apiKey) {
        prefs.edit().putString(KEY_API_KEY, apiKey != null ? apiKey.trim() : "").apply();
    }

    public String getModel() {
        return prefs.getString(KEY_MODEL, getProvider().defaultModel);
    }

    public void setModel(String model) {
        prefs.edit().putString(KEY_MODEL, model != null ? model.trim() : "").apply();
    }

    public String getCustomEndpoint() {
        return prefs.getString(KEY_CUSTOM_ENDPOINT, "http://10.0.2.2:11434");
    }

    public void setCustomEndpoint(String endpoint) {
        prefs.edit().putString(KEY_CUSTOM_ENDPOINT, endpoint != null ? endpoint.trim() : "").apply();
    }
}
