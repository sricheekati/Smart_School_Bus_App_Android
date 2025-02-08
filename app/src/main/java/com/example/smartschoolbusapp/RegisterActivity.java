package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullName, emailRegister, passwordRegister, confirmPassword, studentIdField;
    private Spinner roleSpinner;
    private Button signUp;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;
    private boolean isStudentIdValid = false;

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
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedRole = roleSpinner.getSelectedItem().toString();
                studentIdField.setVisibility(selectedRole.equalsIgnoreCase("Parent") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sign Up Button Listener
        signUp.setOnClickListener(v -> validateInputs());
    }

    // ✅ Validate Inputs Before Registration
    private void validateInputs() {
        String name = fullName.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();
        String role = roleSpinner.getSelectedItem().toString().toLowerCase();
        String studentId = studentIdField.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            fullName.setError("Full Name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            emailRegister.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailRegister.setError("Invalid email format");
            isValid = false;
        }
        if (!isValidPassword(password)) {
            passwordRegister.setError("Password must be at least 6 characters, include 1 uppercase letter & 1 special character");
            isValid = false;
        }
        if (!password.equals(confirmPasswordText)) {
            confirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        if (role.equals("parent") && TextUtils.isEmpty(studentId)) {
            studentIdField.setError("Student ID is required for Parents");
            isValid = false;
        }

        if (!isValid) return; // Stop if any validation fails

        // ✅ Proceed to check if Email Exists
        checkEmailExistsBeforeRegistration(email, role, studentId);
    }

    // ✅ Check if Email is Already Registered in Firestore
    private void checkEmailExistsBeforeRegistration(String email, String role, String studentId) {
        firestore.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        emailRegister.setError("This email is already registered. Please log in.");
                    } else {
                        // If email is unique, check student ID (for parents)
                        if (role.equals("parent")) {
                            validateStudentId(studentId);
                        } else {
                            registerUser(email, role, studentId);
                        }
                    }
                });
    }

    // ✅ Validate Student ID Before Registration
    private void validateStudentId(String studentId) {
        firestore.collection("students").document(studentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            isStudentIdValid = true;
                            registerUser(emailRegister.getText().toString().trim(), "parent", studentId);
                        } else {
                            studentIdField.setError("Invalid Student ID. Please enter a valid ID.");
                        }
                    } else {
                        studentIdField.setError("Error checking student ID. Try again.");
                    }
                });
    }

    // ✅ Register User in Firebase Authentication & Firestore
    private void registerUser(String email, String role, String studentId) {
        String name = fullName.getText().toString().trim();
        String password = passwordRegister.getText().toString();

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

    // ✅ Save User to Firestore
    private void saveUserToFirestore(String userId, String name, String email, String role, String studentId) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("status", role.equals("parent") ? "approved" : "pending");

        if (role.equals("parent")) {
            userData.put("student_id", studentId);
        }

        firestore.collection("users").document(userId).set(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Registration Successful! Please Log In.");
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    // ✅ Strong Password Validation: At least 6 characters, 1 uppercase letter, and 1 special character
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}