package com.matrix159.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public final static String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST = 1;
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private DatabaseReference topRef;
    private GeofencingClient mGeofencingClient;
    private List<Geofence> geofenceList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationClient;
    private Geofence currentGeofence;
    double lat;
    double lng;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Button editItemsButton = this.findViewById(R.id.editItems);
        editItemsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemsActivity.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(mAuth.getUid());


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                //Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            }
            return;
        }

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location ->
        {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                geofenceList.add(getGeofence("locationName", lat, lng, 5));
                currentGeofence = geofenceList.get(0);
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("lat", lat);
                userUpdates.put("lng", lng);
                myRef.child("locationName").updateChildren(userUpdates);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mGeofencingClient = LocationServices.getGeofencingClient(this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location ->
                    {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            geofenceList.add(getGeofence("locationName", lat, lng, 5));
                            currentGeofence = geofenceList.get(0);
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(this);
                        }
                    });

                }
            }
        }
    }

    private static Geofence getGeofence(String indentifier, double lat, double lng, float radius) {
        return new Geofence.Builder()
                .setRequestId(indentifier)

                .setCircularRegion(
                        lat,
                        lng,
                        radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng marker = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(marker).title(currentGeofence.getRequestId()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        CircleOptions circleOptions = new CircleOptions()
                .center(marker)
                .radius(5)
                .fillColor(Color.GREEN)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        mMap.addCircle(circleOptions);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }


    // Life cycle methods below
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
