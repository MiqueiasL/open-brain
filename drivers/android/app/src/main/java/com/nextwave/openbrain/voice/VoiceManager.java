package com.nextwave.openbrain.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

/**
 * OpenBrain VoiceManager
 * Controla os sentidos de audição (STT) e fala (TTS com voz estilo Jarvis)
 * sem precisar de serviços externos ou Modo Desenvolvedor.
 */
public class VoiceManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceManager";
    private final Context context;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private boolean ttsReady = false;
    private VoiceCallback callback;

    public interface VoiceCallback {
        void onSpeechRecognized(String text);
        void onListeningStateChanged(boolean isListening);
        void onError(String error);
    }

    public VoiceManager(Context context, VoiceCallback callback) {
        this.context = context;
        this.callback = callback;
        this.tts = new TextToSpeech(context, this);
        initSpeechRecognizer();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale ptBr = new Locale("pt", "BR");
            int result = tts.setLanguage(ptBr);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Português BR não suportado no TTS padrão, usando padrão do sistema");
                tts.setLanguage(Locale.getDefault());
            }
            // Calibração sutil de pitch e velocidade para tom sóbrio e elegante (estilo Jarvis)
            tts.setPitch(0.92f);
            tts.setSpeechRate(1.02f);
            ttsReady = true;
            Log.i(TAG, "TTS inicializado com sucesso em PT-BR!");
        } else {
            Log.e(TAG, "Falha ao inicializar TextToSpeech: " + status);
        }
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    if (callback != null) callback.onListeningStateChanged(true);
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    if (callback != null) callback.onListeningStateChanged(false);
                }

                @Override
                public void onError(int error) {
                    if (callback != null) {
                        callback.onListeningStateChanged(false);
                        callback.onError("Erro no reconhecimento de voz (código " + error + ")");
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    if (callback != null) {
                        callback.onListeningStateChanged(false);
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            callback.onSpeechRecognized(matches.get(0));
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        } else {
            Log.w(TAG, "SpeechRecognizer não disponível no dispositivo.");
        }
    }

    public void startListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizer.startListening(intent);
        } else if (callback != null) {
            callback.onError("Reconhecedor de voz não disponível no sistema.");
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void speak(String text) {
        if (ttsReady && tts != null && text != null && !text.trim().isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "OPENBRAIN_TTS_" + System.currentTimeMillis());
        }
    }

    public void destroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
