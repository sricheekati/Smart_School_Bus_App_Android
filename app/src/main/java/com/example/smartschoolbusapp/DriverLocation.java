package com.example.smartschoolbusapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DriverLocation {
    private double latitude;
    private double longitude;

    // Required empty constructor for Firestore
    public DriverLocation() {
    }

    public DriverLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}