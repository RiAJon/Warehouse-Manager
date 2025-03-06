package com.cs360.warehousemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.SmsManager;
import androidx.core.app.ActivityCompat;
import java.io.ByteArrayOutputStream;
import io.realm.Realm;
import io.realm.RealmResults;

public class App {

    private static App app; // singleton instance

    // constructor
    private App() {}

    public static App getAppInstance() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    // method to send texts based on low stock
    public void sendSMS(Context context) {

        int permission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            SmsManager manager = SmsManager.getDefault();

            // get shared preference
            SharedPreferences pref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            String threshold = pref.getString("threshold", "0");
            String phoneNumber = pref.getString("phoneNumber", "+1 555-123-4567");

            // get user defined low stock threshold
            int t = Integer.parseInt(threshold);
            String message = "The following item is low in stock: ";

            // open Database
            Realm realm = Realm.getDefaultInstance();
            // retrieve all items
            RealmResults<Item> items = realm.where(Item.class).findAll();

            // iterate through items
            for (Item item : items) {
                Item currentItem = realm.copyFromRealm(item); // get java object

                // if item count is below threshold
                if (currentItem.getCount() <= t) {
                    // send text to local port if permission exists
                    manager.sendTextMessage(phoneNumber, null, message + currentItem.getName(), null, null);
                }
            }

            realm.close();
        }
    }

    // convert byte[] to Bitmap
    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null; // Prevent errors if byteArray is empty
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // convert bitmap to byte[]
    public byte[] bitMapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
