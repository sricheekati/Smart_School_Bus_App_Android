package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ChatsListActivity extends AppCompatActivity {

    private RecyclerView chatsRecyclerView;
    private ChatsListAdapter chatsListAdapter;
    private List<ChatRoomModel> chatRoomList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);

        // Setup the toolbar and enable the back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button

        // Initialize RecyclerView
        chatsRecyclerView = findViewById(R.id.chats_list_recycler);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatRoomList = new ArrayList<>();
        chatsListAdapter = new ChatsListAdapter(chatRoomList, this);
        chatsRecyclerView.setAdapter(chatsListAdapter);

        // Load the chats list
        loadChatsList();
    }

    private void loadChatsList() {
        firestore.collection("chatRooms")
                .whereArrayContains("users", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatRoomList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
                        if (chatRoom != null && chatRoom.getUsers().contains(currentUserId)) {
                            String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
                                    ? chatRoom.getUsers().get(1) : chatRoom.getUsers().get(0);
                            chatRoom.setReceiverId(receiverId);
                            chatRoomList.add(chatRoom);
                        }
                    }
                    chatsListAdapter.notifyDataSetChanged();
                });
    }

    // Handle back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Finish the current activity (go back to the previous screen)
            onBackPressed();  // You can also call finish() here, but onBackPressed() is preferred
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}