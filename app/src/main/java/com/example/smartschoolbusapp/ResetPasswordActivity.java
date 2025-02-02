package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton, backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize Toolbar with Back Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        resetButton = findViewById(R.id.reset_button);
        backButton = findViewById(R.id.back_button);
        mAuth = FirebaseAuth.getInstance();

        // Reset password button listener
        resetButton.setOnClickListener(v -> {
            String userEmail = emailInput.getText().toString().trim();

            if (!TextUtils.isEmpty(userEmail)) {
                resetPassword(userEmail);
            } else {
                emailInput.setError("Email field can't be empty");
            }
        });

        // Back button listener
        backButton.setOnClickListener(v -> navigateToLogin());
    }

    private void resetPassword(String userEmail) {
        resetButton.setEnabled(false); // Disable to prevent multiple clicks
        mAuth.sendPasswordResetEmail(userEmail)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ResetPasswordActivity.this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show();
                    resetButton.setEnabled(true);

                    // ✅ Redirect to LoginActivity after a short delay
                    resetButton.postDelayed(() -> navigateToLogin(), 2000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResetPasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton.setEnabled(true);
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();  // Ensure `ResetPasswordActivity` is removed from back stack
    }

    // Handle Back Button Click in Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}