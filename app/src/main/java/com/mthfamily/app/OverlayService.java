package com.mthfamily.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble, menu;
    private WindowManager.LayoutParams bp, mp;
    private boolean keep = true;

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        getSystemService(NotificationManager.class).createNotificationChannel(
                new NotificationChannel("mth", "MTH FAMILY", NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, "mth")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("MTH FAMILY")
                .setContentText("Bot\u00f3n flotante activo").build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(1, n);
        bp = params(dp(64), dp(64));
        bp.x = dp(16);
        bp.y = dp(200);
        bubble = new LogoView(this, true, 64);
        drag(bubble, bubble, bp, this::openMenu);
        wm.addView(bubble, bp);
    }

    private WindowManager.LayoutParams params(int w, int h) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(w, h,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        return p;
    }

    private void drag(View touch, View target, WindowManager.LayoutParams p, Runnable click) {
        touch.setOnTouchListener(new View.OnTouchListener() {
            float sx0, sy0; int px, py; boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx0 = e.getRawX(); sy0 = e.getRawY(); px = p.x; py = p.y; moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float mx = e.getRawX() - sx0, my = e.getRawY() - sy0;
                        if (Math.hypot(mx, my) > dp(10)) moved = true;
                        if (moved) {
                            DisplayMetrics m = getResources().getDisplayMetrics();
                            p.x = Math.max(0, Math.min(m.widthPixels - target.getWidth(), px + (int) mx));
                            p.y = Math.max(0, Math.min(m.heightPixels - target.getHeight(), py + (int) my));
                            wm.updateViewLayout(target, p);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved && click != null) click.run();
                        return true;
                }
                return false;
            }
        });
    }

    private void openMenu() {
        if (menu != null) return;
        wm.removeView(bubble);
        DisplayMetrics m = getResources().getDisplayMetrics();
        int w = Math.min(m.widthPixels - dp(20), dp(384)), h = dp(248);
        mp = params(w, h);
        int x = bp.x + dp(32) < m.widthPixels / 2 ? bp.x + dp(64) + dp(12) : bp.x - w - dp(12);
        mp.x = Math.max(dp(8), Math.min(m.widthPixels - w - dp(8), x));
        mp.y = Math.max(dp(8), Math.min(m.heightPixels - h - dp(8), bp.y + dp(32) - h / 2));
        menu = makeMenu();
        wm.addView(menu, mp);
    }

    private void closeMenu() {
        if (menu == null) return;
        wm.removeView(menu);
        menu = null;
        if (keep) wm.addView(bubble, bp);
        else stopSelf();
    }

    private GradientDrawable box(int fill, int stroke, int rad) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill); g.setStroke(Math.max(1, dp(1)), stroke); g.setCornerRadius(dp(rad));
        return g;
    }

    private TextView txt(String t, float sp, int color) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color);
        v.setTypeface(Typeface.MONOSPACE); v.setIncludeFontPadding(false);
        return v;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float wt) {
        return new LinearLayout.LayoutParams(w, h, wt);
    }

    private View check(String label, boolean on) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(4), 0, dp(4), 0);
        r.setBackground(box(0xFF111019, 0xFF211A2D, 0));
        final TextView bx = txt(on ? "\u2713" : "", 11, 0xFFFFFFFF);
        bx.setGravity(Gravity.CENTER);
        final boolean[] st = {on};
        final Runnable paint = () -> {
            bx.setText(st[0] ? "\u2713" : "");
            bx.setBackground(box(st[0] ? 0xFF84708F : 0xFF17141D, st[0] ? 0xFFA18DA9 : 0xFF55515B, 2));
        };
        paint.run();
        r.addView(bx, new LinearLayout.LayoutParams(dp(14), dp(14)));
        TextView l = txt(label, 10, 0xFFDED9E5);
        l.setPadding(dp(6), 0, 0, 0); l.setSingleLine(true);
        r.addView(l);
        r.setOnClickListener(v -> { st[0] = !st[0]; paint.run(); });
        return r;
    }

    private View radios(String a, String b) {
        final LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        final TextView[] dots = new TextView[2];
        String[] names = {a, b};
        for (int i = 0; i < 2; i++) {
            final int idx = i;
            LinearLayout o = new LinearLayout(this);
            o.setGravity(Gravity.CENTER_VERTICAL);
            dots[i] = txt("", 8, 0xFFFFFFFF);
            o.addView(dots[i], new LinearLayout.LayoutParams(dp(13), dp(13)));
            TextView l = txt(names[i], 9, 0xFFDED9E5);
            l.setPadding(dp(5), 0, 0, 0);
            o.addView(l);
            o.setOnClickListener(v -> {
                for (int k = 0; k < 2; k++) {
                    GradientDrawable g = new GradientDrawable();
                    g.setShape(GradientDrawable.OVAL);
                    g.setColor(k == idx ? 0xFF927BA0 : 0xFF17141D);
                    g.setStroke(dp(1), 0xFFA18DA9);
                    dots[k].setBackground(g);
                }
            });
            row.addView(o, lp(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
        ((View) dots[0].getParent()).performClick();
        return row;
    }

    private View makeMenu() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(box(0xFF09090B, 0xFF39363D, 6));
        card.setClipToOutline(true);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), 0, dp(8), 0);
        GradientDrawable tg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF41364A, 0xFF302934, 0xFF17151A});
        top.setBackground(tg);
        top.addView(txt("\u25bc", 12, 0xFFDDDDDD));
        TextView brand = txt("MTH TEAM \u2014 MTH FAMILY", 11, 0xFFEEEEEE);
        brand.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        brand.setLetterSpacing(0.14f); brand.setGravity(Gravity.CENTER); brand.setSingleLine(true);
        top.addView(brand, lp(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView x = txt("\u00d7", 17, 0xFFEEEEEE);
        x.setGravity(Gravity.CENTER);
        x.setBackground(box(0xFF302934, 0xFF695775, 3));
        x.setOnClickListener(v -> closeMenu());
        x.setOnLongClickListener(v -> { keep = false; closeMenu(); return true; });
        top.addView(x, new LinearLayout.LayoutParams(dp(24), dp(22)));
        card.addView(top, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(27)));

        LinearLayout body = new LinearLayout(this);
        LinearLayout side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setBackgroundColor(0xFF100B1B);
        final String[] tabs = {"AIM", "ESP", "CFG", "INFO"};
        final TextView[] nb = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            nb[i] = txt(tabs[i], 10, 0xFFD9CCE9);
            nb[i].setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            nb[i].setGravity(Gravity.CENTER);
            nb[i].setOnClickListener(v -> {
                for (int k = 0; k < 4; k++) {
                    nb[k].setBackgroundColor(k == idx ? 0xFF4B3F54 : 0xFF1D1B20);
                    nb[k].setTextColor(k == idx ? 0xFFFFFFFF : 0xFFD9CCE9);
                }
            });
            side.addView(nb[i], lp(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        }
        body.addView(side, new LinearLayout.LayoutParams(dp(42), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout ct = new LinearLayout(this);
        ct.setOrientation(LinearLayout.VERTICAL);
        ct.setBackgroundColor(0xFF0B0911);
        ct.setPadding(dp(7), dp(5), dp(7), dp(6));
        TextView title = txt("AIMBOT", 9, 0xFFA692B2);
        title.setGravity(Gravity.CENTER); title.setLetterSpacing(0.15f);
        title.setBackground(box(0xFF0B0911, 0xFF38313F, 0));
        ct.addView(title, lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(21), 0));

        String[][] opts = {{"Activar Aimbot", "1"}, {"Aim Assist", "0"}, {"Mostrar FOV", "1"}, {"Mostrar l\u00ednea", "0"}, {"Quitar retroceso", "0"}};
        for (int i = 0; i < opts.length; i += 2) {
            LinearLayout r = new LinearLayout(this);
            LinearLayout.LayoutParams l1 = lp(0, dp(21), 1f);
            l1.rightMargin = dp(3);
            LinearLayout.LayoutParams l2 = lp(0, dp(21), 1f);
            l2.leftMargin = dp(3);
            r.addView(check(opts[i][0], opts[i][1].equals("1")), l1);
            if (i + 1 < opts.length) r.addView(check(opts[i + 1][0], opts[i + 1][1].equals("1")), l2);
            else r.addView(new View(this), l2);
            LinearLayout.LayoutParams rl = lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(21), 0);
            rl.topMargin = dp(i == 0 ? 5 : 3);
            ct.addView(r, rl);
        }

        LinearLayout sl = new LinearLayout(this);
        sl.setGravity(Gravity.CENTER_VERTICAL);
        SeekBar sk = new SeekBar(this);
        sk.setMax(100); sk.setProgress(95);
        sk.setProgressTintList(ColorStateList.valueOf(0xFF8B7798));
        sk.setThumbTintList(ColorStateList.valueOf(0xFF725D83));
        final TextView val = txt("95", 10, 0xFFEEEEEE);
        val.setGravity(Gravity.CENTER);
        val.setBackground(box(0xFF181420, 0xFF292132, 0));
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar b, int p, boolean u) { val.setText(String.valueOf(p)); }
            @Override public void onStartTrackingTouch(SeekBar b) { }
            @Override public void onStopTrackingTouch(SeekBar b) { }
        });
        sl.addView(sk, lp(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        sl.addView(val, lp(dp(30), dp(19), 0));
        TextView sll = txt("FOV regular", 9, 0xFFC8C0D0);
        sll.setGravity(Gravity.CENTER);
        sl.addView(sll, lp(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams sp = lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(25), 0);
        sp.topMargin = dp(4);
        ct.addView(sl, sp);

        LinearLayout sr = new LinearLayout(this);
        sr.setGravity(Gravity.CENTER_VERTICAL);
        sr.addView(txt("Zona objetivo:", 9, 0xFFBFB7C8), lp(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        final String[] zonas = {"Cuello", "Cabeza", "Cuerpo"};
        final int[] zi = {0};
        final TextView sel = txt("Cuello \u25be", 10, 0xFFE4DBEA);
        sel.setGravity(Gravity.CENTER_VERTICAL);
        sel.setPadding(dp(5), 0, dp(5), 0);
        sel.setBackground(box(0xFF262329, 0xFF5D5265, 2));
        sel.setOnClickListener(v -> { zi[0] = (zi[0] + 1) % 3; sel.setText(zonas[zi[0]] + " \u25be"); });
        LinearLayout.LayoutParams selp = lp(0, dp(21), 1f);
        selp.leftMargin = dp(6);
        sr.addView(sel, selp);
        ct.addView(sr, lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(27), 0));

        ct.addView(txt("Tipo de asistencia:", 9, 0xFFBFB7C8));
        LinearLayout.LayoutParams rp = lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(24), 0);
        rp.topMargin = dp(4);
        ct.addView(radios("Al disparar", "Al apuntar"), rp);

        body.addView(ct, lp(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        card.addView(body, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(221)));

        drag(top, card, mp, null);
        return card;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try { if (menu != null) wm.removeView(menu); } catch (Exception e) { }
        try { if (menu == null) wm.removeView(bubble); } catch (Exception e) { }
    }
}
