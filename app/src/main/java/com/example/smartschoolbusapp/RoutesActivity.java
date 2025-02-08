package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class RoutesActivity extends AppCompatActivity {

    private EditText routeNameInput, startLocationInput, endLocationInput;
    private Button addRouteButton;
    private RecyclerView routesRecyclerView;
    private RoutesAdapter routesAdapter;
    private List<Routes> routes;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        routeNameInput = findViewById(R.id.route_name);
        startLocationInput = findViewById(R.id.start_location);
        endLocationInput = findViewById(R.id.end_location);
        addRouteButton = findViewById(R.id.btn_add_route);
        routesRecyclerView = findViewById(R.id.routes_list);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        routes = new ArrayList<>();
        routesAdapter = new RoutesAdapter(routes);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routesRecyclerView.setAdapter(routesAdapter);

        userRole = getIntent().getStringExtra("role");

        if (userRole.equals("parent")) {
            addRouteButton.setVisibility(View.GONE);
            routeNameInput.setVisibility(View.GONE);
            startLocationInput.setVisibility(View.GONE);
            endLocationInput.setVisibility(View.GONE);
        }

        addRouteButton.setOnClickListener(v -> addRoute());
        loadRoutes();
    }

    private void addRoute() {
        String routeName = routeNameInput.getText().toString().trim();
        String startLocation = startLocationInput.getText().toString().trim();
        String endLocation = endLocationInput.getText().toString().trim();

        if (TextUtils.isEmpty(routeName) || TextUtils.isEmpty(startLocation) || TextUtils.isEmpty(endLocation)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Routes newRoute = new Routes(routeName, auth.getCurrentUser().getUid(), startLocation, endLocation);
        firestore.collection("routes").add(newRoute)
                .addOnSuccessListener(documentReference -> {
                    routes.add(newRoute);
                    routesAdapter.notifyDataSetChanged();
                    routeNameInput.setText("");
                    startLocationInput.setText("");
                    endLocationInput.setText("");
                    Toast.makeText(this, "Route added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add route", Toast.LENGTH_SHORT).show());
    }

    private void loadRoutes() {
        firestore.collection("routes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                routes.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Routes route = document.toObject(Routes.class);
                    if (route != null) {
                        routes.add(route);
                    }
                }
                routesAdapter.notifyDataSetChanged();
            }
        });
    }
}