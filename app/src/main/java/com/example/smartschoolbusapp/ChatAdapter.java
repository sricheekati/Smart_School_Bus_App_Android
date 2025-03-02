package com.example.smartschoolbusapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Constructor for ChatAdapter
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> messages;
    private String currentUserId;

    private FirebaseFirestore firestore;  // Add this line to declare the Firestore variable

    public ChatAdapter(Context context, List<ChatMessage> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        firestore = FirebaseFirestore.getInstance();  // Initialize firestore
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getMessage());

        // Format the timestamp
        String formattedTime = formatTimestamp(message.getTimestamp());
        holder.timestamp.setText(formattedTime);

        // Check if the message is from the current user or the other user
        if (message.getSenderID().equals(currentUserId)) {
            // Sender
            holder.senderName.setText("You");
            holder.messageText.setBackgroundResource(R.drawable.bg_bot_message); // Style sender's messages differently
        } else {
            // Receiver
            // Fetch the sender's name from Firestore
            firestore.collection("users")
                    .document(message.getSenderID())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String senderName = document.getString("name");
                                holder.senderName.setText(senderName != null ? senderName : "Unknown User");
                            } else {
                                holder.senderName.setText("Unknown User");
                            }
                        } else {
                            holder.senderName.setText("Unknown User");
                            Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    });

            holder.messageText.setBackgroundResource(R.drawable.bg_user_message); // Style receiver's messages differently
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            senderName = itemView.findViewById(R.id.senderName);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}