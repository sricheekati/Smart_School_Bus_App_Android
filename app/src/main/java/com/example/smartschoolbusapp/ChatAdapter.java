package com.example.smartschoolbusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages;

    public ChatAdapter(List<Message> messages) {

        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public ChatAdapter(ChatActivity chatActivity, List<ChatMessage> chatMessages) {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.isUser()) {
            holder.userTextView.setText(message.getText());
            holder.userTextView.setVisibility(View.VISIBLE);
            holder.botTextView.setVisibility(View.GONE);
        } else {
            holder.botTextView.setText(message.getText());
            holder.botTextView.setVisibility(View.VISIBLE);
            holder.userTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView, botTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.userMessage);
            botTextView = itemView.findViewById(R.id.botMessage);
        }
    }
}