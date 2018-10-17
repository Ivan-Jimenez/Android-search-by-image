package mx.ivancastro.android_search_by_image.cloud.imagelabeling;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.ArrayList;
import java.util.List;

import mx.ivancastro.android_search_by_image.VisionProcessorBase;
import mx.ivancastro.android_search_by_image.common.FrameMetaData;
import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/**
 * Cloud Label Detector
 */
public class CloudImageLabelingProcessor extends VisionProcessorBase<List<FirebaseVisionCloudLabel>> {
    private static final String TAG = "CloudLabelingProcessor";

    private final FirebaseVisionCloudLabelDetector detector;

    public CloudImageLabelingProcessor () {
        FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
                .setMaxResults(10)
                .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
                .build();
        detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
    }

    @Override
    protected Task<List<FirebaseVisionCloudLabel>> detectInImage (FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess (@NonNull List<FirebaseVisionCloudLabel> labels,
                              @NonNull FrameMetaData frameMetaData,
                              @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        Log.d(TAG, "cloud label size: " + labels.size());
        List<String> labelsString = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            FirebaseVisionCloudLabel label = labels.get(i);
            Log.d(TAG, "cloud label: " + label);
            if (label.getLabel() != null) {
                labelsString.add(label.getLabel());
            }
        }
        CloudLabelGraphic cloudLabelGraphic = new CloudLabelGraphic(graphicOverlay);
        graphicOverlay.add(cloudLabelGraphic);
        cloudLabelGraphic.updateLabel(labelsString);
    }

    @Override
    protected void onFailure (@NonNull Exception e) {
        Log.e(TAG, "Cloud Label Detection Failed! " + e);
    }
}
