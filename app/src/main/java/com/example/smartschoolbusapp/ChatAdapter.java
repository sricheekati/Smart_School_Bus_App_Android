package com.example.smartschoolbusapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> chatMessages;
    private String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        if (message.getSenderID().equals(currentUserId)) {
            holder.senderMessage.setText(message.getMessage());
            holder.senderMessage.setVisibility(View.VISIBLE);
            holder.receiverMessage.setVisibility(View.GONE);
        } else {
            holder.receiverMessage.setText(message.getMessage());
            holder.receiverMessage.setVisibility(View.VISIBLE);
            holder.senderMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessage, receiverMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.sender_message);
            receiverMessage = itemView.findViewById(R.id.receiver_message);
        }
    }
}