package com.matrix159.finalproject;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.matrix159.finalproject.adapters.ItemAdapter;
import com.matrix159.finalproject.models.Item;
import com.matrix159.finalproject.models.Location;
import com.matrix159.finalproject.models.Trip;
import com.matrix159.finalproject.services.Constants;
import com.matrix159.finalproject.services.GeofenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnCompleteListener<Void>, AdapterView.OnItemSelectedListener {

    public final static String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int LOCATION_REQUEST = 2;
    static final int MAKE_ITEM_LIST = 3;
    static final int SELECT_ITEMS_AND_TRIP = 4;
    private GoogleMap mMap;
    private FirebaseAuth auth;
    private DatabaseReference tripsRef;
    private FirebaseAnalytics analytics;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList;
    private FusedLocationProviderClient locationClient;

    /** Used when requesting to add or remove geofences */
    private PendingIntent geofencePendingIntent;
    private Geofence currentGeofence;
    private List<Item> items;
    double lat;
    double lng;
    public HashMap<String, ArrayList<String>> itemListMapping = new HashMap<>();
    List<Trip> trips;
    Trip activeTrip;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView itemsRecycler;
    @BindView(R.id.main_spinner)
    Spinner spinner;
    LinearLayoutManager layoutManager;
    ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        trips = new ArrayList<>();
        items = new ArrayList<>();
        //Set up the recycler view
        layoutManager = new LinearLayoutManager(this);
        itemsRecycler.setLayoutManager(layoutManager);
        itemAdapter = new ItemAdapter(items);
        itemsRecycler.setAdapter(itemAdapter);
        // Allows the items in the recycler view to be swiped away
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                //items.remove(viewHolder.getLayoutPosition());
                //itemAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                //tr.setValue(itemList);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(itemsRecycler);
        spinner.setOnItemSelectedListener(this);
        // Setting up geofence crap
        geofenceList = new ArrayList<>();
        geofencePendingIntent = null;
        geofencingClient = LocationServices.getGeofencingClient(this);


        auth = FirebaseAuth.getInstance();
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tripsRef = database.getReference(auth.getUid() + "/trips");
        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Trip>> t = new GenericTypeIndicator<List<Trip>>() {};
                List<Trip> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    trips.clear();
                    trips.addAll(snapshot);
                }
                ArrayAdapter<Trip> spinnerAdapter = new ArrayAdapter<Trip>(MainActivity.this, android.R.layout.simple_spinner_item, trips);
                spinner.setAdapter(spinnerAdapter);
                if(trips.size() > 0) {
                    items.clear();
                    Trip trip = trips.get(0);
                    Location location = trip.getLocation();
                    items.addAll(trip.getItems());
                    itemAdapter.notifyDataSetChanged();
                    geofenceList.clear();
                    geofenceList.add(getGeofence(location.getLocationName(), location.getLatitude(), location.getLongitude(), Constants.GEOFENCE_RADIUS_IN_METERS));
                    removeGeofences();
                    addGeofences();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        analytics = FirebaseAnalytics.getInstance(this);

        // TODO: Gotta make sure we have permissions before doing location stuff.
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            }
            return;
        }*/

        if (!checkPermissions()) {
            requestPermissions();
            return;
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Life cycle methods below
    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } /*else {
            removeGeofences();
            addGeofences();
        }*/
    }

    @OnClick(R.id.editItems)
    public void editItems() {
        Intent intent = new Intent(MainActivity.this, AddItemsActivity.class);
        //intent.putStringArrayListExtra("ItemList", listOfItems);
        startActivity(intent);
    }

    @OnClick(R.id.main_add_location)
    public void mainAddLocation() {
        Intent intent = new Intent(this, AddLocationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.build_location_button)
    public void buildLocation() {
        Intent intent = new Intent(this, SetupTripActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    @Override
    @SuppressWarnings("MissingPermission")
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //geofencingClient = LocationServices.getGeofencingClient(this);
                    locationClient.getLastLocation().addOnSuccessListener(this, location ->
                    {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            //geofenceList.add(getGeofence("locationName", lat, lng, 5));
                            //currentGeofence = geofenceList.get(0);
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(this);
                        }
                    });
                } else {
                        // Permission denied.
                        // Notify the user via a SnackBar that they have rejected a core permission for the
                        // app, which makes the Activity useless. In a real app, core permissions would
                        // typically be best requested during a welcome-screen flow.

                        // Additionally, it is important to remember that a permission might have been
                        // rejected without asking the user for permission (device policy or "Never ask
                        // again" prompts). Therefore, a user interface affordance is typically implemented
                        // when permissions are denied. Otherwise, your app could appear unresponsive to
                        // touches or interactions which have required permissions.
                        showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                                view -> {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                });
                    }
            }
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
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
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
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
    @SuppressWarnings("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationClient.getLastLocation().addOnSuccessListener(this, location ->
        {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                //geofenceList.add(getGeofence("locationName", lat, lng, 5));
                //currentGeofence = geofenceList.get(0);
                LatLng marker = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(marker).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                CircleOptions circleOptions = new CircleOptions()
                        .center(marker)
                        .radius(5)
                        .fillColor(Color.GREEN)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2);
                mMap.addCircle(circleOptions);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });

    }
    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.i(TAG, "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
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

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // Set to 0 so it doesn't intially trigger, we only care about exit events.
        builder.setInitialTrigger(0);
        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(geofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }


    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }
        geofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {

    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    private void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {

            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Trip trip = trips.get(position);
        List<Item> wantedItems = trip.getItems();
        Location location = trip.getLocation();

        items.clear();
        items.addAll(wantedItems);
        itemAdapter.notifyDataSetChanged();
        geofenceList.clear();
        geofenceList.add(getGeofence(location.getLocationName(), location.getLatitude(), location.getLongitude(), Constants.GEOFENCE_RADIUS_IN_METERS));
        removeGeofences();
        addGeofences();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }
}
