package mx.ivancastro.android_search_by_image.custommodel;

import android.app.Activity;
import android.graphics.Bitmap;

import java.io.IOException;

import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

public class CustomImageClassifierProcessor {
    private CustomImageClassifier classifier;
    private final Activity activity;

    public CustomImageClassifierProcessor (Activity activity) {
        this.activity = activity;
    }

    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
        try {
            classifier = new CustomImageClassifier(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Since the model just accept 224 by 224 images, we resize the image before pass it to the
        // classifier
        bitmap = Bitmap.createScaledBitmap(bitmap, CustomImageClassifier.DIM_IMG_SIZE_X,
                CustomImageClassifier.DIM_IMG_SIZE_Y, true);
        String label = classifier.classifyFrame(bitmap);
        LabelGraphic labelGraphic = new LabelGraphic(graphicOverlay);
        graphicOverlay.clear();
        graphicOverlay.add(labelGraphic);
        labelGraphic.updateLabel(label);
    }
}
