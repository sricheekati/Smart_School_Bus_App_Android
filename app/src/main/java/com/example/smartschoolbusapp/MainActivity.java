package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 🔁 Clear Firestore Cache BEFORE doing anything else
        FirebaseFirestore.getInstance().clearPersistence()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Cache cleared successfully."))
                .addOnFailureListener(e -> Log.e("Firestore", "Cache clear failed: " + e.getMessage()));
        setContentView(R.layout.activity_main);

        // Initialize logout button
        logoutButton = findViewById(R.id.btn_logout);

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        FirebaseMessaging.getInstance().subscribeToTopic("parents")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to parents topic");
                    }
                });
    }
}