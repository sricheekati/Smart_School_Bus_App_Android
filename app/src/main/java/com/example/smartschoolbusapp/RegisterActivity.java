package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullName, emailRegister, passwordRegister, confirmPassword, studentIdField;
    private Spinner roleSpinner;
    private Button signUp;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        fullName = findViewById(R.id.fullName);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        confirmPassword = findViewById(R.id.confirmPassword);
        studentIdField = findViewById(R.id.studentId);
        roleSpinner = findViewById(R.id.roleSpinner);
        signUp = findViewById(R.id.signUp);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        // Enable Back Navigation
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Populate Role Selection Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Parent", "Driver", "Admin"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Show/hide Student ID field based on role selection
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roleSpinner.getSelectedItem().toString();
                studentIdField.setVisibility(selectedRole.equalsIgnoreCase("Parent") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sign Up Button Listener
        signUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = fullName.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();
        String role = roleSpinner.getSelectedItem().toString().toLowerCase();
        String studentId = studentIdField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            fullName.setError("Full Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailRegister.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordRegister.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPasswordText)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }
        if (role.equals("parent") && TextUtils.isEmpty(studentId)) {
            studentIdField.setError("Student ID is required for Parents");
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            showToast("Verification email sent. Please check your inbox.");
                        }
                    });

                    saveUserToFirestore(user.getUid(), name, email, role, studentId);
                }
            } else {
                showToast("Error: " + task.getException().getMessage());
            }
        });
    }

    private void saveUserToFirestore(String userId, String name, String email, String role, String studentId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", role);

        // Parents get automatic approval, Admins & Drivers need admin approval
        userData.put("status", role.equals("parent") ? "approved" : "pending");

        if (role.equals("parent")) {
            userData.put("student_id", studentId);
        }

        userRef.set(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Registration Successful! Please Log In.");
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}