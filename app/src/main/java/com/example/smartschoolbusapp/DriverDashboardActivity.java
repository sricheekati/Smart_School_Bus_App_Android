package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverDashboardActivity extends AppCompatActivity {

    private EditText latInput, lngInput;
    private Button updateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        // Initialize views
        latInput = findViewById(R.id.lat_input);
        lngInput = findViewById(R.id.lng_input);
        updateLocation = findViewById(R.id.btn_update_location);

        // Update location
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