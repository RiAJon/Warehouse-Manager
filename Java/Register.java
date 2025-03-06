package com.cs360.warehousemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void onRegisterClick(View view) {
        // text edits
        EditText userNameEditText = findViewById(R.id.enter_username);
        EditText passwordEditText = findViewById(R.id.enter_password);

        String userName = userNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (userName.length() > 50 || password.length() > 50){
            Toast.makeText(Register.this, "Fields must contain less than 50 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        // set intent for click
        Intent Regintent = new Intent(this, DataDisplay.class);

        // open database
        Realm realm = Realm.getDefaultInstance();

        // if user has filled in both text edits
        if (!userName.isEmpty() && !password.isEmpty()) {

            try {
                realm.executeTransaction(r -> {
                    // add user to database
                    User user = new User();
                    user.setUsername(userName);
                    user.setPassword(password);
                    r.insertOrUpdate(user); // This ensures the password is stored
                });

                Toast.makeText(Register.this, "You have been registered!.", Toast.LENGTH_LONG).show();
                startActivity(Regintent);

            } catch (Exception e) {
                Toast.makeText(Register.this, "Registration failed.", Toast.LENGTH_LONG).show();
                Log.e("Register", "Registration Error: " + e.getMessage());

            } finally {
                realm.close();
            }
        }else{
            Toast.makeText(Register.this, "Please fill out both fields.", Toast.LENGTH_LONG).show();
        }

    }

    public void onLoginClick(View view) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }
}




