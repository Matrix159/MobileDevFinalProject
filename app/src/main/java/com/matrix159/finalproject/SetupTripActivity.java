package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.matrix159.finalproject.adapters.ItemAdapter;
import com.matrix159.finalproject.adapters.SelectItemsAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.matrix159.finalproject.R.attr.layoutManager;

/*
 * Use this activity to map a list of items to a location?
 */
public class SetupTripActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.location_spinner)
    Spinner locationSpinner;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.build_trip_recycler)
    RecyclerView selectItemsRecycler;

    LinearLayoutManager layoutManager;
    SelectItemsAdapter myAdapter;
    ArrayList<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_trip);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        itemList = new ArrayList<>();
        itemList.add("yes");
        itemList.add("more items");
        itemList.add("Phone Charger");

        layoutManager = new LinearLayoutManager(this);
        selectItemsRecycler.setLayoutManager(layoutManager);
        myAdapter = new SelectItemsAdapter(itemList);
        selectItemsRecycler.setAdapter(myAdapter);

        // Set up the item spinner Probably just gonna crash
        //ArrayList<String> itemLists = getIntent().getExtras().getStringArrayList("ItemLists");
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemLists);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //itemSpinner.setAdapter(dataAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> returnList = new ArrayList<>();
                for (int i = 0; i < itemList.size(); i++){
                    if (myAdapter.getSelectedItems().get(i)){
                        returnList.add(itemList.get(i));
                    }
                }

                Intent intent = new Intent();
                intent.putStringArrayListExtra("SelectedItems", returnList);
                setResult(MainActivity.SELECT_ITEMS_AND_TRIP, intent);
                finish();
            }
        });
    }

}
