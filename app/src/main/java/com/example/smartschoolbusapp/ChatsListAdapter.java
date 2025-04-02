package com.example.smartschoolbusapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatsListAdapter extends RecyclerView.Adapter<ChatsListAdapter.ViewHolder> {

    private List<ChatRoomModel> chatRooms;
    private Context context;
    private FirebaseFirestore firestore;
    private String currentUserId;

    public ChatsListAdapter(List<ChatRoomModel> chatRooms, Context context) {
        this.chatRooms = chatRooms;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoomModel chatRoom = chatRooms.get(position);
        String receiverId = chatRoom.getReceiverId();

        FirebaseFirestore.getInstance().collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        holder.userName.setText(doc.getString("name"));
                    } else {
                        holder.userName.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> holder.userName.setText("Unknown User"));

        // 📨 Show last message
        holder.lastMessage.setText(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "");

        // 🔵 Bold if message is unread by current user
        if (chatRoom.getSeenBy() == null || !chatRoom.getSeenBy().contains(currentUserId)) {
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
        }

        // 🕒 Timestamp
        holder.timestamp.setText(formatTimestamp(chatRoom.getTimestamp()));

        // 🔢 Unread count badge
        int unread = chatRoom.getUnreadCountForUser(currentUserId);
        if (unread > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(unread));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        // 🚀 Launch chat activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", receiverId);
            intent.putExtra("receiverName", holder.userName.getText().toString());
            ((AppCompatActivity) context).startActivityForResult(intent, 1001);
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage, timestamp, unreadBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.chat_user_name);
            lastMessage = itemView.findViewById(R.id.chat_last_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            unreadBadge = itemView.findViewById(R.id.chat_unread_badge);
        }
    }
}
