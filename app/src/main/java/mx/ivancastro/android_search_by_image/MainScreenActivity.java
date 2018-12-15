package mx.ivancastro.android_search_by_image;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mx.ivancastro.android_search_by_image.cloud.landmarkrecognition.CloudLandmarkRecognitionProcessor;
import mx.ivancastro.android_search_by_image.common.GraphicOverlay;
import mx.ivancastro.android_search_by_image.custommodel.CustomImageClassifierProcessor;

public class MainScreenActivity extends AppCompatActivity {
    private static final String TAG = "MainScreenActivity";

    private static final String CUSTOM_MODEL  = "Custom Model";
    private static final String GOOGLE_MODEL = "Google's Model";

    private static final String KEY_IMAGE_URI        = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_URI";
    private static final String KEY_IMAGE_MAX_WIDTH  = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_MAX_WIDTH";
    private static final String KEY_IMAGE_MAX_HEIGHT = "mx.ivancastrotest.firebase.ml.KEY_IMAGE_MAX_HEIGHT";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE  = 1002;

    private static final int PERMISSION_REQUESTS = 1;

    private ImageView preview;
    private GraphicOverlay graphicOverlay;

    boolean isLandscape;

    private Uri imageUri;
    // Max width (portrait mode)
    private Integer imageMaxWidth;
    // Max height (portrait mode)
    private Integer imageMaxHeight;

    private CloudLandmarkRecognitionProcessor imageProcessorGoogle;
    private CustomImageClassifierProcessor imageProcessorCustom;

    private String selectedMode = CUSTOM_MODEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if (!allPermissionsGranted()) getRuntimePermissions();
        // imageProcessor = new CustomImageClassifierProcessor();
        populateModelSelector();

        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(v -> {
            if (!checkInternetConnection() && selectedMode.equals(GOOGLE_MODEL)) return;
            startCameraIntentForResult();
        });

        FloatingActionButton fabGallery = findViewById(R.id.fabGallery);
        fabGallery.setOnClickListener(v -> {
            if (!checkInternetConnection() && selectedMode.equals(GOOGLE_MODEL)) return;
            startChooseImageFromResult();
        });

        preview = findViewById(R.id.previewPane);
        if (preview ==  null) Log.d(TAG, "Preview is null!");

        graphicOverlay = findViewById(R.id.previewOverlay);
        if (graphicOverlay == null) Log.d(TAG, "graphicChange aspect ratioOverlay is null");

        isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri       = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            imageMaxWidth  = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH);
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT);

            if (imageUri != null) tryReloadAndDetectImage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        new MenuInflater(this).inflate(R.menu.action_button_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void populateModelSelector () {
        Spinner modelSpinner = findViewById(R.id.modelSelector);
        List<String> options = new ArrayList<>();
        options.add(CUSTOM_MODEL);
        options.add(GOOGLE_MODEL);
        // Creating the adapter for modelSelector
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        modelSpinner.setAdapter(dataAdapter);
        modelSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedMode = parent.getItemAtPosition(position).toString();
                        createImageProcessor();
                        tryReloadAndDetectImage();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
    }

    private void createImageProcessor () {
        switch (selectedMode) {
            case CUSTOM_MODEL:
                imageProcessorCustom = new CustomImageClassifierProcessor(this);
                break;
            case GOOGLE_MODEL:
                imageProcessorGoogle = new CloudLandmarkRecognitionProcessor();
                break;
                default:
                    throw new IllegalStateException("Unknown selectedMode: " + selectedMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Since the custom model doesn't return the coordinates of the landmark and there is no a
        // Wikipedia article for some of the landmarks in the model we return.
        if (selectedMode.equals(CUSTOM_MODEL)) return false;

        // Check if had found a landmark in the image
        if (!imageProcessorGoogle.hasDetected()) {
            Toast.makeText(this,"No se encontró nada en la imagen.", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check internet connection
        if (!checkInternetConnection()) return false;

        Intent intent;
        switch (item.getItemId()) {
            case R.id.web_search:
                intent = new Intent(this, InfoActivity.class);
                intent.putExtra("landmarkName", imageProcessorGoogle.getLandmarkName());
                startActivity(intent);
                return true;
            case R.id.show_location:
                intent = new Intent(this, LocationActivity.class);
                List<FirebaseVisionLatLng> locations = imageProcessorGoogle.getLocations();
                // Since multiple locations are possible we only take the first one.
                FirebaseVisionLatLng location = locations.get(0);
                intent.putExtra("latitude",  location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                intent.putExtra("landmarkName", imageProcessorGoogle.getLandmarkName());
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        if (imageMaxWidth != null) outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth);
        if (imageMaxHeight != null) outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight);
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
            values.put(MediaStore.Images.Media.TITLE, "new photo");
            values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        if (imageUri == null || imageUri.getPath().equals("")) preview.setImageResource(R.drawable.main_bg);
    }

    private void startChooseImageFromResult () {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), REQUEST_CHOOSE_IMAGE);
    }

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
            if (selectedMode.equals(CUSTOM_MODEL)) imageProcessorCustom.process(resizedBitmap, graphicOverlay);
            if (selectedMode.equals(GOOGLE_MODEL)) imageProcessorGoogle.process(resizedBitmap, graphicOverlay);
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
        }
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight () {
        int maxWidthForPortraitMode  = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        int targetWidth  = isLandscape ? maxHeightForPortraitMode : maxWidthForPortraitMode;
        int targetHeight = isLandscape ? maxWidthForPortraitMode  : maxHeightForPortraitMode;

        return new Pair<>(targetWidth, targetHeight);
    }

    // returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth () {
        if (imageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait
            // for a UI layout pass to get the right values. So delay it to first time image rendering time.
            if (isLandscape) imageMaxWidth =
                    ((View) preview.getParent()).getHeight(); //- findViewById(R.id.controlPanel).getHeight();
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
            else imageMaxHeight = ((View) preview.getParent()).getHeight(); //-
                    //findViewById(R.id.controlPanel).getHeight();
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

    /** Returns true if there is internet connection.*/
    private  boolean checkInternetConnection () {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.isAvailable() && networkInfo.isConnected();
        } else {
            Toast.makeText(this, "No tienes conexión a Internet.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
