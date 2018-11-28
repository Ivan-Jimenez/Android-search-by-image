package mx.ivancastro.android_search_by_image.cloud.landmarkrecognition;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.util.List;

import mx.ivancastro.android_search_by_image.VisionProcessorBase;
import mx.ivancastro.android_search_by_image.common.FrameMetaData;
import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/**
 * Cloud Landmark Detector.
 */
public class CloudLandmarkRecognitionProcessor
        extends VisionProcessorBase<List<FirebaseVisionCloudLandmark>> {
    private static final String TAG = "LandmarkRecognitionProc";

    private final FirebaseVisionCloudLandmarkDetector detector;

    // Landmark location
    private List<FirebaseVisionLatLng> locations;

    private String landmarkName;
    private boolean detected;

    public CloudLandmarkRecognitionProcessor () {
        super();
        FirebaseVisionCloudDetectorOptions options =
            new FirebaseVisionCloudDetectorOptions.Builder()
                .setMaxResults(10)
                .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
                .build();

        detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options);
    }

    @Override
    protected Task<List<FirebaseVisionCloudLandmark>> detectInImage (FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess (@NonNull List<FirebaseVisionCloudLandmark> landmarks,
                              @NonNull FrameMetaData frameMetaData,
                              @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        Log.d(TAG, "cloud landmark size: " + landmarks.size());
        for (int i = 0; i < landmarks.size(); i++) {
            FirebaseVisionCloudLandmark landmark = landmarks.get(i);
            Log.d(TAG, "cloud landmark: " + landmark);

            landmarkName = landmark.getLandmark();

            locations = landmark.getLocations();

            CloudLandmarkGraphic cloudLandmarkGraphic = new CloudLandmarkGraphic(graphicOverlay);
            graphicOverlay.add(cloudLandmarkGraphic);
            cloudLandmarkGraphic.updateLandmark(landmark);

            detected = true;
        }
    }

    @Override
    protected void onFailure (@NonNull Exception e) {
        Log.e(TAG, "Cloud Landmak Detection Failed!" + e);
        detected = false;
    }

    /**
     * Multiple locations are possible, e.g., the location of the depicted
     * landmark an the location the picture was taken.
     * @return List object with the locations detected.
     */
    public List<FirebaseVisionLatLng> getLocations () { return locations; }

    /**
     * @return Landmark's name
     */
    public String getLandmarkName () { return landmarkName; }
    public boolean hasDetected () { return detected; }
}
