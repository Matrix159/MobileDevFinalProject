package com.matrix159.finalproject;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** We will use this to utilize google places and search for a place or have a button to use current
 * location and then pass this information back to the calling activity.
 */
public class AddLocationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 1;
    public final static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public final static String TAG = "AddLocationActivity";
    @BindView(R.id.location_latitude)
    EditText latitude;
    @BindView(R.id.location_longitude)
    EditText longitude;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("LATITUDE", latitude.getText().toString());
            intent.putExtra("LONGITUDE", longitude.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @OnClick(R.id.search_location_button)
    public void searchLocationClicked() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.use_location_button)
    @SuppressWarnings({"MissingPermission"})
    public void useLocationClicked() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location ->
        {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                latitude.setText(String.valueOf(place.getLatLng().latitude));
                longitude.setText(String.valueOf(place.getLatLng().longitude));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
