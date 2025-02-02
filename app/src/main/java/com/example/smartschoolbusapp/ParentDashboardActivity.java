package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentDashboardActivity extends AppCompatActivity {

    private TextView busLocation, notificationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Initialize views
        busLocation = findViewById(R.id.bus_location);
        notificationText = findViewById(R.id.notification_text);

        // Track Bus Location
        trackBusLocation();

        // Listen for Notifications
        listenForNotifications();
    }

    private void trackBusLocation() {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("buses/bus123/location");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lat = snapshot.child("lat").getValue(String.class);
                String lng = snapshot.child("lng").getValue(String.class);
                if (lat != null && lng != null) {
                    busLocation.setText("Bus Location: Latitude: " + lat + ", Longitude: " + lng);
                } else {
                    busLocation.setText("Bus location not available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentDashboardActivity.this, "Failed to load location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForNotifications() {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("buses/bus123/notifications");
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String notification = snapshot.getValue(String.class);
                if (notification != null) {
                    notificationText.setText("Notification: " + notification);
                } else {
                    notificationText.setText("No notifications.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentDashboardActivity.this, "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}