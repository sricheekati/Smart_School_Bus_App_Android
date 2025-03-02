package com.example.smartschoolbusapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Stop {
    private String stopName;
    private double latitude;
    private double longitude;

    public Stop() {}

    public Stop(String stopName, double latitude, double longitude) {
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStopName() { return stopName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setStopName(String stopName) { this.stopName = stopName; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}