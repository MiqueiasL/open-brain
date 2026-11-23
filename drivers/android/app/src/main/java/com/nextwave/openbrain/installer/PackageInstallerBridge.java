package com.nextwave.openbrain.installer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.nextwave.openbrain.shizuku.ShizukuManager;
import java.io.File;

/**
 * OpenBrain Package Installer Bridge
 * Permite que o OpenBrain instale e atualize aplicativos no smartphone de forma 100% autônoma,
 * sem necessidade de conectar cabo ao computador ou abrir o Chrome Remote Desktop.
 */
public class PackageInstallerBridge {
    private static final String TAG = "PackageInstallerBridge";

    /**
     * Instala um APK silenciosamente via Shizuku (privilégios ADB sem root e sem PC)
     */
    public static boolean installSilentlyViaShizuku(String apkPath) {
        if (!ShizukuManager.getInstance().hasPermission()) {
            Log.w(TAG, "Shizuku não disponível para instalação silenciosa.");
            return false;
        }

        Log.i(TAG, "🚀 Iniciando instalação silenciosa via Shizuku: " + apkPath);
        String result = ShizukuManager.getInstance().executeShellCommand("pm install -r -d \"" + apkPath + "\"");
        Log.i(TAG, "Resultado da instalação: " + result);

        return result.toLowerCase().contains("success");
    }

    /**
     * Instala um APK via FileProvider do sistema (modo padrão com prompt de confirmação do Android)
     */
    public static void installViaSystemIntent(Context context, File apkFile) {
        Log.i(TAG, "📲 Disparando PackageInstaller nativo para: " + apkFile.getAbsolutePath());
        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                apkFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }
}
