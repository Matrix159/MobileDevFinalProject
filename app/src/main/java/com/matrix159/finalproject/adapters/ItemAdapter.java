package com.matrix159.finalproject.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matrix159.finalproject.R;

import java.util.List;

import butterknife.OnClick;

/***
 * Created by Doomninja on 11/30/2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    private List<String> items;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView item;
        // Background of the layout, probably won't work
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
                myBackground.setSelected(false);
            }
            else {
                selectedItems.put(getAdapterPosition(), true);
                myBackground.setSelected(true);
            }
        }
    }

    // Create new views
    public ItemAdapter(List<String> items){
        this.items = items;
    }

    // Create new views
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_for_recycler, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
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


}
