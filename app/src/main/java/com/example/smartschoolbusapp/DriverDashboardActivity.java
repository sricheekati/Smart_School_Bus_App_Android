package com.example.smartschoolbusapp;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class DriverDashboardActivity extends AppCompatActivity implements OnMapReadyCallback{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private TextView menuChat, menuRoutes, menuLogout;
    private EditText latInput, lngInput;
    private Button updateLocation;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private Button startTrackingButton;
    private MapView mapView;
    private GoogleMap gMap;
    private Marker busMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        //latInput = findViewById(R.id.lat_input);
        //lngInput = findViewById(R.id.lng_input);
        //updateLocation = findViewById(R.id.btn_update_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        startTrackingButton = findViewById(R.id.startTrackingButton);
        // ✅ Set Toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // ✅ Setup Navigation Drawer Toggle
        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.open, R.string.close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
// Initialize the map view
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
// Set up location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationUpdates();
        // ✅ Get Navigation Header View
        View headerView = navigationView.getHeaderView(0);
        headerView.findViewById(R.id.menuChat).setOnClickListener(v -> startActivity(new Intent(DriverDashboardActivity.this, ChatActivity.class)));
        headerView.findViewById(R.id.menuRoutes).setOnClickListener(v -> startActivity(new Intent(DriverDashboardActivity.this, RoutesActivity.class)));
        headerView.findViewById(R.id.menuLogout).setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(DriverDashboardActivity.this, LoginActivity.class));
            finish();
        });

        // ✅ Get Menu Items
        menuChat = headerView.findViewById(R.id.menuChat);
        menuRoutes = headerView.findViewById(R.id.menuRoutes);
        menuLogout = headerView.findViewById(R.id.menuLogout);

        // ✅ Debugging Check: Log if menuChat is found
        if (menuChat == null) {
            Log.e("DriverDashboardActivity", "menuChat is NULL! Check menu_sidebar.xml");
        } else {
            Log.d("DriverDashboardActivity", "menuChat Found! Setting Click Listener");
            menuChat.setOnClickListener(v -> {
                Log.d("DriverDashboardActivity", "Chat Clicked! Opening ChatBotActivity");
                Intent intent = new Intent(DriverDashboardActivity.this, ChatBotActivity.class);
                startActivity(intent);
            });
        }

        // ✅ Set Click Listeners for Other Menu Items
        menuRoutes.setOnClickListener(v -> {
            Log.d("DriverDashboardActivity", "Routes Clicked! Opening RoutesActivity");
            startActivity(new Intent(DriverDashboardActivity.this, RoutesActivity.class));
        });

        menuLogout.setOnClickListener(v -> {
            Log.d("DriverDashboardActivity", "Logging Out!");
            auth.signOut();
            startActivity(new Intent(DriverDashboardActivity.this, LoginActivity.class));
            finish();
        });

        // ✅ Update Location Button Click Listener
        /*updateLocation.setOnClickListener(v -> {
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
        });*/
        // Update bus location on Firebase and the map when the button is clicked
        startTrackingButton.setOnClickListener(v -> {
            // Fetch the current location (you can adjust this as per your requirements)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Update location in Firebase
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("buses/bus123/location");
                    ref.child("lat").setValue(location.getLatitude());
                    ref.child("lng").setValue(location.getLongitude());

                    // Update location on the map
                    LatLng busPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    if (gMap != null) {
                        if (busMarker == null) {
                            busMarker = gMap.addMarker(new MarkerOptions().position(busPosition).title("Bus Location"));
                        } else {
                            busMarker.setPosition(busPosition);
                        }
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPosition, 15));
                    }

                    Toast.makeText(DriverDashboardActivity.this, "Location Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DriverDashboardActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Method to set up location updates
    private void setupLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // Update every 5 seconds
                .setFastestInterval(2000) // Fastest interval for location updates
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateBusLocation(location);
                }
            }
        };

        requestLocationUpdates();
    }

    // Method to request location updates
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.requestLocationUpdates(LocationRequest.create(), locationCallback, null);
    }

    // Method to update bus location on the map
    private void updateBusLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (gMap != null) {
            LatLng busPosition = new LatLng(latitude, longitude);
            if (busMarker == null) {
                busMarker = gMap.addMarker(new MarkerOptions().position(busPosition).title("Bus Location"));
            } else {
                busMarker.setPosition(busPosition);
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPosition, 15));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
    }

    // Lifecycle methods for the MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}