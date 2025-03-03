package com.example.smartschoolbusapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap mMap;
    private SearchView routesSearchView;
    private RecyclerView routesRecyclerView;
    private RoutesAdapter routesAdapter;
    private List<Routes> routesList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userRole;
    private List<Marker> markers = new ArrayList<>();
    private Button createRouteButton; // Admin button for creating routes
    private LinearLayout routeListContainer;
    private List<TextView> routeTextViews = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // ✅ Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userRole = getIntent().getStringExtra("role");

        if (TextUtils.isEmpty(userRole)) {
            fetchUserRoleFromFirestore();
        }

        mapView = findViewById(R.id.mapView);
        routesSearchView = findViewById(R.id.routesSearchView);
        routesRecyclerView = findViewById(R.id.routesRecyclerView);
        createRouteButton = findViewById(R.id.createRouteButton);
        routeListContainer = findViewById(R.id.routeListContainer);

        routesList = new ArrayList<>();
        routesAdapter = new RoutesAdapter(routesList, this::showEditDeleteDialog);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routesRecyclerView.setAdapter(routesAdapter);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        loadRoutesForRole();
        setupSearchView();

        createRouteButton.setVisibility("admin".equals(userRole) ? View.VISIBLE : View.GONE);
//        createRouteButton.setEnabled(true);
//        createRouteButton.setClickable(true);
        createRouteButton.setOnClickListener(v -> showCreateRouteDialog());

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void fetchUserRoleFromFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        loadRoutesForRole();
                        createRouteButton.setVisibility("admin".equals(userRole) ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadRoutesForRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Query query = firestore.collection("routes");

        if ("admin".equals(userRole)) {
            query = firestore.collection("routes"); // Admin sees all routes
        } else if ("parent".equals(userRole)) {
            // ✅ Step 1: Fetch Student IDs for this Parent
            firestore.collection("students")
                    .whereEqualTo("parent_email", user.getEmail().trim()) // Fetch students assigned to this parent
                    .get()
                    .addOnSuccessListener(studentDocs -> {
                        List<String> studentIds = new ArrayList<>();
                        for (DocumentSnapshot studentDoc : studentDocs) {
                            studentIds.add(studentDoc.getId()); // Get student ID
                        }

                        if (!studentIds.isEmpty()) {
                            // ✅ Step 2: Fetch Routes Assigned to the Parent's Students
                            firestore.collection("routes")
                                    .whereArrayContainsAny("assignedStudents", studentIds)
                                    .get()
                                    .addOnSuccessListener(routeDocs -> {
                                        routesList.clear();
                                        clearMapMarkers();
                                        routeListContainer.removeAllViews();
                                        routeTextViews.clear();

                                        for (DocumentSnapshot document : routeDocs) {
                                            try {
                                                Routes route = document.toObject(Routes.class);
                                                if (route != null) {
                                                    route.setRouteId(document.getId());

                                                    // ✅ Convert Stops from Firestore HashMap to List<Stop>
                                                    if (document.contains("stops")) {
                                                        List<Stop> stopsList = new ArrayList<>();
                                                        List<?> stopsData = (List<?>) document.get("stops");

                                                        for (Object stopObj : stopsData) {
                                                            if (stopObj instanceof HashMap) {
                                                                HashMap<String, Object> stopMap = (HashMap<String, Object>) stopObj;
                                                                String stopName = (String) stopMap.get("stopName");
                                                                double latitude = ((Number) stopMap.get("latitude")).doubleValue();
                                                                double longitude = ((Number) stopMap.get("longitude")).doubleValue();
                                                                stopsList.add(new Stop(stopName, latitude, longitude));
                                                            }
                                                        }
                                                        route.setStops(stopsList);
                                                    }

                                                    routesList.add(route);

                                                    // ✅ Display Assigned Route in UI
                                                    TextView routeTextView = new TextView(this);
                                                    routeTextView.setText(route.getName());
                                                    routeTextView.setTextSize(18);
                                                    routeTextView.setPadding(10, 10, 10, 10);
                                                    routeTextView.setTextColor(Color.BLUE);
                                                    routeTextView.setOnClickListener(v -> showRouteOnMap(route));

                                                    routeListContainer.addView(routeTextView);
                                                    routeTextViews.add(routeTextView);
                                                    addRouteMarkerToMap(route);
                                                }
                                            } catch (Exception e) {
                                                Log.e("Firestore", "Error parsing route data", e);
                                            }
                                        }
                                        routesAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching assigned routes", e));
                        } else {
                            Log.d("Firestore", "No students found for this parent.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching students for parent", e));
        } else if ("driver".equals(userRole)) {
            query = query.whereEqualTo("driverEmail", user.getEmail());
        }

        if (!"parent".equals(userRole)) { // Parents query is handled separately
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    routesList.clear();
                    clearMapMarkers();
                    routeListContainer.removeAllViews();
                    routeTextViews.clear();

                    for (DocumentSnapshot document : task.getResult()) {
                        try {
                            Routes route = document.toObject(Routes.class);
                            if (route != null) {
                                route.setRouteId(document.getId());

                                // ✅ Convert Stops from Firestore HashMap to List<Stop>
                                if (document.contains("stops")) {
                                    List<Stop> stopsList = new ArrayList<>();
                                    List<?> stopsData = (List<?>) document.get("stops");

                                    for (Object stopObj : stopsData) {
                                        if (stopObj instanceof HashMap) {
                                            HashMap<String, Object> stopMap = (HashMap<String, Object>) stopObj;
                                            String stopName = (String) stopMap.get("stopName");
                                            double latitude = ((Number) stopMap.get("latitude")).doubleValue();
                                            double longitude = ((Number) stopMap.get("longitude")).doubleValue();
                                            stopsList.add(new Stop(stopName, latitude, longitude));
                                        }
                                    }
                                    route.setStops(stopsList);
                                }

                                routesList.add(route);

                                // ✅ Display Route Name
                                TextView routeTextView = new TextView(this);
                                routeTextView.setText(route.getName());
                                routeTextView.setTextSize(18);
                                routeTextView.setPadding(10, 10, 10, 10);
                                routeTextView.setTextColor(Color.BLUE);
                                routeTextView.setOnClickListener(v -> showRouteOnMap(route));

                                routeListContainer.addView(routeTextView);
                                routeTextViews.add(routeTextView);
                                addRouteMarkerToMap(route);
                            }
                        } catch (Exception e) {
                            Log.e("Firestore", "Error parsing route data", e);
                        }
                    }
                    routesAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Firestore", "Error loading routes", task.getException());
                }
            });
        }
    }
    private void showRouteOnMap(Routes route) {
        if (mMap == null) return;
        clearMapMarkers();

        LatLng startLocation = new LatLng(route.getStartLat(), route.getStartLng());
        LatLng endLocation = new LatLng(route.getEndLat(), route.getEndLng());

        Marker startMarker = mMap.addMarker(new MarkerOptions().position(startLocation).title("Start: " + route.getStartLocation()));
        Marker endMarker = mMap.addMarker(new MarkerOptions().position(endLocation).title("End: " + route.getEndLocation()));

        if (startMarker != null) markers.add(startMarker);
        if (endMarker != null) markers.add(endMarker);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12));

        // ✅ Real-time tracking for stops
        for (Stop stop : route.getStops()) {
            LatLng stopLocation = new LatLng(stop.getLatitude(), stop.getLongitude());
            Marker stopMarker = mMap.addMarker(new MarkerOptions().position(stopLocation).title("Stop: " + stop.getStopName()));

            if (stopMarker != null) markers.add(stopMarker);
        }

        // ✅ Draw route polyline (Optional)
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(startLocation);
        for (Stop stop : route.getStops()) {
            routePoints.add(new LatLng(stop.getLatitude(), stop.getLongitude()));
        }
        routePoints.add(endLocation);

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(10)
                .color(Color.BLUE);

        mMap.addPolyline(polylineOptions);
    }

    private void addRouteMarkerToMap(Routes route) {
        if (mMap == null || route == null) return;

        LatLng startLocation = new LatLng(route.getStartLat(), route.getStartLng());
        LatLng endLocation = new LatLng(route.getEndLat(), route.getEndLng());

        Marker startMarker = mMap.addMarker(new MarkerOptions()
                .position(startLocation)
                .title("Start: " + route.getStartLocation()));

        Marker endMarker = mMap.addMarker(new MarkerOptions()
                .position(endLocation)
                .title("End: " + route.getEndLocation()));

        // ✅ Store markers for clearing later
        if (startMarker != null) markers.add(startMarker);
        if (endMarker != null) markers.add(endMarker);

        // ✅ Move camera to focus on the route
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12));

        // ✅ Add stop markers if available
        if (route.getStops() != null) {
            for (Stop stop : route.getStops()) {
                LatLng stopLocation = new LatLng(stop.getLatitude(), stop.getLongitude());
                Marker stopMarker = mMap.addMarker(new MarkerOptions()
                        .position(stopLocation)
                        .title("Stop: " + stop.getStopName()));

                if (stopMarker != null) markers.add(stopMarker);
            }
        }
    }

    private void clearMapMarkers() {
        if (mMap != null) {
            for (Marker marker : markers) {
                marker.remove(); // Remove marker from the map
            }
            markers.clear(); // Clear the marker list
        }
    }

    private void showCreateRouteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_route, null);
        builder.setView(dialogView);

        EditText routeName = dialogView.findViewById(R.id.route_name);
        EditText startLocation = dialogView.findViewById(R.id.start_location);
        EditText endLocation = dialogView.findViewById(R.id.end_location);
        EditText driverEmail = dialogView.findViewById(R.id.driver_email);
        EditText stopsField = dialogView.findViewById(R.id.stops_input);
        EditText studentsField = dialogView.findViewById(R.id.students_input);
        Button saveButton = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String name = routeName.getText().toString().trim();
            String start = startLocation.getText().toString().trim();
            String end = endLocation.getText().toString().trim();
            String driver = driverEmail.getText().toString().trim();
            String stopsText = stopsField.getText().toString().trim();
            String studentsText = studentsField.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end) || TextUtils.isEmpty(driver)) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double[] startCoords = getLatLngFromAddress(start);
            double[] endCoords = getLatLngFromAddress(end);
            if (startCoords[0] == 0.0 || endCoords[0] == 0.0) {
                Toast.makeText(this, "Invalid start or end location!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Stop> stopsList = new ArrayList<>();
            if (!stopsText.isEmpty()) {
                for (String stopText : stopsText.split(",")) {
                    double[] stopCoords = getLatLngFromAddress(stopText.trim());
                    stopsList.add(new Stop(stopText.trim(), stopCoords[0], stopCoords[1]));
                }
            }

            List<String> assignedStudents = new ArrayList<>(Arrays.asList(studentsText.split(",")));
            List<String> assignedParents = new ArrayList<>();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Verify the user is an admin before proceeding
            firestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists() && "admin".equals(userDoc.getString("role"))) {
                            AtomicInteger pendingRequests = new AtomicInteger(assignedStudents.size());

                            if (assignedStudents.isEmpty()) {
                                saveRoute(name, start, end, driver, startCoords, endCoords, assignedStudents, assignedParents, stopsList, dialog);
                                return;
                            }

                            // ✅ Fetch parent emails for each assigned student
                            for (String studentId : assignedStudents) {
                                firestore.collection("students").document(studentId.trim()).get()
                                        .addOnSuccessListener(doc -> {
                                            if (doc.exists() && doc.contains("parent_email")) {
                                                String parentEmail = doc.getString("parent_email");
                                                if (!TextUtils.isEmpty(parentEmail) && !assignedParents.contains(parentEmail)) {
                                                    assignedParents.add(parentEmail); // Store unique parent emails
                                                }
                                            }
                                            // ✅ Ensure all requests are completed before saving the route
                                            if (pendingRequests.decrementAndGet() == 0) {
                                                saveRoute(name, start, end, driver, startCoords, endCoords, assignedStudents, assignedParents, stopsList, dialog);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error fetching student data", e);
                                            if (pendingRequests.decrementAndGet() == 0) {
                                                saveRoute(name, start, end, driver, startCoords, endCoords, assignedStudents, assignedParents, stopsList, dialog);
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Access Denied: Only admins can add routes", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching user role", e);
                        Toast.makeText(this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void showEditDeleteDialog(Routes route) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_route, null);
        builder.setView(dialogView);

        EditText routeName = dialogView.findViewById(R.id.route_name);
        EditText startLocation = dialogView.findViewById(R.id.start_location);
        EditText endLocation = dialogView.findViewById(R.id.end_location);
        EditText driverEmail = dialogView.findViewById(R.id.driver_email);
        EditText stopsField = dialogView.findViewById(R.id.stops_input);
        EditText studentsField = dialogView.findViewById(R.id.students_input);
        Button saveButton = dialogView.findViewById(R.id.btn_save);
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");

        // ✅ Populate existing data
        routeName.setText(route.getName());
        startLocation.setText(route.getStartLocation());
        endLocation.setText(route.getEndLocation());
        driverEmail.setText(route.getDriverEmail());
        studentsField.setText(TextUtils.join(",", route.getAssignedStudents()));

        // Convert stops list to comma-separated text
        List<Stop> stops = route.getStops();
        StringBuilder stopsText = new StringBuilder();
        for (Stop stop : stops) {
            stopsText.append(stop.getStopName()).append(",");
        }
        stopsField.setText(stopsText.toString());

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            route.setName(routeName.getText().toString());
            route.setStartLocation(startLocation.getText().toString());
            route.setEndLocation(endLocation.getText().toString());
            route.setDriverEmail(driverEmail.getText().toString());

            // ✅ Process updated stops
            List<Stop> updatedStops = new ArrayList<>();
            for (String stopName : stopsField.getText().toString().split(",")) {
                double[] stopCoords = getLatLngFromAddress(stopName.trim());
                updatedStops.add(new Stop(stopName.trim(), stopCoords[0], stopCoords[1]));
            }
            route.setStops(updatedStops);

            // ✅ Update Firestore with new route data
            FirebaseFirestore.getInstance().collection("routes")
                    .document(route.getRouteId())
                    .set(route)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Route updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoutesForRole();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating route", Toast.LENGTH_SHORT).show());
        });

        deleteButton.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("routes")
                    .document(route.getRouteId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Route deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoutesForRole();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error deleting route", Toast.LENGTH_SHORT).show());
        });

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialog.dismiss());
        builder.setPositiveButton("Delete", (dialogInterface, i) -> deleteButton.performClick());

        dialog.show();
    }

    private void saveRoute(String name, String start, String end, String driver, double[] startCoords, double[] endCoords, List<String> students, List<String> parents, List<Stop> stops, AlertDialog dialog) {
        Routes newRoute = new Routes("", name, driver, start, end, startCoords[0], startCoords[1], endCoords[0], endCoords[1], parents, students, stops);
        firestore.collection("routes").add(newRoute)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Route Created Successfully!", Toast.LENGTH_SHORT).show();
                    loadRoutesForRole();
                    dialog.dismiss(); // ✅ Close only on success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving route!", Toast.LENGTH_SHORT).show();
                });
    }

    private double[] getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new double[]{location.getLatitude(), location.getLongitude()};
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new double[]{0.0, 0.0};
    }

    public void drawRoutePolyline(List<LatLng> points) {
        if (mMap != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(10)
                    .color(Color.BLUE);

            mMap.addPolyline(polylineOptions);
        }
    }

    private void setupSearchView() {
        routesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRoutesList(newText);
                return false;
            }
        });
    }
    private void searchRouteByName(String routeName) {
        for (Routes route : routesList) {
            if (route.getName().equalsIgnoreCase(routeName.trim())) {
                showRouteOnMap(route);
                return;
            }
        }
        Toast.makeText(this, "Route not found!", Toast.LENGTH_SHORT).show();
    }
    private void filterRoutesList(String query) {
        for (TextView routeTextView : routeTextViews) {
            if (routeTextView.getText().toString().toLowerCase().contains(query.toLowerCase())) {
                routeTextView.setVisibility(View.VISIBLE);
            } else {
                routeTextView.setVisibility(View.GONE);
            }
        }
    }
    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}