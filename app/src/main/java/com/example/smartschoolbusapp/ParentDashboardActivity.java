package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private TextView menuChat, menuRoutes, menuLogout, busLocation, notificationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        busLocation = findViewById(R.id.bus_location);
        notificationText = findViewById(R.id.notification_text);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // ✅ Check if toolbar exists before setting it
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        View headerView = navigationView.getHeaderView(0);
        menuChat = headerView.findViewById(R.id.menuChat);
        menuRoutes = headerView.findViewById(R.id.menuRoutes);
        menuLogout = headerView.findViewById(R.id.menuLogout);

        // ✅ Check if drawerLayout exists before using it
        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.open, R.string.close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        menuChat.setOnClickListener(v -> startActivity(new Intent(ParentDashboardActivity.this, ChatActivity.class)));
        menuRoutes.setOnClickListener(v -> startActivity(new Intent(ParentDashboardActivity.this, RoutesActivity.class)));

        menuLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ParentDashboardActivity.this, LoginActivity.class));
            finish();
        });

        trackBusLocation();
        listenForNotifications();
    }

    private void trackBusLocation() {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("buses/bus123/location");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String lat = snapshot.child("lat").getValue(String.class);
                String lng = snapshot.child("lng").getValue(String.class);
                if (lat != null && lng != null) {
                    busLocation.setText("Bus Location: Latitude: " + lat + ", Longitude: " + lng);
                } else {
                    busLocation.setText("Bus location not available.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ParentDashboardActivity.this, "Failed to load location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForNotifications() {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("buses/bus123/notifications");
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String notification = snapshot.getValue(String.class);
                if (notification != null) {
                    notificationText.setText("Notification: " + notification);
                } else {
                    notificationText.setText("No notifications.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ParentDashboardActivity.this, "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}