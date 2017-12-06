package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.matrix159.finalproject.models.Location;

import java.util.ArrayList;
import java.util.List;

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
    @BindView(R.id.location_name_edit)
    EditText locationName;
    @BindView(R.id.location_latitude)
    EditText latitude;
    @BindView(R.id.location_longitude)
    EditText longitude;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edit_location_recycler)
    RecyclerView recyclerView;

    private List<Location> locations;
    private FirebaseAuth auth;
    private DatabaseReference topRef;
    private FusedLocationProviderClient mFusedLocationClient;
    private RecyclerView.LayoutManager layoutManager;
    private LocationAdapter locationAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        topRef = database.getReference(auth.getUid() + "/locations");

        locations = new ArrayList<>();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            String name = locationName.getText().toString();
            double lat, lng;
            if(name.length() == 0) {
                Snackbar.make(locationName, "Please enter a location name", Snackbar.LENGTH_SHORT).show();
                return;
            }
            try {
                lat = Double.parseDouble(latitude.getText().toString());
                lng = Double.parseDouble(longitude.getText().toString());
            } catch(Exception ex) {
                Snackbar.make(locationName, "Please enter a valid latitude and longitude", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Location location = new Location(name, lat, lng);
            locations.add(location);
            locationAdapter.notifyDataSetChanged();
            topRef.setValue(locations);
            /*Intent intent = new Intent();
            intent.putExtra("LATITUDE", latitude.getText().toString());
            intent.putExtra("LONGITUDE", longitude.getText().toString());
            setResult(RESULT_OK, intent);
            finish();*/
        });
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        locationAdapter = new LocationAdapter(locations);
        recyclerView.setAdapter(locationAdapter);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Read from the database
        topRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Location>> t = new GenericTypeIndicator<List<Location>>() {};
                List<Location> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    locations.clear();
                    locations.addAll(snapshot);
                    locationAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
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
                locations.remove(viewHolder.getLayoutPosition());
                locationAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                topRef.setValue(locations);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
                locationName.setText(place.getName());
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
