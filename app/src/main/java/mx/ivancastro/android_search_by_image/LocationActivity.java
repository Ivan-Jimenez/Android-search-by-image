package mx.ivancastro.android_search_by_image;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.util.List;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        TextView txtViewLocations = findViewById(R.id.txtViewLocations);
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        txtViewLocations.setText("latitude:" + latitude + " longitude:" + longitude);
    }
}
