package com.cs360.warehousemanager;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Item extends RealmObject {
    @PrimaryKey
    private String sku;

    private String name;
    private int count;
    private  byte[] image;

    // Getters & Setters
    public String getSKU() { return sku; }
    public void setSKU(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}

