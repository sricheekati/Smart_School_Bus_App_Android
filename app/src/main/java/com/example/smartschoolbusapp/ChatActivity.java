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
import java.util.Arrays;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ✅ Add Back Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        receiverId = getIntent().getStringExtra("receiverId"); // ✅ Fetch receiverId from intent

        // ✅ Debugging: Log receiverId before setup
        System.out.println("🔹 ChatActivity received receiverId: " + receiverId);

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Error: Receiver ID is missing! Fetching from Firestore...", Toast.LENGTH_SHORT).show();
            fetchReceiverId();  // 🔥 Fetch from Firestore if missing
        } else {
            setupChat();
        }
    }

    // ✅ Ensure fetchReceiverId() is logging data properly
    private void fetchReceiverId() {
        firestore.collection("chatRooms")
                .whereArrayContains("users", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            List<String> users = (List<String>) doc.get("users");
                            if (users != null) {
                                for (String userId : users) {
                                    if (!userId.equals(currentUserId)) {
                                        receiverId = userId;
                                        System.out.println("✅ Fetched receiverId from Firestore: " + receiverId);
                                        setupChat();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    Toast.makeText(ChatActivity.this, "Receiver ID could not be found!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Failed to fetch receiver ID", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // ✅ Setup Chat Room
    private void setupChat() {
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Receiver ID is missing! Fetching from Firestore...", Toast.LENGTH_SHORT).show();
            fetchReceiverId(); // 🔥 Try to fetch again if missing
            return;
        }

        chatRoomId = getChatRoomId(currentUserId, receiverId);
        createChatRoomIfNotExists();
        listenForMessages();
    }

    // ✅ Create Chat Room if It Doesn't Exist
    private void createChatRoomIfNotExists() {
        firestore.collection("chatRooms").document(chatRoomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().exists()) {
                        Map<String, Object> chatRoom = new HashMap<>();
                        chatRoom.put("users", Arrays.asList(currentUserId, receiverId));
                        chatRoom.put("lastMessage", "");
                        chatRoom.put("timestamp", System.currentTimeMillis());

                        firestore.collection("chatRooms").document(chatRoomId).set(chatRoom);
                    }
                });
    }

    // ✅ Send Message
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Error: Receiver ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUserId;
        long timestamp = System.currentTimeMillis();

        Map<String, Object> message = new HashMap<>();
        message.put("senderID", senderId);
        message.put("receiverID", receiverId);
        message.put("message", messageText);
        message.put("timestamp", timestamp);

        firestore.collection("chatRooms").document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");  // ✅ Clear input field

                    // ✅ Update chatRooms to store last message preview
                    firestore.collection("chatRooms").document(chatRoomId)
                            .update("lastMessage", messageText, "timestamp", timestamp);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Listen for Messages
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
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : snapshots) {
                            ChatMessage message = document.toObject(ChatMessage.class);
                            chatMessages.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.post(() -> chatRecyclerView.scrollToPosition(chatMessages.size() - 1));
                });
    }

    // ✅ Generate Chat Room ID
    private String getChatRoomId(String senderId, String receiverId) {
        return senderId.compareTo(receiverId) < 0 ? senderId + "_" + receiverId : receiverId + "_" + senderId;
    }

    // ✅ Handle Back Button Click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}