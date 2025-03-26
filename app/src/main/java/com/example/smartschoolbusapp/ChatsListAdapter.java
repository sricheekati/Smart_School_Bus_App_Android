package com.example.smartschoolbusapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatsListAdapter extends RecyclerView.Adapter<ChatsListAdapter.ViewHolder> {

    private List<ChatRoomModel> chatRooms;
    private Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;

    public ChatsListAdapter(List<ChatRoomModel> chatRooms, Context context) {
        this.chatRooms = chatRooms;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.currentUserId = auth.getCurrentUser().getUid();
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
        String receiverId = chatRoom.getUsers().get(0).equals(currentUserId)
                ? chatRoom.getUsers().get(1)
                : chatRoom.getUsers().get(0);

        firestore.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.userName.setText(documentSnapshot.getString("name"));
                    } else {
                        holder.userName.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> holder.userName.setText("Unknown User"));

        String message = chatRoom.getLastMessage();
        holder.lastMessage.setText(message != null ? message : "");

        if (chatRoom.getSeenBy() == null || !chatRoom.getSeenBy().contains(currentUserId)) {
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
        }




        holder.timestamp.setText(formatTimestamp(chatRoom.getTimestamp()));

        // ✅ Set unread badge visibility
        int unreadCount = chatRoom.getUnreadCount();
        if (unreadCount > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(unreadCount));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        // Open chat on click
        holder.itemView.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("receiverId", receiverId);
            chatIntent.putExtra("receiverName", holder.userName.getText().toString());
            //context.startActivity(chatIntent);
            ((AppCompatActivity) context).startActivityForResult(chatIntent, 1001);

        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ✅ ViewHolder with unread badge
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage, timestamp, unreadBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.chat_user_name);
            lastMessage = itemView.findViewById(R.id.chat_last_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            unreadBadge = itemView.findViewById(R.id.chat_unread_badge); // ✅ Connect badge
        }
    }
}
