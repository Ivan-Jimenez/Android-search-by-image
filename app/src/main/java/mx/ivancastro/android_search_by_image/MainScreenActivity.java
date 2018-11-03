package mx.ivancastro.android_search_by_image;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mx.ivancastro.android_search_by_image.cloud.landmarkrecognition.CloudLandmarkRecognitionProcessor;
import mx.ivancastro.android_search_by_image.common.GraphicOverlay;

/**
 * Activity for testing feature detector for labeling
 */
public class MainScreenActivity extends AppCompatActivity {
    private static final String TAG = "MainScreenActivity";

    // TODO: fix the android:layout_toEndOf="@id/firePreview" if something wrong

    // FIXME: Images and labels loads out  of focus. Change aspect ratio

    // TODO: Translate labels to spanish. Change text color.

    // TODO: Implement permissions in execution time.

    // TODO: Change size off the options menu

    // TODO: Implement notification for the user when no landmarks found.

    private static final String CLOUD_LABEL_DETECTION    = "Cloud Label";
    private static final String CLOUD_LANDMARK_DETECTION = "Cloud Landmark";

    private static final String SIZE_PREVIEW  = "w:max"; // Available on-screen width.
    private static final String SIZE_1024_768 = "w.1024"; // 1024 * 768 in a normal ratio
    private static final String SIZE_640_480  = "w:640"; // 640 * 480 in a normal ratio

    // TODO: Check what is happening with this.
    private static final String KEY_IMAGE_URI        = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_URI";
    private static final String KEY_IMAGE_MAX_WIDTH  = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_MAX_WIDTH";
    private static final String KEY_IMAGE_MAX_HEIGHT = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_MAX_HEIGHT";
    private static final String KEY_SELECTED_SIZE    = "mx.ivancastrotest.firebase.ml.KEY_SELECTED_SIZE";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE  = 1002;

    private static final int PERMISSION_REQUESTS = 1;

    private Button getImageButton;
    private Button getActionButton;
    private ImageView preview;
    private GraphicOverlay graphicOverlay;
    private String selectedMode = CLOUD_LABEL_DETECTION;
    private String selectedSize = SIZE_PREVIEW;

    boolean isLandscape;

    private Uri imageUri;
    // Max width (portrait mode)
    private Integer imageMaxWidth;
    // Max height (portrait mode)
    private Integer imageMaxHeight;
    private Bitmap bitmapForDetection;
    //private VisionImageProcessor imageProcessor;
    private CloudLandmarkRecognitionProcessor imageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if (!allPermissionsGranted()) getRuntimePermissions();
        imageProcessor = new CloudLandmarkRecognitionProcessor();

        getImageButton = findViewById(R.id.getImageButton);
        getImageButton.setOnClickListener((v) -> {
            // Menu for selecting either: a) take a new photo b) select one from existing
            PopupMenu popupMenu = new PopupMenu(MainScreenActivity.this, v);
            popupMenu.setOnMenuItemClickListener((item) -> {
                switch (item.getItemId()) {
                    case R.id.select_images_from_local:
                        startChooseImageFromResult();
                        return true;
                    case R.id.take_photo_using_camera:
                        startCameraIntentForResult();
                        return true;
                        default:
                            return false;
                }
            });
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.camera_button_menu, popupMenu.getMenu());
            popupMenu.show();
        });
        getActionButton = findViewById(R.id.getActionButton);
        getActionButton.setOnClickListener((v) -> {
            // Menu for selecting for the user to select what to do
            PopupMenu popupMenu = new PopupMenu(MainScreenActivity.this, v);
            popupMenu.setOnMenuItemClickListener((item) -> {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.web_search:
                        intent = new Intent(this, InfoActivity.class);
                        intent.putExtra("landmarkName", imageProcessor.getLandmarkName());
                        startActivity(intent);
                        return true;
                    case R.id.show_location:
                        intent = new Intent(this, LocationActivity.class);
                        List<FirebaseVisionLatLng> locations = imageProcessor.getLocations();
                        // Since multiple locations are possible we only take the first one.
                        FirebaseVisionLatLng location = locations.get(0);
                        intent.putExtra("latitude",  location.getLatitude());
                        intent.putExtra("longitude", location.getLongitude());
                        intent.putExtra("landmarkName", imageProcessor.getLandmarkName());
                        startActivity(intent);
                        return true;
                    case R.id.similar_images:
                        intent = new Intent(this, SimilarImagesActivity.class);
                        startActivity(intent);
                        return true;
                        default:
                            return false;
                }
            });
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.action_button_menu, popupMenu.getMenu());
            popupMenu.show();
        });
        preview = findViewById(R.id.previewPane);
        if (preview ==  null) Log.d(TAG, "Preview is null!");

        graphicOverlay = findViewById(R.id.previewOverlay);
        if (graphicOverlay == null) Log.d(TAG, "graphicChange aspect ratioOverlay is null");

        //populateFeatureSelector();
        //populateSizeSelector();

        isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri       = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            imageMaxWidth  = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH);
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT);
            selectedSize   = savedInstanceState.getString(KEY_SELECTED_SIZE);

            if (imageUri != null) tryReloadAndDetectImage();
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        if (imageMaxWidth != null) outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth);
        if (imageMaxHeight != null) outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectImage();
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it,
            imageUri = data.getData();
            tryReloadAndDetectImage();
        }
    }

    private void startCameraIntentForResult () {
        // Clean up last time's image
        imageUri = null;
        preview.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Nueva Foto");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la Camara");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startChooseImageFromResult () {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), REQUEST_CHOOSE_IMAGE);
    }

    /*
    private void populateFeatureSelector () {
        Spinner featureSpinner = findViewById(R.id.featureSelector);
        List<String> options = new ArrayList<>();
        options.add(CLOUD_LABEL_DETECTION); // For now
        options.add(CLOUD_LANDMARK_DETECTION);

        // Creating the adapter for featureSpiner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Dropdown layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Attaching data adapter to spinner
        featureSpinner.setAdapter(dataAdapter);
        featureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = parent.getItemAtPosition(position).toString();
                //imageProcessor = new CloudImageLabelingProcessor();
                //createImageProcessor();
                imageProcessor = new CloudLandmarkRecognitionProcessor();
                tryReloadAndDetectImage();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    */

    /*
    private void createImageProcessor() {
        switch (selectedMode) {
            case CLOUD_LABEL_DETECTION:
                imageProcessor = new CloudImageLabelingProcessor();
                break;
            case CLOUD_LANDMARK_DETECTION:
                imageProcessor = new CloudLandmarkRecognitionProcessor();
                break;
        }
    }*/

    /*
    private void populateSizeSelector () {
        Spinner sizeSpinner = findViewById(R.id.sizeSelector);
        List<String> options = new ArrayList<>();
        options.add(SIZE_PREVIEW);
        options.add(SIZE_1024_768);
        options.add(SIZE_640_480);

        // Creating adapter for featureSpinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Dropdown layout style -list view width radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Attaching data adapter to spinner
        sizeSpinner.setAdapter(dataAdapter);
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSize = parent.getItemAtPosition(position).toString();
                tryReloadAndDetectImage();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }*/

    private void tryReloadAndDetectImage () {
        try {
            if (imageUri ==  null) return;

            // Clear the overlay first
            graphicOverlay.clear();

            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Get the dimensions of the view
            Pair<Integer, Integer> targetedsize = getTargetedWidthHeight();

            int targetWidth  = targetedsize.first;
            int targetHeight = targetedsize.second;

            // Determine how much to scale down the image
            float scaleFactor = Math.max(
                    (float) imageBitmap.getWidth()  / (float) targetWidth,
                    (float) imageBitmap.getHeight() / (float) targetHeight);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap (
                    imageBitmap,
                    (int) (imageBitmap.getWidth()  / scaleFactor),
                    (int) (imageBitmap.getHeight() / scaleFactor),
                    true);

            preview.setImageBitmap(resizedBitmap);
            bitmapForDetection = resizedBitmap;

            imageProcessor.process(bitmapForDetection, graphicOverlay);
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
        }
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight () {
        int targetWidth  = 0;
        int targetHeight = 0;

        switch (selectedSize) {
            case SIZE_PREVIEW:
                int maxWidthForPortraitMode  = getImageMaxWidth();
                int maxHeightForPortraitMode = getImageMaxHeight();
                targetWidth  = isLandscape ? maxHeightForPortraitMode : maxWidthForPortraitMode;
                targetHeight = isLandscape ? maxWidthForPortraitMode  : maxHeightForPortraitMode;
                break;
            case SIZE_640_480:
                targetWidth =  isLandscape ? 640 : 480;
                targetHeight = isLandscape ? 480 : 640;
                break;
            case SIZE_1024_768:
                targetWidth  = isLandscape ? 1024 : 768;
                targetHeight = isLandscape ? 768  : 1024;
                break;
        }
        return new Pair<>(targetWidth, targetHeight);
    }

    // returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth () {
        if (imageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait
            // for a UI layout pass to get the right values. So delay it to first time image rendering time.
            if (isLandscape) imageMaxWidth =
                    ((View) preview.getParent()).getHeight() - findViewById(R.id.controlPanel).getHeight();
            else imageMaxWidth = ((View) preview.getParent()).getWidth();
        }
        return imageMaxWidth;
    }

    // returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight () {
        if (imageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait
            // for a UI layout pass to get the right values. So delay it to first time image rendering time.
            if (isLandscape) imageMaxHeight = ((View) preview.getParent()).getWidth();
            else imageMaxHeight = ((View) preview.getParent()).getHeight() -
                    findViewById(R.id.controlPanel).getHeight();
        }
        return imageMaxHeight;
    }

    private String[] getRequiredPermissions () {
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = info.requestedPermissions;
            if (permissions != null && permissions.length > 0) return permissions;
            else return new String[0];
        } catch (Exception e) { return new String[0]; }
    }

    private boolean allPermissionsGranted () {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) return false;
        }
        return true;
    }

    private void getRuntimePermissions () {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) allNeededPermissions.add(permission);
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted (Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
