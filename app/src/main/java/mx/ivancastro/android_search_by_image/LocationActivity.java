package mx.ivancastro.android_search_by_image;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;

public class LocationActivity extends FragmentActivity implements
        OnMapReadyCallback {

    private static final String TAG = "LocationActivity";

    // TODO: Show information about the landmark in the map

    // TODO: Implement street view maybe??

    // TODO: Implement how to get there

    // TODO:  Smoothly zoom on the marker

    // TODO: Buttons for zoom in and zoom out

    // TODO: Implement panoramic view

    /**
     * The amount by which the scroll camera. This is given in pixels no dp
     */
    private static final int SCROLL_BY_PX = 100;

    // Landmark location
    private static double latitude;
    private static double longitude;
    private String landmarkName;
    private static CameraPosition LANDMARK;

    private boolean isCancel = false;

    private GoogleMap mMap;


    private Button zoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Gets the location from the MainScreen
        Intent intent = getIntent();
        latitude     = intent.getDoubleExtra("latitude", 0);
        longitude    = intent.getDoubleExtra("longitude", 0);
        landmarkName = intent.getStringExtra("landmarkName");

        LANDMARK = new CameraPosition.Builder().target(new LatLng(latitude, longitude))
                        .zoom(15.5f)
                        .bearing(0)
                        .tilt(25)
                        .build();

        zoomButton = findViewById(R.id.zoomButton);
        zoomButton.setOnClickListener( v -> onGoToLandmark());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(MAP_TYPE_SATELLITE);

        LatLng landmark = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(landmark).title(landmarkName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(landmark));
    }

    private void onGoToLandmark () {
        if (!checkReady()) return;
        changeCamera(CameraUpdateFactory.newCameraPosition(LANDMARK));
    }

    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     * @return
     */
    private boolean checkReady () {
        if (mMap == null) {
            Toast.makeText(this, "El mapa  aun no esta listo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    private void changeCamera (CameraUpdate update) { changeCamera(update, null); }

    /**
     * Change the camera position
     * @param update
     * @param callback
     */
    private void changeCamera (CameraUpdate update, GoogleMap.CancelableCallback callback) {
        mMap.animateCamera(update, 100, callback);
    }
}
