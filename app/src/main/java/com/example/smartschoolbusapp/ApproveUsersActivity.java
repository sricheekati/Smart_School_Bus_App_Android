package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ApproveUsersActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView pendingList;
    private PendingUsersAdapter adapter;
    private List<DocumentSnapshot> pendingUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_users);

        firestore = FirebaseFirestore.getInstance();
        pendingList = findViewById(R.id.pending_list);

        pendingUsers = new ArrayList<>();
        adapter = new PendingUsersAdapter(pendingUsers, this);
        pendingList.setLayoutManager(new LinearLayoutManager(this));
        pendingList.setAdapter(adapter);

        loadPendingUsers();
    }

    private void loadPendingUsers() {
        firestore.collection("users").whereEqualTo("status", "pending").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                pendingUsers.clear();
                pendingUsers.addAll(task.getResult().getDocuments());
                adapter.notifyDataSetChanged();
            }
        });
    }
}