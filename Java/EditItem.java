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

public class EditItem extends AppCompatActivity {

    // for photo functionality
    int CAMERA_REQUEST_CODE = 100;
    ImageView imageView;

    int quantity;
    String name;
    String sku;
    String count;
    Bitmap photo;
    String selectedWarehouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // extra values passed by intent
        name = getIntent().getStringExtra("name");
        sku = getIntent().getStringExtra("sku");
        count = getIntent().getStringExtra("count");
        selectedWarehouse = getIntent().getStringExtra("selectedWarehouse");

        if (selectedWarehouse != null) {
            Log.d("EditItem", selectedWarehouse);
        }

        // set values passed by intent to display
        TextView title = findViewById(R.id.item);
        title.setText(name);

        EditText editName = findViewById(R.id.enter_name);
        editName.setText(name);

        TextView textSku = findViewById(R.id.sku_display);
        textSku.setText(sku);

        TextView editCount = findViewById(R.id.enter_count);
        editCount.setText(count);

        countToQuantity(count);

        //photo functionality
        imageView = findViewById(R.id.imageView1);
        photo = getPhoto(sku);
        if (photo != null) {
            imageView.setImageBitmap(photo);
        }
    }

    // converts count string to int to be used by methods below
    public void countToQuantity(String count) {
        // item quantity
        quantity = Integer.parseInt(count);
    }

    private Bitmap getPhoto(String sku) {
        Realm realm = Realm.getDefaultInstance();

        Item item = realm.where(Item.class).equalTo("sku", sku).findFirst();
        Item itemCopy = realm.copyFromRealm(item);

        byte[] imageArray = itemCopy.getImage();
        Bitmap image;

        if (imageArray != null) {
            image = App.getAppInstance().byteArrayToBitmap(imageArray);
        }
        else {
            //Log.e("EditItem", "Image null");
            image = null;
        }

        realm.close();
        return image;
    }

    // on decrease button click
    public void onEditDecrease(View view){

        if (quantity > 0) { // no negative counts
            quantity--;
        }

        String quantDisplay = Integer.toString(quantity);
        TextView quantView = findViewById(R.id.enter_count);
        quantView.setText(quantDisplay);
    }

    // on increase button click
    public void onEditIncrease(View view){
        quantity++;
        String quantDisplay = Integer.toString(quantity);

        TextView quantView = findViewById(R.id.enter_count);
        quantView.setText(quantDisplay);
    }

    public void onUpdate(View view){
        // text edits
        EditText nameEdit = findViewById(R.id.enter_name);
        String nameNew = nameEdit.getText().toString();

        if (nameNew.length() > 50) {
            Toast.makeText(EditItem.this, "Name must be less than 50 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        Integer countNew = quantity;

        // open database
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction( r -> {

            // get warehouse
            Warehouse warehouse = r.copyFromRealm(Objects.requireNonNull(r.where(Warehouse.class)
                    .equalTo("name", selectedWarehouse, Case.INSENSITIVE)
                    .findFirst()));

            // get item from warehouse
            if (warehouse != null) {

                RealmList<Item> itemList = warehouse.getItems();
                Item item = null;

                // Find the item in the warehouse
                for (Item i : itemList) {
                    if (i.getName() != null) {
                        if (i.getSKU().equals(sku)) {
                            item = i;
                            break;
                        }
                    }
                }

                if (item != null) {
                    // update item
                    item.setName(nameNew);
                    item.setCount(countNew);

                    if (photo != null) {
                        item.setImage(App.getAppInstance().bitMapToByteArray(photo));
                    }

                    realm.copyToRealmOrUpdate(item);
                    Toast.makeText(EditItem.this, "Item Updated!", Toast.LENGTH_LONG).show();

                    // check for low stock items & send SMS
                    App.getAppInstance().sendSMS(getApplicationContext());

                    Intent intent = new Intent(this, DataDisplay.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(EditItem.this, "Item not found in the warehouse.", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(EditItem.this, "Warehouse not found.", Toast.LENGTH_LONG).show();
            }

        });

        realm.close();
    }


    public void onDelete(View view) {

        // open database
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction( r -> {
            Item item = realm.where(Item.class).equalTo("sku", sku).findFirst();

            // if item exists
            if (item != null) {
                item.deleteFromRealm();
                Toast.makeText(EditItem.this, "Item Deleted!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, DataDisplay.class);
                startActivity(intent); // go back
            }
        });

        realm.close();
    }

    public void onPhotoClick(View view) {

        // for Camera
        int camPermission = ActivityCompat.checkSelfPermission(EditItem.this, android.Manifest.permission.CAMERA);
        if (camPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditItem.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE
            );
            try {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
            catch (Exception e) {
                Toast.makeText(EditItem.this, "You have to grant camera permission to use this feature.", Toast.LENGTH_LONG).show();
            }
        } else {

            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        }
    }

    // method to return to main activity
    public void onBack(View view){
        Intent intent = new Intent(this, DataDisplay.class);
        startActivity(intent);
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