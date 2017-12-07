package com.matrix159.finalproject.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matrix159.finalproject.R;
import com.matrix159.finalproject.models.Location;

import java.util.List;

import butterknife.BindView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder>{

    private List<Location> locations;

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView locationName;
        TextView locationLatitude;
        TextView locationLongitude;

        public ViewHolder(View v){
            super(v);
            locationName = v.findViewById(R.id.location_name_item);
            locationLatitude = v.findViewById(R.id.location_lat_item);
            locationLongitude = v.findViewById(R.id.location_long_item);
        }
    }

    // Create new views
    public LocationAdapter(List<Location> locations){
        this.locations = locations;
    }

    // Create new views
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_recycler_item, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Location location = locations.get(position);
        holder.locationName.setText("Name: " + location.getLocationName());
        holder.locationLatitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
        holder.locationLongitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
    }

    // This for some reason
    @Override
    public int getItemCount(){
        return locations.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
