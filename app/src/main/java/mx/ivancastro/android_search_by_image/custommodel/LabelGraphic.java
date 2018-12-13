package mx.ivancastro.android_search_by_image.custommodel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/** Graphic instance for rendering image labels. */
public class LabelGraphic extends GraphicOverlay.Graphic {

    private final Paint textPaint;
    private final GraphicOverlay overlay;

    private String label;

    LabelGraphic(GraphicOverlay overlay) {
        super(overlay);
        this.overlay = overlay;
        textPaint = new Paint();
        textPaint.setColor(Color.CYAN);
        textPaint.setTextSize(40.0f);
    }

    synchronized void updateLabel(String label) {
        this.label = label;
        postInvalidate();
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = overlay.getWidth() / 5.0f;
        float y = overlay.getHeight() / 5.0f;

        canvas.drawText(label, x, y, textPaint);
    }
}
