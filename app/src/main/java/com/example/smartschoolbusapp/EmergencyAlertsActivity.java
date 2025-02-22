package com.example.smartschoolbusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyAlertsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView emergencyAlertsList;
    private EmergencyAlertAdapter alertAdapter;
    private List<EmergencyAlertModel> alertList;
    private EditText alertInput;
    private Button sendAlertButton, clearAlertsButton;
    private TextView noAlertsText;
    private String userRole, userName, userId;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EmergencyAlertsPrefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alerts);

        // ✅ Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Emergency Alerts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ✅ Initialize Firestore, Auth & SharedPreferences
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = auth.getCurrentUser().getUid();

        emergencyAlertsList = findViewById(R.id.emergency_alerts_list);
        alertInput = findViewById(R.id.alert_message_input);
        sendAlertButton = findViewById(R.id.send_alert_button);
        clearAlertsButton = findViewById(R.id.clear_alerts_button);
        noAlertsText = findViewById(R.id.no_alerts_text);

        emergencyAlertsList.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertAdapter = new EmergencyAlertAdapter(alertList);
        emergencyAlertsList.setAdapter(alertAdapter);

        loadUserDetails();

        // ✅ Handle Sending Alerts
        sendAlertButton.setOnClickListener(v -> sendEmergencyAlert());

        // ✅ Handle Clearing Alerts
        clearAlertsButton.setOnClickListener(v -> clearUserAlerts());
    }

    // ✅ Fetch user role & name
    private void loadUserDetails() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        userName = documentSnapshot.getString("name");

                        if (userRole == null) userRole = "Unknown";
                        if (userName == null) userName = "Unknown";

                        // ✅ Hide input fields for parents
                        if ("parent".equals(userRole)) {
                            sendAlertButton.setVisibility(View.GONE);
                            alertInput.setVisibility(View.GONE);
                        }

                        loadEmergencyAlerts();
                    }
                })
                .addOnFailureListener(e -> {
                    userRole = "Unknown";
                    userName = "Unknown";
                    loadEmergencyAlerts();
                });
    }

    private void loadEmergencyAlerts() {
        long clearedTimestamp = sharedPreferences.getLong(userId + "_cleared_timestamp", 0);

        firestore.collection("emergency_alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading alerts!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    alertList.clear();
                    for (DocumentChange doc : snapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            EmergencyAlertModel alert = doc.getDocument().toObject(EmergencyAlertModel.class);

                            // ✅ Check if timestamp is cleared for this user
                            if (alert.getTimestamp() > clearedTimestamp) {
                                alertList.add(alert);
                            }
                        }
                    }

                    if (alertList.isEmpty()) {
                        noAlertsText.setVisibility(View.VISIBLE);
                        clearAlertsButton.setVisibility(View.GONE);
                        emergencyAlertsList.setVisibility(View.GONE);
                    } else {
                        noAlertsText.setVisibility(View.GONE);
                        emergencyAlertsList.setVisibility(View.VISIBLE);
                        clearAlertsButton.setVisibility(View.VISIBLE);
                        alertAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void sendEmergencyAlert() {
        String alertMessage = alertInput.getText().toString().trim();
        if (alertMessage.isEmpty()) {
            Toast.makeText(this, "Enter an alert message!", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        if (userRole == null || userRole.isEmpty()) userRole = "Unknown";
        if (userName == null || userName.isEmpty()) userName = "Unknown";

        Map<String, Object> alertData = new HashMap<>();
        alertData.put("message", alertMessage);
        alertData.put("sender_name", userName);
        alertData.put("sender_role", userRole);
        alertData.put("timestamp", timestamp);

        firestore.collection("emergency_alerts")
                .add(alertData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Emergency alert sent!", Toast.LENGTH_SHORT).show();
                    alertInput.setText("");
                    loadEmergencyAlerts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send alert!", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearUserAlerts() {
        long currentTimestamp = System.currentTimeMillis();
        sharedPreferences.edit().putLong(userId + "_cleared_timestamp", currentTimestamp).apply();

        alertList.clear();
        alertAdapter.notifyDataSetChanged();
        noAlertsText.setVisibility(View.VISIBLE);
        clearAlertsButton.setVisibility(View.GONE);
        emergencyAlertsList.setVisibility(View.GONE);

        Toast.makeText(this, "Alerts cleared from your view!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}