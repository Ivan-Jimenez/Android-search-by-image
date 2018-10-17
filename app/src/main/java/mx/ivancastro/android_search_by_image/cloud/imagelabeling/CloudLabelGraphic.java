package mx.ivancastro.android_search_by_image.cloud.imagelabeling;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/**
 * Graphic instance for rendering detected label.
 */
public class CloudLabelGraphic extends GraphicOverlay.Graphic {
    private final Paint textPaint;
    private final GraphicOverlay graphicOverlay;

    private List<String> labels;

    CloudLabelGraphic (GraphicOverlay overlay) {
        super(overlay);
        this.graphicOverlay = overlay;
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60.0f);
    }

    synchronized void updateLabel (List<String> labels) {
        this.labels = labels;
        postInvalidate();
    }

    @Override
    public synchronized void draw (Canvas canvas) {
        float x = graphicOverlay.getWidth() / 4.0f;
        float y = graphicOverlay.getHeight() / 4.0f;

        for (String label : labels) {
            canvas.drawText(label, x, y, textPaint);
            y = y - 62.0f;
        }
    }
}
