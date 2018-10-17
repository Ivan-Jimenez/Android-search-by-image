package mx.ivancastro.android_search_by_image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final String TAG = Class.class.getName();

    private Button btnSnap;
    private Button btnText;
    private Button btnLabels;
    private ImageView imageView;
    private TextView txtView;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSnap = findViewById(R.id.snapBtn);
        btnText = findViewById(R.id.btnText);
        imageView = findViewById(R.id.imageView);
        txtView = findViewById(R.id.txtView);
        btnLabels = findViewById(R.id.btnLabels);

        btnSnap.setOnClickListener((v) -> dispatchTakePictureIntent());
        btnText.setOnClickListener((v) -> getText());
        btnLabels.setOnClickListener((v) -> detectLandmarks());
    }

    // Landmakrs
    private void detectLandmarks () {
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(15)
                .build();

        // Create FirebaseVisionImage from a bitmap object
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        // Get an instance of FirebaseVisionCloudLandmarkDetector
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector(options); // replace the default options for ours

        // Pass the image in the detectInImage method
        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
                    // task completed successfully
                    // get information about landmark
                    for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {
                        Rect bounds = landmark.getBoundingBox();
                        String landmarkName = landmark.getLandmark();
                        String landmarkId = landmark.getEntityId();
                        float confidence = landmark.getConfidence();

                        // put in the TextView
                        Toast.makeText(this, "place: " + landmarkName + confidence, Toast.LENGTH_SHORT).show();

                        // Multiple locations are possible, e.g., the location of the depicted
                        // landmark and the location the picture was taken.
                        for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                            double latitude = loc.getLatitude();
                            double longitude = loc.getLongitude();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // task completed with an exception
                    Toast.makeText(this, "Error trying to find Landmarks", Toast.LENGTH_SHORT).show();
                });
    }

    private void getLabels () {
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(15)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLabelDetector(options);
        // Or, to change the default settings:
        // FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
        //         .getVisionCloudLabelDetector(options);

        detectLabels(detector, image);
    }

    private void detectLabels (FirebaseVisionCloudLabelDetector detector,FirebaseVisionImage image) {
        Task<List<FirebaseVisionCloudLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                labels -> {
                                    // Get information from label objects
                                    txtView.setText("");
                                    for (FirebaseVisionCloudLabel label: labels) {
                                        String text = label.getLabel();
                                        txtView.setText(txtView.getText() + " " + text);
                                        String entityId = label.getEntityId();
                                        float confidence = label.getConfidence();

                                    }
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    // ...
                                });
    }

    /**
     * =============================================================================================
     * ================================> TAKE PHOTO <===============================================
     * */
    private void dispatchTakePictureIntent () {
        Intent takePictureInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureInt.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureInt, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        Bundle extras = data.getExtras();
        imageBitmap = (Bitmap) extras.get("data");
        imageView.setImageBitmap(imageBitmap);
    }


    /**
     * ============================================================================================
     * ===================> TEXT DETECTION <=======================================================
     * */
    private void getText () {
        //FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
        //        .getCloudTextRecognizer();

        // Or, to provide language hints to assist with language detection:
        // See https://cloud.google.com/vision/docs/languages for supported languages
        FirebaseVisionCloudTextRecognizerOptions options =
                new FirebaseVisionCloudTextRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("en", "es"))
                        .build();

        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getCloudTextRecognizer(options);


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        //FirebaseVisionTextRecognizer txtDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(firebaseVisionText ->
                processTxt(firebaseVisionText)).addOnFailureListener(e -> {

        });
    }

    private void processTxt (FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No se encontró texto :(", Toast.LENGTH_LONG).show();
            return;
        }
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            String txt = block.getText();
            //txtView.setTextSize(24);
            txtView.setText(txt);
        }
    }
}
