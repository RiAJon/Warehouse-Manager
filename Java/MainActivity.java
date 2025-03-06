package com.cs360.warehousemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import io.realm.*;

public class MainActivity extends AppCompatActivity {

    //FIXME: (future update) - accounts - link user and warehouses

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    public void onLoginClick(View view) {

        // text edits
        EditText userNameEditText = findViewById(R.id.enter_username_login);
        EditText passwordEditText = findViewById(R.id.enter_password_login);

        String userName_entered = userNameEditText.getText().toString().trim();
        String password_entered = passwordEditText.getText().toString().trim();

        // Database values
        String password_actual;

        // Wrong credentials message
        String credsMessage = "The username or password you've entered isn't correct. Please try again.";

        // set intent for click
        Intent intent = new Intent(this, DataDisplay.class);

        // if user has filled in both text edits
        if (!userName_entered.isEmpty() && !password_entered.isEmpty()) {

            // get user credentials from database
            Realm realm = Realm.getDefaultInstance();
            User userQResults = realm.where(User.class).equalTo("username", userName_entered).findFirst();

            // if query returns user
            if (userQResults != null) {

                User user = realm.copyFromRealm(userQResults);  // get object's accessible form
                password_actual = user.getPassword().trim();

                if (password_entered.equals(password_actual)) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, credsMessage, Toast.LENGTH_LONG).show();
                }

            }
            else {
                Toast.makeText(MainActivity.this, credsMessage, Toast.LENGTH_LONG).show();
            }

            realm.close();
        }
        else {
            Toast.makeText(MainActivity.this, "Please fill out both fields.", Toast.LENGTH_LONG).show();
        }

    }

    public void onRegClick(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    //FIXME: (future update) - add forgot password functionality

}