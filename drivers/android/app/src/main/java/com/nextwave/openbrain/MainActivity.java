package com.nextwave.openbrain;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nextwave.openbrain.accessibility.OpenBrainAccessibilityService;
import com.nextwave.openbrain.control.DeviceControlManager;
import com.nextwave.openbrain.hud.FloatingHudService;
import com.nextwave.openbrain.shizuku.ShizukuManager;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatusA11y;
    private TextView tvStatusShizuku;
    private TextView tvStatusOverlay;
    private DeviceControlManager controlManager;
    private boolean isTorchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlManager = new DeviceControlManager(this);

        tvStatusA11y = findViewById(R.id.tv_status_a11y);
        tvStatusShizuku = findViewById(R.id.tv_status_shizuku);
        tvStatusOverlay = findViewById(R.id.tv_status_overlay);

        Button btnEnableA11y = findViewById(R.id.btn_enable_a11y);
        Button btnReqShizuku = findViewById(R.id.btn_req_shizuku);
        Button btnStartHud = findViewById(R.id.btn_start_hud);
        Button btnTestTorch = findViewById(R.id.btn_test_torch);
        Button btnTestBattery = findViewById(R.id.btn_test_battery);

        btnEnableA11y.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnReqShizuku.setOnClickListener(v -> {
            if (ShizukuManager.getInstance().isShizukuAvailable()) {
                ShizukuManager.getInstance().requestPermission(101);
            } else {
                Toast.makeText(this, "Shizuku não está rodando no aparelho!", Toast.LENGTH_LONG).show();
            }
        });

        btnStartHud.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                startService(new Intent(this, FloatingHudService.class));
                Toast.makeText(this, "Cápsula flutuante iniciada!", Toast.LENGTH_SHORT).show();
            }
        });

        btnTestTorch.setOnClickListener(v -> {
            isTorchOn = !isTorchOn;
            controlManager.setFlashlight(isTorchOn);
            Toast.makeText(this, "Lanterna: " + (isTorchOn ? "Ligada" : "Desligada"), Toast.LENGTH_SHORT).show();
        });

        btnTestBattery.setOnClickListener(v -> {
            int level = controlManager.getBatteryPercentage();
            Toast.makeText(this, "Nível da Bateria: " + level + "%", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }

    private void updateStatuses() {
        boolean a11yOk = OpenBrainAccessibilityService.isRunning();
        tvStatusA11y.setText("• Acessibilidade: " + (a11yOk ? "🟢 CONECTADA" : "🔴 DESATIVADA"));

        boolean shizukuOk = ShizukuManager.getInstance().hasPermission();
        tvStatusShizuku.setText("• Shizuku ADB: " + (shizukuOk ? "🟢 CONECTADO" : "🔴 NÃO AUTORIZADO"));

        boolean overlayOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        tvStatusOverlay.setText("• Janela Flutuante (HUD): " + (overlayOk ? "🟢 AUTORIZADA" : "🔴 NÃO AUTORIZADA"));
    }
}
