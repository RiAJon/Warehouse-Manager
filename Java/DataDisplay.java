package com.cs360.warehousemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;
import io.realm.Case;
import io.realm.Realm;

public class DataDisplay extends AppCompatActivity implements RecyclerViewInterface{

    // for recycler view
    RecyclerView recyclerView;
    ArrayList<String> name, sku;
    ArrayList<Integer> count;
    RecyclerAdapter adapter;

    // for spinner
    Spinner spinner;
    String selectedWarehouse;

    // for search bar
    SearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //for spinner dropdown menu
        spinner = findViewById(R.id.warehouse);
        List<String> warehouseNames = getWarehouseNames();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, warehouseNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Set listener for item selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                selectedWarehouse = parent.getItemAtPosition(position).toString();
                //Log.d("DataDisplay", "Selected warehouse: " + selectedWarehouse);

                // Clear existing data before loading new warehouse items
                name.clear();
                sku.clear();
                count.clear();

                fillArrays();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing (optional)
            }
        });

        // for SMS
        int permission = ActivityCompat.checkSelfPermission(DataDisplay.this, android.Manifest.permission.SEND_SMS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DataDisplay.this,
                    new String[] {Manifest.permission.SEND_SMS}, 999
            );
        }

        // for Camera
        int camPermission = ActivityCompat.checkSelfPermission(DataDisplay.this, Manifest.permission.CAMERA);
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DataDisplay.this,
                    new String[] {Manifest.permission.CAMERA}, 100
            );
        }

        // create recycler view
        recyclerView = findViewById(R.id.itemRecycler);
        setAdapter();   // set RecyclerViewAdapter
        fillArrays();   // get data from database

        // for search bar
        search = findViewById(R.id.search);
        search.clearFocus();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

    }

    private void setAdapter() {
        adapter = new RecyclerAdapter(this, this, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

    private void fillArrays() {

        name = new ArrayList<>();
        sku = new ArrayList<>();
        count = new ArrayList<>();

        if (selectedWarehouse == null || selectedWarehouse.trim().isEmpty()) {
            //Log.e("DataDisplay", "Error: No warehouse selected.");
            return;
        }

        // open Database
        Realm realm = Realm.getDefaultInstance();

        // retrieve items
        Warehouse warehouse = realm.copyFromRealm(realm.where(Warehouse.class).equalTo("name", selectedWarehouse.trim(), Case.INSENSITIVE).findFirst());

        if (warehouse != null) {
            List<Item> items = warehouse.getItems();

            if (items != null && !items.isEmpty()) {

                // Clear previous data before adding new items
                name.clear();
                sku.clear();
                count.clear();

                for (Item item : items) {

                    name.add(item.getName());
                    sku.add(item.getSKU());
                    count.add(item.getCount());

                    //Log.d("DataDisplay", "Loaded item: " + item.getName() + ", SKU: " + item.getSKU());
                }
            }
            else {
                Log.e("DataDisplay", "Warehouse has no items.");
            }
        }
        else {
            Log.e("DataDisplay", "Selected warehouse not found.");
        }

        adapter.updateData(name,sku,count);
        realm.close();
    }

    // Add button functionality
    public void onFloatClick(View view) {
        Intent intent = new Intent(this, AddItem.class);

        // pass selectedWarehouse so new item is stored correctly
        intent.putExtra("selectedWarehouse", selectedWarehouse);

        startActivity(intent);
    }

    // Preferences button functionality
    public void onPrefClick(View view) {
        Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
    }

    // Warehouse button functionality
    public void onWarehouseClick(View view){
        Intent intent = new Intent(this, ManageWarehouse.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, EditItem.class);

        // pass extras to activity to customize page based on item clicked
        intent.putExtra("name", name.get(position));
        intent.putExtra("sku", sku.get(position));
        intent.putExtra("count", String.valueOf(count.get(position)));
        intent.putExtra("selectedWarehouse", selectedWarehouse);

        startActivity(intent);
    }

    // search bar filtering by name and SKU
    private void filterList(String text){
        ArrayList<String> filteredNames = new ArrayList<>();
        ArrayList<String> filteredSKUs = new ArrayList<>();
        ArrayList<Integer> filteredCounts = new ArrayList<>();

        for (int i = 0; i < name.size(); i++){
            String itemName = name.get(i).toLowerCase();
            String itemSKU = sku.get(i).toLowerCase();

            if (itemName.contains(text.toLowerCase()) || itemSKU.contains(text.toLowerCase())) {
                filteredNames.add(name.get(i));
                filteredSKUs.add(sku.get(i));
                filteredCounts.add(count.get(i));
            }
        }
        // check if there are results
        if (!filteredNames.isEmpty()){
            adapter.setFilteredList(filteredNames, filteredSKUs, filteredCounts);
        }
    }

    private List<String> getWarehouseNames() {
        Realm realm = Realm.getDefaultInstance();
        List<String> warehouseNames = new ArrayList<>();

        List<Warehouse> warehouses = realm.copyFromRealm(realm.where(Warehouse.class).findAll());

        for (Warehouse warehouse : warehouses) {
            if (warehouse.getName() != null) {
                warehouseNames.add(warehouse.getName().trim());
            }
            else {
                Log.e("DataDisplay", "Found a warehouse with a null name!");
            }
        }

        realm.close();
        return warehouseNames;
    }

}

