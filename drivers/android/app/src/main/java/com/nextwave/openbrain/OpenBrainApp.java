package com.nextwave.openbrain;

import android.app.Application;
import android.util.Log;
import com.nextwave.openbrain.shizuku.ShizukuManager;

public class OpenBrainApp extends Application {
    private static final String TAG = "OpenBrainApp";
    private static OpenBrainApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG, "🧠 OpenBrain App iniciado. Inicializando Shizuku e Subsistemas...");

        // Inicializar listeners do Shizuku
        ShizukuManager.getInstance().init();
    }

    public static OpenBrainApp getInstance() {
        return instance;
    }
}
