package com.example.smartschoolbusapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Routes {
    private String name;
    private String driverId;
    private String startLocation;
    private String endLocation;

    // Required empty constructor for Firestore
    public Routes() {
    }

    public Routes(String name, String driverId, String startLocation, String endLocation) {
        this.name = name;
        this.driverId = driverId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public String getName() {
        return name;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }
}