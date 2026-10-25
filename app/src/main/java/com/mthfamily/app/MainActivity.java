package com.mthfamily.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private float d;

    private View btn() {
        View v = new View(this);
        GradientDrawable g = new GradientDrawable();
        g.setColor(0x12FFFFFF); g.setStroke((int) d, 0x47FFFFFF); g.setCornerRadius(12 * d);
        v.setBackground(g);
        return v;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF080808);
        getWindow().setNavigationBarColor(0xFF080808);
        DisplayMetrics m = getResources().getDisplayMetrics();
        d = m.density;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF080808);
        int s = (int) Math.min(m.widthPixels * .66f, 350 * d);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(s, s, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        lp.topMargin = (int) (m.heightPixels * .1f);
        root.addView(new LogoView(this, false, 41), lp);

        int side = (int) (m.widthPixels * .06f);
        LinearLayout st = new LinearLayout(this);
        st.setOrientation(LinearLayout.VERTICAL);
        st.setPadding(side, 0, side, (int) (m.heightPixels * .06f));
        int bh = (int) (46 * d), gap = (int) (10 * d);

        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams h1 = new LinearLayout.LayoutParams(0, bh, 1f);
        h1.rightMargin = gap / 2;
        LinearLayout.LayoutParams h2 = new LinearLayout.LayoutParams(0, bh, 1f);
        h2.leftMargin = gap / 2;
        row.addView(btn(), h1);
        row.addView(btn(), h2);
        st.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bh));

        TextView big = new TextView(this);
        big.setBackground(btn().getBackground());
        big.setText("INICIAR MENU");
        big.setTextColor(0xFFFFFFFF);
        big.setTextSize(14);
        big.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        big.setLetterSpacing(0.22f);
        big.setGravity(Gravity.CENTER);
        big.setOnClickListener(v -> startFloating());
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bh);
        bl.topMargin = gap;
        st.addView(big, bl);

        LinearLayout tag = new LinearLayout(this);
        tag.setGravity(Gravity.CENTER_VERTICAL);
        TextView tv = new TextView(this);
        tv.setText("By MTH FAMILY");
        tv.setTextColor(0x29FFFFFF);
        tv.setTextSize(10);
        tv.setLetterSpacing(0.1f);
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tv.setSingleLine(true);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(0, (int) d, 1f);
        ll.rightMargin = (int) (6 * d);
        LinearLayout.LayoutParams lr = new LinearLayout.LayoutParams(0, (int) d, 1f);
        lr.leftMargin = (int) (6 * d);
        View l1 = new View(this), l2 = new View(this);
        l1.setBackgroundColor(0x29FFFFFF);
        l2.setBackgroundColor(0x29FFFFFF);
        tag.addView(l1, ll);
        tag.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tag.addView(l2, lr);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tl.topMargin = (int) (14 * d);
        st.addView(tag, tl);

        root.addView(st, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
    }

    private boolean waiting = false;

    private void startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Mostrar sobre otras apps")
                    .setMessage("Para poner el bot\u00f3n flotante sobre otras apps, activa el permiso \"Mostrar sobre otras apps\" para MTH FAMILY.")
                    .setPositiveButton("Ir a ajustes", (d, w) -> {
                        waiting = true;
                        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
                    })
                    .setNegativeButton("Ahora no", null)
                    .show();
            return;
        }
        startForegroundService(new Intent(this, OverlayService.class));
        moveTaskToBack(true);
    }

    @Override protected void onResume() {
        super.onResume();
        if (waiting && Settings.canDrawOverlays(this)) {
            waiting = false;
            startFloating();
        }
    }
}
