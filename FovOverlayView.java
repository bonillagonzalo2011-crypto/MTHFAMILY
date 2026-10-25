package com.mth.family;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class FovOverlayView extends View {
    private Paint paint;
    private float fovRadius = 95f; // Valor inicial del FOV
    private boolean showFov = false;

    public FovOverlayView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        // Color verde semitransparente para que se vea bien sobre el juego
        paint.setColor(Color.argb(180, 0, 255, 0)); 
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setAntiAlias(true);
    }

    public void setFovRadius(float radius) {
        this.fovRadius = radius;
        invalidate(); // Vuelve a dibujar el círculo con el nuevo tamaño
    }

    public void setShowFov(boolean show) {
        this.showFov = show;
        invalidate(); // Muestra u oculta el círculo según el switch
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showFov) {
            // Dibuja el círculo justo en el centro de la pantalla
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            canvas.drawCircle(cx, cy, fovRadius, paint);
        }
    }
                             }
