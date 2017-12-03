package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * We will use this class to add items to our "global" item list.
 */
public class AddItemsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.fab2)
    FloatingActionButton fab2;
    @BindView(R.id.add_items_edit_text)
    EditText addItemEdit;
    public ArrayList<String> items = new ArrayList<>();
    @BindView(R.id.add_items_recycler)
    RecyclerView itemsRecycler;

    LinearLayoutManager layoutManager;
    ItemAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutManager = new LinearLayoutManager(this);
        itemsRecycler.setLayoutManager(layoutManager);

        myAdapter = new ItemAdapter(items);
        itemsRecycler.setAdapter(myAdapter);

        fab.setOnClickListener(view -> {
            items.add(addItemEdit.getText().toString());
            myAdapter.notifyDataSetChanged();
        });

        // When they press the save button, send the data back to the main activity
        fab2.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("RecyclerItems", items);
            setResult(MainActivity.MAKE_ITEM_LIST, intent);
            finish();
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
