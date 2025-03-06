package com.cs360.warehousemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Preferences extends AppCompatActivity {
    String thresholdEntered;
    String numberEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // display preferences
        SharedPreferences pref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String threshold = pref.getString("threshold","0");
        String Pnumber = pref.getString("phoneNumber", "+1 555-123-4567");

        EditText enter = findViewById(R.id.enter_threshold);
        EditText number = findViewById(R.id.enter_number);

        enter.setText(threshold);
        number.setText(Pnumber);
    }

    // method to update sms threshold
    public void onUpdateThresh(View view) {
        // text edits
        EditText threshold = findViewById(R.id.enter_threshold);
        EditText number = findViewById(R.id.enter_number);

        thresholdEntered = threshold.getText().toString();
        numberEntered = number.getText().toString();

        // create shared preferences
        SharedPreferences pref = getSharedPreferences(
                "myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("threshold", thresholdEntered);
        editor.putString("phoneNumber", numberEntered);
        editor.apply();

        Toast.makeText(Preferences.this, "Preference Updated!", Toast.LENGTH_LONG).show();

        // check for low stock items & send SMS
        App.getAppInstance().sendSMS(getApplicationContext());

        Intent intent = new Intent(this, DataDisplay.class);
        startActivity(intent);
    }

    // method to return to main activity
    public void onBack(View view){
        Intent intent = new Intent(this, DataDisplay.class);
        startActivity(intent);
    }

    public void onLogout(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}


