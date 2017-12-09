package com.matrix159.finalproject.models;

import java.util.List;

/**
 * Created by Eldridge on 12/8/2017.
 */

public class Overall {

    public List<Trip> trips;
    public List<Item> items;
    public List<Location> locations;
    public Trip activeTrip;

    public Overall() {

    }
    public Overall(List<Trip> trips, List<Item> items, List<Location> locations, Trip activeTrip) {
        this.trips = trips;
        this.items = items;
        this.locations = locations;
        this.activeTrip = activeTrip;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public Trip getActiveTrip() {
        return activeTrip;
    }

    public void setActiveTrip(Trip activeTrip) {
        this.activeTrip = activeTrip;
    }
}
