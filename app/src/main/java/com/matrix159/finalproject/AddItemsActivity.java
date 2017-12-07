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
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.matrix159.finalproject.adapters.ItemAdapter;
import com.matrix159.finalproject.models.Location;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * We will use this class to add items to our "global" item list.
 */
public class AddItemsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference topRef;
    public ArrayList<String> itemList = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.fab2)
    FloatingActionButton fab2;
    @BindView(R.id.add_items_edit_text)
    EditText addItemEdit;
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

        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        topRef = database.getReference(auth.getUid() + "/items");

        // Get this list from firebase instead of passing it
        // itemList = getIntent().getExtras().getStringArrayList("ItemList");
        itemList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(this);
        itemsRecycler.setLayoutManager(layoutManager);
        myAdapter = new ItemAdapter(itemList);
        itemsRecycler.setAdapter(myAdapter);

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
                itemList.remove(viewHolder.getLayoutPosition());
                myAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                topRef.setValue(itemList);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(itemsRecycler);

        // Read from the database
        topRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> snapshot = dataSnapshot.getValue(t);
                if(snapshot != null) {
                    itemList.clear();
                    itemList.addAll(snapshot);
                    myAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        fab.setOnClickListener(view -> {
            if(!addItemEdit.getText().toString().equals("")){
                // Check if the item is already in their list, if it is, don't add it
                Boolean inList = false;
                for (String s : itemList){
                    if(addItemEdit.getText().toString().toLowerCase().equals(s.toLowerCase())){
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content), "That item is already added", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        inList = true;
                    }
                }

                if(!inList) {
                    //itemList.add(addItemEdit.getText().toString());
                    itemList.add(addItemEdit.getText().toString());
                    topRef.setValue(itemList);
                    myAdapter.notifyDataSetChanged();
                    addItemEdit.setText("");
                }
            }
        });

        // When they press the save button, send the data back to the main activity
        fab2.setOnClickListener(view -> {
            // Get all pressed buttons
            if((itemList.size() != 0)){
                Intent intent = new Intent();
                intent.putExtra("RecyclerItems", itemList);
                setResult(MainActivity.MAKE_ITEM_LIST, intent);
                finish();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
