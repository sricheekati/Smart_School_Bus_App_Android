package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatMessages;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");
        getSupportActionBar().setTitle("Chat with " + receiverName);

        chatRoomId = getChatRoomId(currentUserId, receiverId);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages, currentUserId);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        createChatRoomIfNotExists();
        listenForMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void createChatRoomIfNotExists() {
        firestore.collection("chatRooms").document(chatRoomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        Map<String, Object> chatRoomData = new HashMap<>();
                        chatRoomData.put("users", Arrays.asList(currentUserId, receiverId));
                        chatRoomData.put("lastMessage", "");
                        chatRoomData.put("timestamp", System.currentTimeMillis());

                        firestore.collection("chatRooms").document(chatRoomId)
                                .set(chatRoomData)
                                .addOnSuccessListener(aVoid -> System.out.println("Chat room created"))
                                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error creating chat room", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            long timestamp = System.currentTimeMillis();

            Map<String, Object> message = new HashMap<>();
            message.put("senderID", currentUserId);
            message.put("receiverID", receiverId);
            message.put("message", messageText);
            message.put("timestamp", timestamp);
            message.put("seen", false);

            firestore.collection("chatRooms").document(chatRoomId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        messageInput.setText("");
                        updateLastMessage(messageText, timestamp);
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateLastMessage(String messageText, long timestamp) {
        Map<String, Object> chatRoomUpdate = new HashMap<>();
        chatRoomUpdate.put("lastMessage", messageText);
        chatRoomUpdate.put("timestamp", timestamp);
        chatRoomUpdate.put("seenBy", Collections.singletonList(currentUserId));
        chatRoomUpdate.put("unreadCount." + receiverId, FieldValue.increment(1));

        firestore.collection("chatRooms").document(chatRoomId)
                .update(chatRoomUpdate)
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update chat room", Toast.LENGTH_SHORT).show());
    }

    /*private void listenForMessages() {
        firestore.collection("chatRooms").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatItem> items = new ArrayList<>();
                    String lastDate = "";

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            String dateString = formatDateHeader(message.getTimestamp());

                            if (!message.getSenderID().equals(currentUserId) && !message.isSeen()) {
                                doc.getReference().update("seen", true);
                            }

                            if (!dateString.equals(lastDate)) {
                                items.add(new DateHeader(dateString));
                                lastDate = dateString;
                            }

                            items.add(message);
                        }
                    }

                    chatMessages.clear();
                    chatMessages.addAll(items);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

                   //firestore.collection("chatRooms").document(chatRoomId)
                     //       .update("seenBy", FieldValue.arrayUnion(currentUserId),
                                //    "unreadCount." + currentUserId, 0);

                    boolean hasUnread = false;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (!message.getSenderID().equals(currentUserId) && !message.isSeen()) {
                            hasUnread = true;
                            doc.getReference().update("seen", true); // mark message seen
                        }
          }

                    if (hasUnread) {
                        //firestore.collection("chatRooms").document(chatRoomId)
                             //   .update("seenBy", FieldValue.arrayUnion(currentUserId),
                                   //     "unreadCount." + currentUserId, 0);

                        // ✅ Mark messages as seen only if current user is viewing chat
                        if (!isFinishing() && !isDestroyed()) {
                            firestore.collection("chatRooms").document(chatRoomId)
                                    .update(
                                            "seenBy", FieldValue.arrayUnion(currentUserId),
                                            "unreadCount." + currentUserId, 0
                                    );
                        }

                    }

                });
    */

    private void listenForMessages() {
        firestore.collection("chatRooms").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatItem> items = new ArrayList<>();
                    String lastDate = "";

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            String dateString = formatDateHeader(message.getTimestamp());

                            // ✅ Mark individual message as seen only if it's from receiver and not already seen
                            if (!message.getSenderID().equals(currentUserId) && !message.isSeen()) {
                                doc.getReference().update("seen", true);
                            }

                            if (!dateString.equals(lastDate)) {
                                items.add(new DateHeader(dateString));
                                lastDate = dateString;
                            }

                            items.add(message);
                        }
                    }

                    // ✅ After rendering messages, update chatRoom seen status
                    chatMessages.clear();
                    chatMessages.addAll(items);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

                    // 🔥 Now safe to mark chatRoom as seen and reset unread count
                    if (!isFinishing() && !isDestroyed()) {
                        firestore.collection("chatRooms").document(chatRoomId)
                                .update(
                                        "seenBy", FieldValue.arrayUnion(currentUserId),
                                        "unreadCount." + currentUserId, 0
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(ChatActivity.this, "Failed to mark chat as seen", Toast.LENGTH_SHORT).show()
                                );
                    }
                });
    }


    private String formatDateHeader(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        Calendar messageCal = Calendar.getInstance();
        messageCal.setTime(date);

        Calendar today = Calendar.getInstance();
        if (messageCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && messageCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        if (messageCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && messageCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        return sdf.format(date);
    }

    private String getChatRoomId(String senderId, String receiverId) {
        return senderId.compareTo(receiverId) < 0 ? senderId + receiverId : receiverId + senderId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (chatRoomId != null && !chatRoomId.isEmpty()) {
            firestore.collection("chatRooms").document(chatRoomId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            //markChatRoomAsSeen();
                        }
                    });
        }
    }

    private void markChatRoomAsSeen() {
        firestore.collection("chatRooms").document(chatRoomId)
                .update("seenBy", FieldValue.arrayUnion(currentUserId),
                        "unreadCount." + currentUserId, 0);
    }
}


