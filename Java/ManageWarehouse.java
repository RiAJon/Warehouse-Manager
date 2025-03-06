package com.cs360.warehousemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import io.realm.Realm;

public class ManageWarehouse extends AppCompatActivity implements RecyclerViewInterface{

    // recycler view
    RecyclerView recyclerView;
    List<Warehouse> warehouses;
    WarehouseRecyclerAdapter recyclerAdapter;
    HashSet<Integer> selectedPositions;

    public ManageWarehouse() {
        // Default constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_warehouses);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // recycler view
        recyclerView = findViewById(R.id.warehouseRecycler);
        setAdapter();
    }

    private void setAdapter() {
        warehouses = getWarehouses();

        recyclerAdapter = new WarehouseRecyclerAdapter(this, this, warehouses);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(recyclerAdapter);
    }

    public void onAddClick(View view) {
        // text edits
        EditText nameText = findViewById(R.id.warehouseNameInput);
        EditText addressText = findViewById(R.id.warehouseAddressInput);

        String warehouseName = nameText.getText().toString().trim();
        String warehouseAddress = addressText.getText().toString().trim();

        if (warehouseName.isEmpty() || warehouseAddress.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_LONG).show();
            return;
        } else if (warehouseName.length() > 50 || warehouseAddress.length() > 50) {
            Toast.makeText(this, "Fields must contain less than 50 characters", Toast.LENGTH_LONG).show();
            return;
        }

        // open database
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.executeTransaction(r -> {
                if (r.where(Warehouse.class).equalTo("address", warehouseAddress).findFirst() != null) {
                    throw new IllegalStateException("Another selectedWarehouse already exists with that address.");
                }

                // add warehouse to database
                Warehouse warehouse = new Warehouse();
                warehouse.setName(warehouseName);
                warehouse.setAddress(warehouseAddress);
                warehouse.createList();
                r.insertOrUpdate(warehouse);

                recyclerAdapter.updateData(warehouse);
            });

            Toast.makeText(ManageWarehouse.this, "Warehouse Added!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(ManageWarehouse.this, "Addition of selectedWarehouse failed: " + e.getMessage(), Toast.LENGTH_LONG).show();

        } finally {
            realm.close();
        }

    }

    // method to return to main activity
    public void onBack (View view){
        Intent intent = new Intent(this, DataDisplay.class);
        startActivity(intent);
    }

    public void onDelete(View view) {
        selectedPositions = recyclerAdapter.getSelectedItems();

        if (selectedPositions.isEmpty()) {
            Toast.makeText(ManageWarehouse.this, "Select the warehouses you would like to delete.", Toast.LENGTH_LONG).show();
        }

        for (Integer position : selectedPositions) {
            Warehouse selectedWarehouse = warehouses.get(position);

            Realm realm = Realm.getDefaultInstance();

            realm.executeTransaction(r -> {
                Warehouse warehouse = realm.where(Warehouse.class).equalTo("address", selectedWarehouse.getAddress()).findFirst();

                // if item exists
                if (warehouse != null) {
                    warehouse.deleteFromRealm();
                } else {
                    Toast.makeText(ManageWarehouse.this, "Unable to delete Warehouse.", Toast.LENGTH_LONG).show();
                }

                recyclerAdapter.removeData(warehouse);
            });
        }

        refreshPage();

    }

    private void refreshPage() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public List<Warehouse> getWarehouses() {
        Realm realm = Realm.getDefaultInstance();
        List<Warehouse> results = realm.copyFromRealm(realm.where(Warehouse.class).findAll());
        realm.close();

        return results;
    }

    @Override
    public void onItemClick(int position) {
        //FIXME: (future update) - access warehouse details from here
    }
}
