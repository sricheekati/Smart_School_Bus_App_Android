package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;

public class DriverActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                double lat = locationResult.getLastLocation().getLatitude();
                double lng = locationResult.getLastLocation().getLongitude();
                updateDriverLocation(lat, lng);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateDriverLocation(double lat, double lng) {
        firestore.collection("drivers").document(driverId)
                .set(new DriverLocation(lat, lng))
                .addOnSuccessListener(aVoid -> Log.d("DriverActivity", "Location Updated"))
                .addOnFailureListener(e -> Log.e("DriverActivity", "Error updating location", e));
    }
}