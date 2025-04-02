package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        chatsRecyclerView = findViewById(R.id.chats_list_recycler);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomList = new ArrayList<>();
        chatsListAdapter = new ChatsListAdapter(chatRoomList, this);
        chatsRecyclerView.setAdapter(chatsListAdapter);

        loadChatsList();
    }

    private void loadChatsList() {
        firestore.collection("chatRooms")
                .whereArrayContains("users", currentUserId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Toast.makeText(this, "Error loading chats!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatRoomModel> tempList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatRoomModel chatRoom = doc.toObject(ChatRoomModel.class);
                        if (chatRoom != null && chatRoom.getUsers() != null && chatRoom.getUsers().size() == 2) {
                            chatRoom.setReceiverId(
                                    chatRoom.getUsers().get(0).equals(currentUserId)
                                            ? chatRoom.getUsers().get(1)
                                            : chatRoom.getUsers().get(0)
                            );
                            chatRoom.setChatRoomId(doc.getId());
                            tempList.add(chatRoom);
                        }
                    }
                    tempList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    chatRoomList.clear();
                    chatRoomList.addAll(tempList);
                    chatsListAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            loadChatsList();
        }
    }
}
