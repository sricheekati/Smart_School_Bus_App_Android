package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton;
    private TextView register, forgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        register = findViewById(R.id.register);
        forgotPassword = findViewById(R.id.forgot_password);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Login button listener
        loginButton.setOnClickListener(v -> loginUser());

        // Register listener
        register.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Forgot Password listener
        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class)));
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Password is required");
            return;
        }

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            checkUserVerificationAndRole(currentUser);
                        }
                    } else {
                        showToast("Error: " + task.getException().getMessage());
                    }
                });
    }

    private void checkUserVerificationAndRole(FirebaseUser currentUser) {
        if (!currentUser.isEmailVerified()) {
            showToast("Please verify your email before logging in.");
            mAuth.signOut();
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot snapshot = task.getResult();
                        String role = snapshot.getString("role");
                        String status = snapshot.getString("status");

                        if (!"approved".equals(status)) {
                            showToast("Your account is pending approval by the Admin.");
                            mAuth.signOut();
                        } else {
                            navigateToDashboard(role);
                        }
                    } else {
                        showToast("User not found.");
                        mAuth.signOut();
                    }
                });
    }

    private void navigateToDashboard(String role) {
        if ("admin".equals(role)) {
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
        } else if ("driver".equals(role)) {
            startActivity(new Intent(LoginActivity.this, DriverDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, ParentDashboardActivity.class));
        }
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}