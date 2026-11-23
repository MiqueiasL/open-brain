package com.nextwave.openbrain.hud;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.nextwave.openbrain.accessibility.OpenBrainAccessibilityService;
import com.nextwave.openbrain.shizuku.ShizukuManager;
import java.util.List;

/**
 * OpenBrain Floating HUD Service
 * Cápsula flutuante tática e compacta sobre qualquer aplicativo.
 */
public class FloatingHudService extends Service {
    private static final String CHANNEL_ID = "openbrain_hud_channel";
    private WindowManager windowManager;
    private View hudView;
    private WindowManager.LayoutParams params;
    private boolean isExpanded = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("OpenBrain Ativo")
                .setContentText("HUD Flutuante operacional")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1001, notification);
        initFloatingView();
    }

    private void initFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 20;
        params.y = 250;

        // Container Principal
        FrameLayout rootLayout = new FrameLayout(this);

        // Cápsula Compacta
        TextView capsule = new TextView(this);
        capsule.setText("🧠 OpenBrain");
        capsule.setTextColor(Color.WHITE);
        capsule.setTextSize(13);
        capsule.setPadding(28, 16, 28, 16);
        capsule.setBackgroundColor(Color.parseColor("#0F172A"));

        // Card Expandido de Ações
        LinearLayout expandedCard = new LinearLayout(this);
        expandedCard.setOrientation(LinearLayout.VERTICAL);
        expandedCard.setBackgroundColor(Color.parseColor("#1E293B"));
        expandedCard.setPadding(32, 24, 32, 24);
        expandedCard.setVisibility(View.GONE);

        TextView statusTxt = new TextView(this);
        statusTxt.setText("⚡ AÇÕES RÁPIDAS");
        statusTxt.setTextColor(Color.parseColor("#38BDF8"));
        statusTxt.setTextSize(12);
        expandedCard.addView(statusTxt);

        // Botão 1: Ler Tela Atual
        TextView btnScan = createActionButton("👁️ Ler Tela Atual", () -> {
            if (OpenBrainAccessibilityService.isRunning()) {
                List<String> texts = OpenBrainAccessibilityService.getInstance().getVisibleScreenText();
                Toast.makeText(this, "Capturados " + texts.size() + " textos da tela!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Acessibilidade desativada!", Toast.LENGTH_SHORT).show();
            }
        });
        expandedCard.addView(btnScan);

        // Botão 2: Testar Shizuku
        TextView btnShizuku = createActionButton("⚡ Status Shizuku", () -> {
            boolean ok = ShizukuManager.getInstance().hasPermission();
            Toast.makeText(this, "Shizuku ADB: " + (ok ? "🟢 CONECTADO" : "🔴 DESCONECTADO"), Toast.LENGTH_SHORT).show();
        });
        expandedCard.addView(btnShizuku);

        // Botão 3: Fechar HUD
        TextView btnClose = createActionButton("❌ Fechar HUD", this::stopSelf);
        expandedCard.addView(btnClose);

        rootLayout.addView(capsule);
        rootLayout.addView(expandedCard);
        hudView = rootLayout;

        // Gestos de Arrastar e Alternar Expansão
        capsule.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isClick = true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 10 || Math.abs(event.getRawY() - initialTouchY) > 10) {
                            isClick = false;
                        }
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(hudView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            isExpanded = !isExpanded;
                            expandedCard.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                            capsule.setText(isExpanded ? "➖ Minimizar" : "🧠 OpenBrain");
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(hudView, params);
    }

    private TextView createActionButton(String label, Runnable onClick) {
        TextView btn = new TextView(this);
        btn.setText(label);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(13);
        btn.setPadding(16, 16, 16, 16);
        btn.setOnClickListener(v -> onClick.run());
        return btn;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "OpenBrain HUD",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hudView != null && windowManager != null) {
            windowManager.removeView(hudView);
        }
    }
}
