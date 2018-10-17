package mx.ivancastro.android_search_by_image.cloud.landmarkrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;

import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/**
 * Graphic rendering detected landmark
 */
public class CloudLandmarkGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.CYAN;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROCKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint landmarkPaint;
    private FirebaseVisionCloudLandmark landmark;

    CloudLandmarkGraphic (GraphicOverlay overlay) {
        super(overlay);

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROCKE_WIDTH);

        landmarkPaint = new Paint();
        landmarkPaint.setColor(TEXT_COLOR);
        landmarkPaint.setColor(TEXT_COLOR);
        landmarkPaint.setTextSize(TEXT_SIZE);
    }

    /**
     * Updates the landmark instance from the detection of the most recent frame. Invalidates the
     * relevant potions of the overlay ti trigger a redraw.
     * @param landmark
     */
    void updateLandmark (FirebaseVisionCloudLandmark landmark) {
        this.landmark = landmark;
        postInvalidate();
    }

    /**
     * Draws the landmark block annotations for position, size, and raw value on the supplied canvas.
     * @param canvas
     */
    @Override
    public void draw (Canvas canvas) {
        if (landmark == null)
            throw new IllegalStateException("Attempting to draw a null landmark.");
        if (landmark.getLandmark() == null || landmark.getBoundingBox() == null) return;

        // Draws the bounding box around the landmarkblock.
        RectF rect = new RectF(landmark.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateX(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateX(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        // Renders the landmark at the bottom of the box.
        canvas.drawText(landmark.getLandmark(), rect.left, rect.bottom, landmarkPaint);
    }
}
