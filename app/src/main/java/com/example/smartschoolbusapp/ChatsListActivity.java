//package com.example.smartschoolbusapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChatsListActivity extends AppCompatActivity {
//
//    private RecyclerView chatsRecyclerView;
//    private ChatsListAdapter chatsListAdapter;
//    private List<ChatRoomModel> chatRoomList;
//    private FirebaseAuth auth;
//    private FirebaseFirestore firestore;
//    private String currentUserId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chats_list);
//
//        // Setup the toolbar and enable the back button
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button
//
//        // Initialize RecyclerView
//        chatsRecyclerView = findViewById(R.id.chats_list_recycler);
//        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Initialize Firebase components
//        auth = FirebaseAuth.getInstance();
//        firestore = FirebaseFirestore.getInstance();
//        currentUserId = auth.getCurrentUser().getUid();
//        chatRoomList = new ArrayList<>();
//        chatsListAdapter = new ChatsListAdapter(chatRoomList, this);
//        chatsRecyclerView.setAdapter(chatsListAdapter);
//
//        // Load the chats list
//        loadChatsList();
//    }
//
//   /* private void loadChatsList() {
//        firestore.collection("chatRooms")
//                .whereArrayContains("users", currentUserId)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .addSnapshotListener((queryDocumentSnapshots, error) -> {
//                    if (error != null) {
//                        Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    chatRoomList.clear();
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
//                        if (chatRoom != null && chatRoom.getUsers().contains(currentUserId)) {
//                            String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
//                                    ? chatRoom.getUsers().get(1) : chatRoom.getUsers().get(0);
//                            chatRoom.setReceiverId(receiverId);
//                            chatRoomList.add(chatRoom);
//                        }
//                    }
//                    chatsListAdapter.notifyDataSetChanged();
//                });
//    }*/
//
//   /*private void loadChatsList() {
//        firestore.collection("chatRooms")
//                .whereArrayContains("users", currentUserId)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .addSnapshotListener((queryDocumentSnapshots, error) -> {
//                    if (error != null) {
//                        Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    chatRoomList.clear();
//
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
//
//                        if (chatRoom != null && chatRoom.getUsers().contains(currentUserId)) {
//                            String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
//                                    ? chatRoom.getUsers().get(1)
//                                    : chatRoom.getUsers().get(0);
//                            chatRoom.setReceiverId(receiverId);
//
//                            // 👇 Count unseen messages for this chat room
//                            firestore.collection("chatRooms")
//                                    .document(document.getId())
//                                    .collection("messages")
//                                    .whereEqualTo("receiverID", currentUserId)
//                                    .whereEqualTo("seen", false)
//                                    .get()
//                                    .addOnSuccessListener(querySnapshot -> {
//                                        int unreadCount = querySnapshot.size();
//                                        chatRoom.setUnreadCount(unreadCount);  // 🔥 Save to model
//                                        chatRoomList.add(chatRoom);
//                                        chatsListAdapter.notifyDataSetChanged();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        chatRoom.setUnreadCount(0);
//                                        chatRoomList.add(chatRoom);
//                                        chatsListAdapter.notifyDataSetChanged();
//                                    });
//                        }
//                    }
//                });
//    }
//*/
//   private void loadChatsList() {
//       firestore.collection("chatRooms")
//               .whereArrayContains("users", currentUserId)
//               .orderBy("timestamp", Query.Direction.DESCENDING)
//               .addSnapshotListener((queryDocumentSnapshots, error) -> {
//                   if (error != null || queryDocumentSnapshots == null) {
//                       Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
//                       return;
//                   }
//
//                   List<ChatRoomModel> tempList = new ArrayList<>();
//                   List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//                   /*final int[] processedCount = {0};
//                   int totalValidRooms = 0;
//
//                   for (DocumentSnapshot document : documents) {
//                       ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
//
//                       if (chatRoom != null && chatRoom.getUsers().contains(currentUserId)) {
//                           totalValidRooms++;
//
//                           String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
//                                   ? chatRoom.getUsers().get(1)
//                                   : chatRoom.getUsers().get(0);
//                           chatRoom.setReceiverId(receiverId);
//
//                           firestore.collection("chatRooms")
//                                   .document(document.getId())
//                                   .collection("messages")
//                                   .whereEqualTo("receiverID", currentUserId)
//                                   .whereEqualTo("seen", false)
//                                   .get()
//                                   .addOnSuccessListener(querySnapshot -> {
//                                       chatRoom.setUnreadCount(querySnapshot.size());
//                                       tempList.add(chatRoom);
//                                       processedCount[0]++;
//                                       if (processedCount[0] == totalValidRooms) {
//                                           sortAndDisplayChats(tempList);
//                                       }
//                                   })
//                                   .addOnFailureListener(e -> {
//                                       chatRoom.setUnreadCount(0);
//                                       tempList.add(chatRoom);
//                                       processedCount[0]++;
//                                       if (processedCount[0] == totalValidRooms) {
//                                           sortAndDisplayChats(tempList);
//                                       }
//                                   });
//                       }
//                   }
//
//                   // Handle case when there are no valid chatRooms at all
//                   if (totalValidRooms == 0) {
//                       sortAndDisplayChats(tempList);
//                   }
//               });*/
//
//                   final int[] processedCount = {0};
//                   final int[] totalValidRooms = {0};
//
//                   for (DocumentSnapshot document : documents) {
//                       ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
//
//                       if (chatRoom != null && chatRoom.getUsers().contains(currentUserId)) {
//                           totalValidRooms[0]++;
//
//                           String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
//                                   ? chatRoom.getUsers().get(1)
//                                   : chatRoom.getUsers().get(0);
//                           chatRoom.setReceiverId(receiverId);
//
//                           firestore.collection("chatRooms")
//                                   .document(document.getId())
//                                   .collection("messages")
//                                   .whereEqualTo("receiverID", currentUserId)
//                                   .whereEqualTo("seen", false)
//                                   .get()
//                                   .addOnSuccessListener(querySnapshot -> {
//                                       chatRoom.setUnreadCount(querySnapshot.size());
//                                       tempList.add(chatRoom);
//                                       processedCount[0]++;
//                                       if (processedCount[0] == totalValidRooms[0]) {
//                                           sortAndDisplayChats(tempList);
//                                       }
//                                   })
//                                   .addOnFailureListener(e -> {
//                                       chatRoom.setUnreadCount(0);
//                                       tempList.add(chatRoom);
//                                       processedCount[0]++;
//                                       if (processedCount[0] == totalValidRooms[0]) {
//                                           sortAndDisplayChats(tempList);
//                                       }
//                                   });
//                       }
//                   }
//
//                   if (totalValidRooms[0] == 0) {
//                       sortAndDisplayChats(tempList);
//                   }
//
//               }
//   }
//
//    private void sortAndDisplayChats(List<ChatRoomModel> tempList) {
//        tempList.sort((chat1, chat2) -> Long.compare(chat2.getTimestamp(), chat1.getTimestamp())); // Descending
//        chatRoomList.clear();
//        chatRoomList.addAll(tempList);
//        chatsListAdapter.notifyDataSetChanged();
//    }
//
//
//
//
//
//    // Handle back button in the toolbar
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            // Finish the current activity (go back to the previous screen)
//            onBackPressed();  // You can also call finish() here, but onBackPressed() is preferred
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadChatsList();  // 👈 This will reload and update the unread counts
//    }
//
//}

package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Setup RecyclerView
        chatsRecyclerView = findViewById(R.id.chats_list_recycler);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomList = new ArrayList<>();
        chatsListAdapter = new ChatsListAdapter(chatRoomList, this);
        chatsRecyclerView.setAdapter(chatsListAdapter);

        // Load the chat list
        loadChatsList();
    }

    private void loadChatsList() {
        firestore.collection("chatRooms")
                .whereArrayContains("users", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null || queryDocumentSnapshots == null) {
                        Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatRoomModel> tempList = new ArrayList<>();
                    final int[] processedCount = {0};
                    final int[] totalValidRooms = {0};

                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    // Step 1: Filter valid chat rooms
                    List<DocumentSnapshot> validChatRooms = new ArrayList<>();
                    for (DocumentSnapshot doc : documents) {
                        ChatRoomModel chatRoom = doc.toObject(ChatRoomModel.class);
                        if (chatRoom != null && chatRoom.getUsers() != null && chatRoom.getUsers().contains(currentUserId)) {
                            validChatRooms.add(doc);
                        }
                    }

                    totalValidRooms[0] = validChatRooms.size();

                    // Handle case: no valid rooms
                    if (totalValidRooms[0] == 0) {
                        sortAndDisplayChats(tempList);
                        return;
                    }

                    // Step 2: Process each chat room
                    for (DocumentSnapshot document : validChatRooms) {
                        ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
                        if (chatRoom == null) {
                            processedCount[0]++;
                            continue;
                        }

                        // Identify the other user
                        String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
                                ? chatRoom.getUsers().get(1)
                                : chatRoom.getUsers().get(0);
                        chatRoom.setReceiverId(receiverId);

                        // Count unseen messages for this room
                        firestore.collection("chatRooms")
                                .document(document.getId())
                                .collection("messages")
                                .whereEqualTo("receiverID", currentUserId)
                                .whereEqualTo("seen", false)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    chatRoom.setUnreadCount(querySnapshot.size());
                                    tempList.add(chatRoom);
                                    processedCount[0]++;
                                    if (processedCount[0] == totalValidRooms[0]) {
                                        sortAndDisplayChats(tempList);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    chatRoom.setUnreadCount(0);
                                    tempList.add(chatRoom);
                                    processedCount[0]++;
                                    if (processedCount[0] == totalValidRooms[0]) {
                                        sortAndDisplayChats(tempList);
                                    }
                                });
                    }
                });
    }

    private void sortAndDisplayChats(List<ChatRoomModel> tempList) {
        // Sort by most recent timestamp
        tempList.sort((chat1, chat2) -> Long.compare(chat2.getTimestamp(), chat1.getTimestamp()));
        chatRoomList.clear();
        chatRoomList.addAll(tempList);
        chatsListAdapter.notifyDataSetChanged();
    }

    // Toolbar back button handler
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Not required anymore since we are using real-time updates
    // But can be kept as a fallback refresh
    @Override
    protected void onResume() {
        super.onResume();
        // loadChatsList(); // Optional now
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            loadChatsList(); // 👈 Force refresh chat list when returning from ChatActivity
        }
    }

}