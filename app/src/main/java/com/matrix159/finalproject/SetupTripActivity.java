package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Use this activity to map a list of items to a location?
 */
public class SetupTripActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.location_spinner)
    Spinner locationSpinner;
    @BindView(R.id.item_spinner)
    Spinner itemSpinner;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_trip);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up the item spinner Probably just gonna crash
        ArrayList<String> itemLists = getIntent().getExtras().getStringArrayList("ItemLists");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemLists);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(dataAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // TODO: Get the currently selected spinner items, send the text back (the key in the hashmap), make the main intent display the list of items that is in the hashmap
                //intent.putExtra("LocationKey", locationSpinner.getSelectedItem().toString());
                intent.putExtra("ItemListKey", itemSpinner.getSelectedItem().toString());
                setResult(MainActivity.SELECT_ITEMS_AND_TRIP, intent);
                finish();
            }
        });
    }

}
