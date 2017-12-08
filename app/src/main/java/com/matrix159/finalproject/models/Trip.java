package com.matrix159.finalproject.models;

import java.util.List;

/**
 * Created by josel on 12/7/2017.
 */

public class Trip {

    public Location location;
    public List<Item> items;

    public Trip() {

    }
    public Trip(Location location, List<Item> items) {
        this.location = location;
        this.items = items;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
