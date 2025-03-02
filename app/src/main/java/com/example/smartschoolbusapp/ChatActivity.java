package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private String receiverId;
    private String chatRoomId;
    private String currentUserId;
    private String receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get receiverId and receiverName from Intent
        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");
        getSupportActionBar().setTitle("Chat with " + receiverName); // Set the title to the receiver's name

        // Generate chat room ID
        chatRoomId = getChatRoomId(currentUserId, receiverId);

        // Initialize message list and adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages, currentUserId);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check if the chat room exists, if not create one
        createChatRoomIfNotExists();

        // Load chat messages from Firestore
        listenForMessages();

        // Send message action
        sendButton.setOnClickListener(v -> sendMessage());
    }

    // Create chat room if it doesn't exist
    private void createChatRoomIfNotExists() {
        firestore.collection("chatRooms").document(chatRoomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        // If the chat room doesn't exist, create a new one
                        Map<String, Object> chatRoomData = new HashMap<>();
                        chatRoomData.put("users", List.of(currentUserId, receiverId));
                        chatRoomData.put("lastMessage", "");
                        chatRoomData.put("timestamp", System.currentTimeMillis());

                        firestore.collection("chatRooms").document(chatRoomId)
                                .set(chatRoomData)
                                .addOnSuccessListener(aVoid -> {
                                    // Chat room successfully created
                                    System.out.println("Chat room created");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChatActivity.this, "Error creating chat room", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Send message
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            long timestamp = System.currentTimeMillis();

            Map<String, Object> message = new HashMap<>();
            message.put("senderID", currentUserId);
            message.put("receiverID", receiverId);
            message.put("message", messageText);
            message.put("timestamp", timestamp);

            firestore.collection("chatRooms").document(chatRoomId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        messageInput.setText("");  // Clear input field
                        updateLastMessage(messageText, timestamp);  // Update chat room with last message
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Update last message in the chat room
    private void updateLastMessage(String messageText, long timestamp) {
        firestore.collection("chatRooms").document(chatRoomId)
                .update("lastMessage", messageText, "timestamp", timestamp);
    }

    // Listen for new messages
    private void listenForMessages() {
        firestore.collection("chatRooms").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    chatMessages.clear();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            chatMessages.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);  // Scroll to latest message
                });
    }

    // Generate a unique chat room ID
    private String getChatRoomId(String senderId, String receiverId) {
        return senderId.compareTo(receiverId) < 0 ? senderId + "" + receiverId : receiverId + "" + senderId;
    }

    // Handle back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}