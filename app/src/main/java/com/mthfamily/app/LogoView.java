package com.mthfamily.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;

public class FloatingService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private SharedPreferences prefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Inicializar SharedPreferences y forzar todas las funciones en falso (apagadas) por defecto
        prefs = getSharedPreferences("MthSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (!prefs.contains("pref_aimbot")) editor.putBoolean("pref_aimbot", false);
        if (!prefs.contains("pref_aim_assist")) editor.putBoolean("pref_aim_assist", false);
        if (!prefs.contains("pref_show_fov")) editor.putBoolean("pref_show_fov", false);
        if (!prefs.contains("pref_show_line")) editor.putBoolean("pref_show_line", false);
        if (!prefs.contains("pref_no_recoil")) editor.putBoolean("pref_no_recoil", false);
        editor.apply();

        // 2. Inflar el diseño del menú flotante
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);

        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        // 3. Conectar cada switch y asegurar que arranquen apagados
        setupSwitch(floatingView.findViewById(R.id.switch_aimbot), "pref_aimbot");
        setupSwitch(floatingView.findViewById(R.id.switch_aim_assist), "pref_aim_assist");
        setupSwitch(floatingView.findViewById(R.id.switch_show_fov), "pref_show_fov");
        setupSwitch(floatingView.findViewById(R.id.switch_show_line), "pref_show_line");
        setupSwitch(floatingView.findViewById(R.id.switch_no_recoil), "pref_no_recoil");
    }

    private void setupSwitch(Switch sw, String key) {
        if (sw != null) {
            // Forzar estado inicial con el valor guardado (por defecto false)
            sw.setChecked(prefs.getBoolean(key, false));
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(key, isChecked).apply();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
                    }
