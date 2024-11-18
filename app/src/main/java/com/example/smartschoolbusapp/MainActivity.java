package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize logout button
        logoutButton = findViewById(R.id.btn_logout);

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity after logout
        });
    }
}