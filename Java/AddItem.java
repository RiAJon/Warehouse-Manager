package com.cs360.warehousemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class AddItem extends AppCompatActivity {

    //FIXME: (future update) - item selection - allow same item in multiple warehouses

    // for camera use
    ImageView imageView;
    int CAMERA_REQUEST_CODE;
    Bitmap photo;
    String selectedWarehouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // for camera use
        imageView = findViewById(R.id.imageView);
        CAMERA_REQUEST_CODE = 100;

        selectedWarehouse = getIntent().getStringExtra("selectedWarehouse");
    }
    // item quantity
    int quantity = 0;

    // FIXME: (future update) - add text edit as UI option for adding count

    // on decrease button click
    public void onDecrease(View view){

        if (quantity > 0) { // no negative counts
            quantity--;
        }

        String quantDisplay = Integer.toString(quantity);
        TextView quantView = findViewById(R.id.quantView);
        quantView.setText(quantDisplay);
    }

    // on increase button click
    public void onIncrease(View view){
        quantity++;
        String quantDisplay = Integer.toString(quantity);

        TextView quantView = findViewById(R.id.quantView);
        quantView.setText(quantDisplay);
    }

    // add button on click
    public void onAdd(View view){

        //FIXME: (future update) - pass back selected warehouse so that the warehouse is open in datadisplay

        // text edits
        EditText itemNameEditText = findViewById(R.id.enter_name);
        EditText itemSkuEditText = findViewById(R.id.enter_sku);

        String itemName = itemNameEditText.getText().toString();
        String itemSku = itemSkuEditText.getText().toString();

        if (itemName.length()>50 || itemSku.length()>50) {
            Toast.makeText(AddItem.this, "Fields must contain less than 50 characters.", Toast.LENGTH_LONG).show();
        }
        else {
            addToDatabase(itemName, itemSku);
        }
    }

    // method to return to main activity
    public void onBack(View view){
        Intent intent = new Intent(this, DataDisplay.class);
        startActivity(intent);
    }

    public void onPhotoClick(View view) {

        // for Camera
        int camPermission = ActivityCompat.checkSelfPermission(AddItem.this, Manifest.permission.CAMERA);
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddItem.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE
            );
            try {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
            catch (Exception e) {
                Toast.makeText(AddItem.this, "You have to grant camera permission to use this feature.", Toast.LENGTH_LONG).show();
            }
        } else {

            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        }
    }

    private void addToDatabase(String name, String sku) {

        Realm realm = Realm.getDefaultInstance();

        try {
            realm.executeTransaction(r -> {
                // Find the selected warehouse
                Warehouse warehouse = r.copyFromRealm(Objects.requireNonNull(r.where(Warehouse.class)
                        .equalTo("name", selectedWarehouse, Case.INSENSITIVE)
                        .findFirst()));

                //Log.d("addItem", "warehouse: " + warehouse + "selected warehouse: " + selectedWarehouse);

                if (warehouse != null) {

                    // Create a new Item object
                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setSKU(sku); // primary key
                    newItem.setCount(quantity);

                    if (photo != null) {
                        newItem.setImage(App.getAppInstance().bitMapToByteArray(photo));
                    } else {
                        newItem.setImage(null);
                    }

                    // Add the item to the warehouse's item list
                    RealmList<Item> itemList = warehouse.getItems();
                    itemList.add(newItem);
                    realm.copyToRealmOrUpdate(warehouse);

                    //Log.d("AddItem", "Item added: " + name + " to warehouse: " + warehouse.getName());

                } else {
                    Log.e("AddItem", "Error: Warehouse not found.");
                }
            });
            Toast.makeText(AddItem.this, "Item Added!", Toast.LENGTH_LONG).show();

            // Check for low stock items & send SMS
            App.getAppInstance().sendSMS(getApplicationContext());

            // Go back to data display page
            Intent addIntent = new Intent(this, DataDisplay.class);
            startActivity(addIntent);

        } catch (Exception e) {
            Log.e("AddItem", "Item not added: " + e.getMessage());
            Toast.makeText(AddItem.this, "Item could not be added.", Toast.LENGTH_LONG).show();
        } finally {
            realm.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imageView.setImageBitmap(photo); // Display the captured image
        }
    }


}