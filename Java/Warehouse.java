package com.cs360.warehousemanager;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Warehouse extends RealmObject {
    @PrimaryKey
    String address;

    String name;
    RealmList<Item> items = new RealmList<>();

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public RealmList<Item> getItems() {
        if (items != null) {
            return items;
        }
        else {
            return new RealmList<>();
        }
    }

    public void createList() {
        items = new RealmList<>();
    }
}
