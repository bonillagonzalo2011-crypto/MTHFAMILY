package com.mthfamily.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LogoView extends View {
    private final boolean tile;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG), dark = new Paint(Paint.ANTI_ALIAS_FLAG),
            disc = new Paint(Paint.ANTI_ALIAS_FLAG), ring3 = new Paint(Paint.ANTI_ALIAS_FLAG),
            ring1 = new Paint(Paint.ANTI_ALIAS_FLAG), t1 = new Paint(Paint.ANTI_ALIAS_FLAG),
            t2 = new Paint(Paint.ANTI_ALIAS_FLAG), bg = new Paint(Paint.ANTI_ALIAS_FLAG),
            bd = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path big = new Path(), small = new Path();
    private float angle;
    private ValueAnimator anim;

    public LogoView(Context c, boolean tile, int alpha) {
        super(c);
        this.tile = tile;
        for (Paint p : new Paint[]{fill, ring3, ring1, t1, t2}) { p.setColor(Color.WHITE); p.setAlpha(alpha); }
        ring3.setStyle(Paint.Style.STROKE); ring3.setStrokeWidth(3.2f);
        ring1.setStyle(Paint.Style.STROKE); ring1.setStrokeWidth(1f);
        dark.setColor(0xFF080808); dark.setStrokeWidth(1.2f);
        disc.setColor(0xFF080808);
        bg.setColor(0xFF080808);
        bd.setColor(0xFFFFFFFF); bd.setStyle(Paint.Style.STROKE);
        t1.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD)); t1.setTextSize(28); t1.setTextAlign(Paint.Align.CENTER); t1.setLetterSpacing(0.07f);
        t2.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD)); t2.setTextSize(9); t2.setTextAlign(Paint.Align.CENTER); t2.setLetterSpacing(0.44f);
        big.moveTo(-7, -68); big.lineTo(0, -98); big.lineTo(7, -68); big.close();
        small.moveTo(-5, -68); small.lineTo(0, -87); small.lineTo(5, -68); small.close();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        anim = ValueAnimator.ofFloat(0, 360);
        anim.setDuration(24000); anim.setInterpolator(new LinearInterpolator()); anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.addUpdateListener(v -> { angle = (float) v.getAnimatedValue(); invalidate(); });
        anim.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (anim != null) anim.cancel();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), s = Math.min(w, h);
        if (tile) {
            c.drawRoundRect(0, 0, w, h, s * .22f, s * .22f, bg);
            bd.setStrokeWidth(s * .045f);
            c.drawRoundRect(s * .025f, s * .025f, w - s * .025f, h - s * .025f, s * .215f, s * .215f, bd);
        }
        c.save();
        c.translate(w / 2, h / 2);
        float k = s / (tile ? 222f : 200f);
        c.scale(k, k);
        c.save();
        c.rotate(angle);
        for (int i = 0; i < 12; i++) {
            c.drawPath(big, fill);
            c.drawLine(0, -94, 0, -70, dark);
            c.save(); c.rotate(15); c.drawPath(small, fill); c.restore();
            c.rotate(30);
        }
        c.restore();
        c.drawCircle(0, 0, 69, disc);
        c.drawCircle(0, 0, 69, ring3);
        c.drawCircle(0, 0, 63, ring1);
        c.drawText("MTH", 1, 1, t1);
        c.drawText("FAMILY", 2, 17, t2);
        c.restore();
    }
}
