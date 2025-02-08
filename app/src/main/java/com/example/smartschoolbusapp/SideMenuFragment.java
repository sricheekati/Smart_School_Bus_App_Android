package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SideMenuFragment extends Fragment {

    private Button btnChat, btnRoutes, btnApproveUsers, btnLogout;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_item, container, false);

        btnChat = view.findViewById(R.id.btn_chat);
        btnRoutes = view.findViewById(R.id.btn_routes);
        btnApproveUsers = view.findViewById(R.id.btn_approve_users);
        btnLogout = view.findViewById(R.id.btn_logout);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("users").document(user.getUid())
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userRole = documentSnapshot.getString("role");
                            updateMenuButtons();
                        }
                    });
        }

        btnChat.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChatActivity.class)));
        btnRoutes.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoutesActivity.class);
            intent.putExtra("role", userRole);
            startActivity(intent);
        });

        btnApproveUsers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ApproveUsersActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void updateMenuButtons() {
        btnApproveUsers.setVisibility(userRole.equals("admin") ? View.VISIBLE : View.GONE);
    }
}