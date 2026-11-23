package com.nextwave.openbrain.shizuku;

import android.content.pm.PackageManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import rikka.shizuku.Shizuku;

/**
 * OpenBrain Shizuku Manager
 * Permite execução de comandos de terminal com privilégios de ADB/Root diretamente no celular,
 * sem precisar de cabo USB ou computador conectado.
 */
public class ShizukuManager {
    private static final String TAG = "ShizukuManager";
    private static ShizukuManager instance;

    private boolean isBinderReceived = false;
    private boolean isPermissionGranted = false;

    private final Shizuku.OnBinderReceivedListener binderReceivedListener = () -> {
        Log.i(TAG, "🟢 Shizuku Binder recebido com sucesso!");
        isBinderReceived = true;
        hasPermission();
    };

    private final Shizuku.OnBinderDeadListener binderDeadListener = () -> {
        Log.w(TAG, "🔴 Shizuku Binder desconectado.");
        isBinderReceived = false;
        isPermissionGranted = false;
    };

    private final Shizuku.OnRequestPermissionResultListener permissionResultListener = (requestCode, grantResult) -> {
        isPermissionGranted = (grantResult == PackageManager.PERMISSION_GRANTED);
        Log.i(TAG, "Shizuku permissão resultado: " + (isPermissionGranted ? "CONCEDIDA" : "NEGADA"));
    };

    public static synchronized ShizukuManager getInstance() {
        if (instance == null) {
            instance = new ShizukuManager();
        }
        return instance;
    }

    public void init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener);
            Shizuku.addBinderDeadListener(binderDeadListener);
            Shizuku.addRequestPermissionResultListener(permissionResultListener);
        } catch (Throwable t) {
            Log.e(TAG, "Erro ao registrar listeners do Shizuku: " + t.getMessage());
        }
    }

    public boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean hasPermission() {
        if (!isShizukuAvailable()) return false;
        try {
            if (Shizuku.isPreV11()) return false;
            isPermissionGranted = (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED);
            return isPermissionGranted;
        } catch (Throwable t) {
            return false;
        }
    }

    public void requestPermission(int requestCode) {
        if (!isShizukuAvailable()) {
            Log.w(TAG, "Não é possível solicitar permissão: Shizuku não está em execução.");
            return;
        }
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(requestCode);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Falha ao solicitar permissão Shizuku: " + t.getMessage());
        }
    }

    public String executeShellCommand(String command) {
        if (!hasPermission()) {
            return "[Erro] Permissão do Shizuku não concedida ou Shizuku inativo no aparelho.";
        }

        StringBuilder output = new StringBuilder();
        try {
            Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                output.append("[stderr] ").append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao executar comando Shizuku: " + command, e);
            return "[Exceção] " + e.getMessage();
        }

        return output.toString().trim();
    }
}
