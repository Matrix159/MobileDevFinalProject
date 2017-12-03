package com.matrix159.finalproject;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnCompleteListener<Void> {

    public final static String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST = 1;
    private static final int LOCATION_REQUEST = 2;
    static final int MAKE_ITEM_LIST = 3;
    private GoogleMap mMap;
    private FirebaseAuth auth;
    private DatabaseReference topRef;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList;
    private FusedLocationProviderClient locationClient;
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }
    /** Used when requesting to add or remove geofences */
    private PendingIntent mGeofencePendingIntent;
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;
    private Geofence currentGeofence;
    private ArrayList<String> items = new ArrayList<>();
    double lat;
    double lng;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView itemsRecycler;
    LinearLayoutManager layoutManager;
    ItemAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        items.add("Hello");
        items.add("Hello");
        items.add("Hello");
        items.add("Hello");
        items.add("Hello");

        //Set up the recycler view
        layoutManager = new LinearLayoutManager(this);
        itemsRecycler.setLayoutManager(layoutManager);
        myAdapter = new ItemAdapter(items);
        itemsRecycler.setAdapter(myAdapter);

        geofenceList = new ArrayList<>();
        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        auth = FirebaseAuth.getInstance();
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        topRef = database.getReference(auth.getUid());
        // Read from the database
        topRef.addValueEventListener(new ValueEventListener() {
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


        locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            }
            return;
        }

        geofencingClient = LocationServices.getGeofencingClient(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @OnClick(R.id.editItems)
    public void editItems() {
        Intent intent = new Intent(MainActivity.this, AddItemsActivity.class);
        startActivityForResult(intent, MAKE_ITEM_LIST);
    }

    @OnClick(R.id.main_add_location)
    public void mainAddLocation() {
        Intent intent = new Intent(this, AddLocationActivity.class);
        startActivityForResult(intent, LOCATION_REQUEST);
    }

    @OnClick(R.id.build_location_button)
    public void buildLocation() {
        Intent intent = new Intent(this, SetupTripActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Returned from making list of items
        if (resultCode == MAKE_ITEM_LIST){
            items = data.getStringArrayListExtra("RecyclerItems");
            //myAdapter.notifyDataSetChanged();
            myAdapter = new ItemAdapter(items);
            itemsRecycler.setAdapter(myAdapter);
        }

        if (requestCode == LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {

                Bundle bundle = data.getExtras();
                try{
                    double lat = Double.parseDouble(bundle.getString("LATITUDE"));
                    double lng = Double.parseDouble(bundle.getString("LONGITUDE"));
                    LatLng marker = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(marker).title(currentGeofence.getRequestId()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                    CircleOptions circleOptions = new CircleOptions()
                            .center(marker)
                            .radius(10)
                            .fillColor(Color.GREEN)
                            .strokeColor(Color.TRANSPARENT)
                            .strokeWidth(2);
                    mMap.addCircle(circleOptions);
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    geofencingClient = LocationServices.getGeofencingClient(this);
                    locationClient.getLastLocation().addOnSuccessListener(this, location ->
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
        locationClient.getLastLocation().addOnSuccessListener(this, location ->
        {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                geofenceList.add(getGeofence("locationName", lat, lng, 5));
                currentGeofence = geofenceList.get(0);
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
                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("lat", lat);
                userUpdates.put("lng", lng);
                topRef.child("locationName").updateChildren(userUpdates);
            }
        });

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

    @Override
    public void onComplete(@NonNull Task<Void> task) {

    }
}
