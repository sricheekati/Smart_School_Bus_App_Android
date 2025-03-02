package com.example.smartschoolbusapp;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class Routes {
    private String routeId;
    private String name;
    private String driverEmail;
    private String startLocation;
    private String endLocation;
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private List<String> assignedParents;
    private List<String> assignedStudents;
    private List<Stop> stops;  // ✅ NEW: List of Stops

    // ✅ Required empty constructor for Firestore
    public Routes() {}

    // ✅ Full constructor with all fields
    public Routes(String routeId, String name, String driverEmail, String startLocation, String endLocation,
                  double startLat, double startLng, double endLat, double endLng,
                  List<String> assignedParents, List<String> assignedStudents, List<Stop> stops) {
        this.routeId = routeId;
        this.name = name;
        this.driverEmail = driverEmail;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.assignedParents = assignedParents;
        this.assignedStudents = assignedStudents;
        this.stops = stops;
    }

    // ✅ Firestore will NOT store this field, but we can use it in Java
    @Exclude
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    // ✅ Getters for Firestore mapping
    public String getName() { return name; }
    public String getDriverEmail() { return driverEmail; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public double getStartLat() { return startLat; }
    public double getStartLng() { return startLng; }
    public double getEndLat() { return endLat; }
    public double getEndLng() { return endLng; }
    public List<String> getAssignedParents() { return assignedParents; }
    public List<String> getAssignedStudents() { return assignedStudents; }
    public List<Stop> getStops() { return stops; }

    // ✅ Setters to allow route updates
    public void setName(String name) { this.name = name; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public void setStartLat(double startLat) { this.startLat = startLat; }
    public void setStartLng(double startLng) { this.startLng = startLng; }
    public void setEndLat(double endLat) { this.endLat = endLat; }
    public void setEndLng(double endLng) { this.endLng = endLng; }
    public void setAssignedParents(List<String> assignedParents) { this.assignedParents = assignedParents; }
    public void setAssignedStudents(List<String> assignedStudents) { this.assignedStudents = assignedStudents; }
    public void setStops(List<Stop> stops) { this.stops = stops; }
}