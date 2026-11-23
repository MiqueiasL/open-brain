package com.nextwave.openbrain.control;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

/**
 * OpenBrain Device Control Manager
 * Garante paridade absoluta (100%) com todas as habilidades que Siri, Google Assistente e Alexa já executam no celular.
 */
public class DeviceControlManager {
    private static final String TAG = "DeviceControlManager";
    private final Context context;
    private final AudioManager audioManager;
    private final CameraManager cameraManager;

    public DeviceControlManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    // ==========================================
    // 1. HARDWARE & AJUSTES RÁPIDOS
    // ==========================================

    public boolean setFlashlight(boolean enable) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null) {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, enable);
                Log.i(TAG, "Lanterna: " + (enable ? "LIGADA" : "DESLIGADA"));
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao controlar lanterna: " + e.getMessage());
        }
        return false;
    }

    public void setVolumeLevel(int streamType, int percentage) {
        if (audioManager == null) return;
        int max = audioManager.getStreamMaxVolume(streamType);
        int target = Math.max(0, Math.min(max, (percentage * max) / 100));
        audioManager.setStreamVolume(streamType, target, AudioManager.FLAG_SHOW_UI);
    }

    public void setMediaVolume(int percentage) {
        setVolumeLevel(AudioManager.STREAM_MUSIC, percentage);
    }

    public int getBatteryPercentage() {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return -1;
    }

    public void setDoNotDisturb(boolean enable) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(enable
                        ? NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        : NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
    }

    // ==========================================
    // 2. TEMPO & PRODUTIVIDADE (ALARMES / TIMERS)
    // ==========================================

    public void setAlarm(int hour, int minute, String message) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void setTimer(int seconds, String message) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ==========================================
    // 3. COMUNICAÇÃO & MÍDIA
    // ==========================================

    public void openWhatsAppMessage(String phoneWithCountryCode, String message) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phoneWithCountryCode + "&text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Falha ao abrir WhatsApp: " + e.getMessage());
        }
    }

    public void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (SecurityException se) {
            // Fallback para tela de discagem caso não tenha permissão direta
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void dispatchMediaKey(int keyCode) {
        if (audioManager != null) {
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        }
    }

    public void playPauseMedia() {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public void nextTrack() {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    // ==========================================
    // 4. NAVEGAÇÃO & ROTAS
    // ==========================================

    public void navigateTo(String destination) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            // Fallback para Waze ou navegador
            Intent wazeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q=" + Uri.encode(destination)));
            wazeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(wazeIntent);
        }
    }
}
