package com.matrix159.finalproject.adapters;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matrix159.finalproject.R;
import com.matrix159.finalproject.models.Item;

import java.util.List;

/***
 * Created by Doomninja on 12/6/2017.
 */

public class SelectItemsAdapter extends RecyclerView.Adapter<SelectItemsAdapter.ViewHolder>{

    private List<Item> items;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView item;
        private CardView myBackground;

        public ViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            item = v.findViewById(R.id.item_name_text);
            myBackground = v.findViewById(R.id.myBackground);
        }

        @Override
        public void onClick(View v) {
            if (selectedItems.get(getAdapterPosition(), false)) {
                selectedItems.delete(getAdapterPosition());
                myBackground.setBackgroundResource(R.color.cardview_dark_background);
                item.setTextColor(Color.LTGRAY);
            }
            else {
                selectedItems.put(getAdapterPosition(), true);
                myBackground.setBackgroundResource(R.color.colorAccent);
                item.setTextColor(Color.WHITE);
            }
        }
    }

    // Create new views
    public SelectItemsAdapter(List<Item> items){
        this.items = items;
    }

    // Create new views
    @Override
    public SelectItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_items_recycler, parent, false);

        return new SelectItemsAdapter.ViewHolder(v);
    }

    // Replace the contents
    @Override
    public void onBindViewHolder(SelectItemsAdapter.ViewHolder holder, int position){
        holder.item.setText(items.get(position).getItemName());
        holder.myBackground.setSelected(selectedItems.get(position, false));
    }

    // This for some reason
    @Override
    public int getItemCount(){
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public SparseBooleanArray getSelectedItems(){
        return selectedItems;
    }
}
