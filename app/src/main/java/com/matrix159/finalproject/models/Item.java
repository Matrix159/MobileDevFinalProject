package com.matrix159.finalproject.models;

/**
 * Created by josel on 12/7/2017.
 */

public class Item {

    public String itemName;
    public boolean checked;

    public Item() {
    }

    public Item(String itemName, boolean checked) {
        this.itemName = itemName;
        this.checked = checked;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return itemName;
    }
}
