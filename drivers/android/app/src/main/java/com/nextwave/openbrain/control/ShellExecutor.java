package com.nextwave.openbrain.control;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ShellExecutor (Método 3: Shell Nativo sem Modo Dev)
 * Permite que o OpenBrain execute comandos de terminal locais dentro do Android
 * através de ProcessBuilder sem precisar de Shizuku ou Modo Desenvolvedor.
 */
public class ShellExecutor {
    private static final String TAG = "ShellExecutor";

    public static String execute(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder()
                    .command("/system/bin/sh", "-c", command)
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            Log.i(TAG, "Comando executado [Exit " + exitCode + "]: " + command);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao executar shell: " + e.getMessage());
            return "Erro ao executar shell: " + e.getMessage();
        }

        String result = output.toString().trim();
        return result.isEmpty() ? "Comando executado com sucesso (sem retorno)." : result;
    }
}
