package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private TextView menuChat, menuRoutes, menuLogout;
    private EditText latInput, lngInput;
    private Button updateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        latInput = findViewById(R.id.lat_input);
        lngInput = findViewById(R.id.lng_input);
        updateLocation = findViewById(R.id.btn_update_location);
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

        menuChat.setOnClickListener(v -> startActivity(new Intent(DriverDashboardActivity.this, ChatActivity.class)));
        menuRoutes.setOnClickListener(v -> startActivity(new Intent(DriverDashboardActivity.this, RoutesActivity.class)));

        menuLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(DriverDashboardActivity.this, LoginActivity.class));
            finish();
        });

        updateLocation.setOnClickListener(v -> {
            String lat = latInput.getText().toString();
            String lng = lngInput.getText().toString();

            if (lat.isEmpty() || lng.isEmpty()) {
                Toast.makeText(DriverDashboardActivity.this, "Enter valid coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("buses/bus123/location");
            ref.child("lat").setValue(lat);
            ref.child("lng").setValue(lng);
            Toast.makeText(DriverDashboardActivity.this, "Location Updated", Toast.LENGTH_SHORT).show();
        });
    }
}