package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        receiverId = getIntent().getStringExtra("receiverID");

        if (currentUser != null && receiverId != null) {
            chatRoomId = getChatRoomId(currentUser.getUid(), receiverId);
            listenForMessages();
        } else {
            Toast.makeText(this, "Error: Receiver not found", Toast.LENGTH_SHORT).show();
        }

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        Map<String, Object> message = new HashMap<>();
        message.put("senderID", currentUser.getUid());
        message.put("receiverID", receiverId);
        message.put("message", messageText);
        message.put("timestamp", System.currentTimeMillis());

        firestore.collection("chats").document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> messageInput.setText(""))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show());
    }

    private void listenForMessages() {
        CollectionReference messagesRef = firestore.collection("chats").document(chatRoomId).collection("messages");

        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    return;
                }

                chatMessages.clear();
                for (QueryDocumentSnapshot document : value) {
                    ChatMessage message = document.toObject(ChatMessage.class);
                    chatMessages.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }

    private String getChatRoomId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}