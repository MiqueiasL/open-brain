package com.nextwave.openbrain;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.nextwave.openbrain.accessibility.OpenBrainAccessibilityService;
import com.nextwave.openbrain.ai.AiOrchestrator;
import com.nextwave.openbrain.ai.LlmConfig;
import com.nextwave.openbrain.brain.BrainDispatcher;
import com.nextwave.openbrain.control.DeviceControlManager;
import com.nextwave.openbrain.hud.FloatingHudService;
import com.nextwave.openbrain.voice.VoiceManager;

public class MainActivity extends AppCompatActivity implements VoiceManager.VoiceCallback {
    private static final int PERM_REQ_AUDIO = 201;

    private Button btnStatusA11y;
    private Button btnSettingsIa;
    private Button btnToggleHud;
    private ScrollView scrollHistory;
    private LinearLayout layoutChatHistory;
    private TextView tvVoiceStatus;
    private EditText etCommandInput;
    private Button btnVoiceRecord;
    private Button btnSendCommand;

    private DeviceControlManager controlManager;
    private AiOrchestrator aiOrchestrator;
    private VoiceManager voiceManager;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlManager = new DeviceControlManager(this);
        aiOrchestrator = new AiOrchestrator(this, controlManager);
        voiceManager = new VoiceManager(this, this);

        btnStatusA11y = findViewById(R.id.btn_status_a11y);
        btnSettingsIa = findViewById(R.id.btn_settings_ia);
        btnToggleHud = findViewById(R.id.btn_toggle_hud);
        scrollHistory = findViewById(R.id.scroll_history);
        layoutChatHistory = findViewById(R.id.layout_chat_history);
        tvVoiceStatus = findViewById(R.id.tv_voice_status);
        etCommandInput = findViewById(R.id.et_command_input);
        btnVoiceRecord = findViewById(R.id.btn_voice_record);
        btnSendCommand = findViewById(R.id.btn_send_command);

        // Configuração de IA (Gemini, Groq, Claude, OpenAI, etc.)
        btnSettingsIa.setOnClickListener(v -> showSettingsDialog());

        // Ação do Botão de Acessibilidade
        btnStatusA11y.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        // Ação da Cápsula Flutuante
        btnToggleHud.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                startService(new Intent(this, FloatingHudService.class));
                Toast.makeText(this, "Cápsula flutuante ativada!", Toast.LENGTH_SHORT).show();
            }
        });

        // Ação de Enviar Comando por Texto
        btnSendCommand.setOnClickListener(v -> processInputCommand());
        etCommandInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                processInputCommand();
                return true;
            }
            return false;
        });

        // Ação de Microfone (Voz)
        btnVoiceRecord.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQ_AUDIO);
            } else {
                toggleVoiceListening();
            }
        });

        updateSettingsButtonLabel();
    }

    private void updateSettingsButtonLabel() {
        LlmConfig cfg = aiOrchestrator.getConfig();
        String hasKey = !cfg.getApiKey().isEmpty() ? " 🟢" : " 🔴";
        btnSettingsIa.setText("⚙️ " + cfg.getProvider().name() + hasKey);
    }

    private void showSettingsDialog() {
        LlmConfig cfg = aiOrchestrator.getConfig();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 24, 40, 16);

        TextView tvProv = new TextView(this);
        tvProv.setText("Selecione o Provedor de IA:");
        tvProv.setTextColor(Color.WHITE);
        layout.addView(tvProv);

        Spinner spinner = new Spinner(this);
        LlmConfig.Provider[] providers = LlmConfig.Provider.values();
        String[] names = new String[providers.length];
        int selectedIndex = 0;
        for (int i = 0; i < providers.length; i++) {
            names[i] = providers[i].displayName;
            if (providers[i] == cfg.getProvider()) selectedIndex = i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        layout.addView(spinner);

        TextView tvKey = new TextView(this);
        tvKey.setText("\nChave de API (API Key):");
        tvKey.setTextColor(Color.WHITE);
        layout.addView(tvKey);

        EditText etKey = new EditText(this);
        etKey.setHint("Cole sua API Key aqui...");
        etKey.setText(cfg.getApiKey());
        etKey.setTextColor(Color.WHITE);
        etKey.setHintTextColor(Color.GRAY);
        layout.addView(etKey);

        TextView tvModel = new TextView(this);
        tvModel.setText("\nModelo Neural:");
        tvModel.setTextColor(Color.WHITE);
        layout.addView(tvModel);

        EditText etModel = new EditText(this);
        etModel.setText(cfg.getModel());
        etModel.setTextColor(Color.WHITE);
        layout.addView(etModel);

        new AlertDialog.Builder(this)
                .setTitle("🧠 Cérebro de IA do OpenBrain")
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    int pos = spinner.getSelectedItemPosition();
                    cfg.setProvider(providers[pos]);
                    cfg.setApiKey(etKey.getText().toString());
                    cfg.setModel(etModel.getText().toString());
                    updateSettingsButtonLabel();
                    Toast.makeText(this, "IA configurada: " + providers[pos].displayName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toggleVoiceListening() {
        if (!isListening) {
            voiceManager.startListening();
        } else {
            voiceManager.stopListening();
        }
    }

    private void processInputCommand() {
        String text = etCommandInput.getText().toString().trim();
        if (!text.isEmpty()) {
            etCommandInput.setText("");
            executeJarvisCommand(text);
        }
    }

    private void executeJarvisCommand(String prompt) {
        addMessageBubble("👤 VOCÊ", prompt, Color.parseColor("#38BDF8"), Color.parseColor("#1E293B"));
        tvVoiceStatus.setText("🧠 Jarvis pensando...");

        // O Cérebro Neural (Gemini / LLM) processa a ordem e dispara as Tools
        aiOrchestrator.processPrompt(prompt, result -> {
            tvVoiceStatus.setText("Toque no microfone para falar ou digite abaixo");

            // Feedback Falado via TTS
            voiceManager.speak(result.spokenText);

            // Feedback Visual
            String responseDisplay = result.spokenText + (result.detailedLog != null && !result.detailedLog.isEmpty() ? "\n\n" + result.detailedLog : "");
            int textColor = result.success ? Color.parseColor("#4ADE80") : Color.parseColor("#F87171");
            addMessageBubble("🤖 JARVIS", responseDisplay, textColor, Color.parseColor("#0F172A"));
        });
    }

    private void addMessageBubble(String sender, String message, int textColor, int bgColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(bgColor);
        card.setPadding(24, 18, 24, 18);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        card.setLayoutParams(params);

        TextView tvSender = new TextView(this);
        tvSender.setText(sender);
        tvSender.setTextColor(textColor);
        tvSender.setTextSize(11);
        tvSender.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvSender);

        TextView tvMsg = new TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(Color.parseColor("#F1F5F9"));
        tvMsg.setTextSize(14);
        tvMsg.setPadding(0, 6, 0, 0);
        card.addView(tvMsg);

        layoutChatHistory.addView(card);
        scrollHistory.post(() -> scrollHistory.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }

    private void updateStatuses() {
        boolean a11yOk = OpenBrainAccessibilityService.isRunning();
        if (a11yOk) {
            btnStatusA11y.setText("🟢 Acessibilidade Conectada (Pronto)");
            btnStatusA11y.setBackgroundColor(Color.parseColor("#065F46"));
        } else {
            btnStatusA11y.setText("🔴 Toque para Conectar o Serviço de Acessibilidade");
            btnStatusA11y.setBackgroundColor(Color.parseColor("#991B1B"));
        }
    }

    // Callbacks de Voz (VoiceManager.VoiceCallback)
    @Override
    public void onSpeechRecognized(String text) {
        runOnUiThread(() -> executeJarvisCommand(text));
    }

    @Override
    public void onListeningStateChanged(boolean isListening) {
        this.isListening = isListening;
        runOnUiThread(() -> {
            if (isListening) {
                tvVoiceStatus.setText("🔴 Ouvindo você... Pode falar!");
                btnVoiceRecord.setText("⏹️");
            } else {
                tvVoiceStatus.setText("Toque no microfone para falar ou digite abaixo");
                btnVoiceRecord.setText("🎙️");
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Toque no microfone para falar ou digite abaixo");
            btnVoiceRecord.setText("🎙️");
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQ_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleVoiceListening();
        } else {
            Toast.makeText(this, "Permissão de microfone necessária para voz.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
