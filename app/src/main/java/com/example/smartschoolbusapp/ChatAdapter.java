package com.example.smartschoolbusapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatItem> chatItems;
    private String currentUserId;
    private FirebaseFirestore firestore;

    private static final int VIEW_TYPE_MESSAGE = 0;
    private static final int VIEW_TYPE_DATE_HEADER = 1;

    public ChatAdapter(Context context, List<ChatItem> chatItems, String currentUserId) {
        this.context = context;
        this.chatItems = chatItems;
        this.currentUserId = currentUserId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false);
            return new DateViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);
        if (item.getType() == VIEW_TYPE_DATE_HEADER) {
            ((DateViewHolder) holder).dateText.setText(((DateHeader) item).getDate());
        } else {
            ChatMessage message = (ChatMessage) item;
            ((MessageViewHolder) holder).bind(message);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestamp;
        ImageView statusIcon;
        LinearLayout messageContainer, statusContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            messageContainer = itemView.findViewById(R.id.message_container);
            statusContainer = itemView.findViewById(R.id.status_container);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            timestamp.setText(formatTimestamp(message.getTimestamp()));

            // Adjust layout_gravity of the statusContainer dynamically
            LinearLayout.LayoutParams statusParams = (LinearLayout.LayoutParams) statusContainer.getLayoutParams();

            if (message.getSenderID().equals(currentUserId)) {
                // Sender (Right side)
                messageContainer.setGravity(Gravity.END);
                statusParams.gravity = Gravity.END;
                statusContainer.setLayoutParams(statusParams);

                messageText.setBackgroundResource(R.drawable.bg_bot_message);
                messageText.setTextColor(context.getResources().getColor(android.R.color.black));

                statusIcon.setVisibility(View.VISIBLE);
                if (message.isSeen()) {
                    statusIcon.setImageResource(R.drawable.ic_double_tick_blue);
                } else {
                    statusIcon.setImageResource(R.drawable.ic_double_tick_gray);
                }
            } else {
                // Receiver (Left side)
                messageContainer.setGravity(Gravity.START);
                statusParams.gravity = Gravity.START;
                statusContainer.setLayoutParams(statusParams);

                messageText.setBackgroundResource(R.drawable.bg_user_message);
                messageText.setTextColor(context.getResources().getColor(android.R.color.black));
                statusIcon.setVisibility(View.GONE);
            }
        }
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
        }
    }
}