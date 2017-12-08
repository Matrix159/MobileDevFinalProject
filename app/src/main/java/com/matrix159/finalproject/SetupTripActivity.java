package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.matrix159.finalproject.adapters.SelectItemsAdapter;
import com.matrix159.finalproject.models.Item;
import com.matrix159.finalproject.models.Location;
import com.matrix159.finalproject.models.Trip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Use this activity to map a list of items to a location?
 */
public class SetupTripActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.location_spinner)
    Spinner locationSpinner;
    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;
    @BindView(R.id.fab_delete)
    FloatingActionButton fabDelete;
    @BindView(R.id.build_trip_recycler)
    RecyclerView selectItemsRecycler;

    private LinearLayoutManager layoutManager;
    private SelectItemsAdapter itemAdapter;
    private FirebaseAuth auth;
    private DatabaseReference locationsRef;
    private DatabaseReference tripsRef;
    private DatabaseReference itemsRef;
    private List<Location> locations;
    private List<Item> itemList;
    private List<Trip> trips;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_trip);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        locations = new ArrayList<>();
        itemList = new ArrayList<>();
        trips = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        selectItemsRecycler.setLayoutManager(layoutManager);
        itemAdapter = new SelectItemsAdapter(itemList);
        selectItemsRecycler.setAdapter(itemAdapter);

        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        locationsRef = database.getReference(auth.getUid() + "/locations");
        itemsRef = database.getReference(auth.getUid() + "/items");
        tripsRef = database.getReference(auth.getUid() + "/trips");


        ArrayAdapter<Location> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locationSpinner.setAdapter(spinnerAdapter);

        // Read from the database
        locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Location>> t = new GenericTypeIndicator<List<Location>>() {};
                List<Location> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    locations.clear();
                    locations.addAll(snapshot);
                    ArrayAdapter<Location> spinnerAdapter = new ArrayAdapter<Location>(SetupTripActivity.this, android.R.layout.simple_spinner_item, locations);
                    locationSpinner.setAdapter(spinnerAdapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Item>> t = new GenericTypeIndicator<List<Item>>() {};
                List<Item> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    itemList.clear();
                    itemList.addAll(snapshot);
                    itemAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Trip>> t = new GenericTypeIndicator<List<Trip>>() {};
                List<Trip> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    trips.clear();
                    trips.addAll(snapshot);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });



        // Set up the item spinner Probably just gonna crash
        //ArrayList<String> itemLists = getIntent().getExtras().getStringArrayList("ItemLists");
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemLists);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //itemSpinner.setAdapter(dataAdapter);

        fabAdd.setOnClickListener(view -> {
            List<Item> saveItemList = new ArrayList<>();
            //ArrayList<Item> returnList = new ArrayList<>();
            for (int i = 0; i < itemList.size(); i++){
                if (itemAdapter.getSelectedItems().get(i)){
                    //returnList.add(itemList.get(i));
                    saveItemList.add(new Item(itemList.get(i).getItemName(), true));
                }
            }
            boolean updated = false;
            Location location = locations.get(locationSpinner.getSelectedItemPosition());
            for (final ListIterator<Trip> i = trips.listIterator(); i.hasNext();) {
                final Trip trip = i.next();
                if(trip.getLocation().getLocationName().equalsIgnoreCase(location.getLocationName())) {
                    Trip updatedTrip = new Trip(trip.getLocation(), saveItemList);
                    i.set(updatedTrip);
                    updated = true;
                    break;
                }
            }

            if(!updated) {
                Trip trip = new Trip(location, saveItemList);
                trips.add(trip);
                tripsRef.setValue(trips);
                Snackbar.make(locationSpinner, "Added trip", Snackbar.LENGTH_SHORT).show();
            } else {
                tripsRef.setValue(trips);
                Snackbar.make(locationSpinner, "Updated trip", Snackbar.LENGTH_SHORT).show();
            }

            /*Intent intent = new Intent();
            intent.putStringArrayListExtra("SelectedItems", returnList);
            setResult(MainActivity.SELECT_ITEMS_AND_TRIP, intent);
            finish();*/
        });

        fabDelete.setOnClickListener(v -> {
            Location location = locations.get(locationSpinner.getSelectedItemPosition());
            Iterator<Trip> iterator = trips.iterator();
            while(iterator.hasNext()) {
                Trip trip = iterator.next();
                if(trip.getLocation().getLocationName().equalsIgnoreCase(location.getLocationName())) {
                    Snackbar.make(locationSpinner, "Deleted trip", Snackbar.LENGTH_SHORT).show();
                    iterator.remove();
                    break;
                }
            }
            tripsRef.setValue(trips);
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        // Change the selection of items based on location here
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
