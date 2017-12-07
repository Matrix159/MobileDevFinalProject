package com.matrix159.finalproject.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matrix159.finalproject.R;

import java.util.List;

/***
 * Created by Doomninja on 12/6/2017.
 */

public class SelectItemsAdapter extends RecyclerView.Adapter<SelectItemsAdapter.ViewHolder>{

    private List<String> items;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView item;
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
            }
            else {
                selectedItems.put(getAdapterPosition(), true);
                myBackground.setBackgroundResource(R.color.common_google_signin_btn_text_light_pressed);
            }
        }
    }

    // Create new views
    public SelectItemsAdapter(List<String> items){
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
        holder.item.setText(items.get(position));
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
