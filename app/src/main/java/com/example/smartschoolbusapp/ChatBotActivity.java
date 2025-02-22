package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText messageInput;
    private Button sendButton;
    private Map<String, String> predefinedResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // 🔹 Initialize UI Components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // 🔹 Initialize Message List
        messageList = new ArrayList<>();
//        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 🔹 Load Predefined Responses
        loadPredefinedResponses();

        // 🔹 Send Button Click Event
        sendButton.setOnClickListener(v -> {
            String userMessage = messageInput.getText().toString().trim();

            if (!userMessage.isEmpty()) {
                addMessage(userMessage, true);

                // 🔹 Check if predefined response exists
                String botReply = predefinedResponses.getOrDefault(
                        userMessage.toLowerCase(),
                        "Sorry, I don't understand. Try asking something else."
                );

                // 🔹 Add a small delay before the bot responds (Feels more natural)
                new android.os.Handler().postDelayed(() -> addMessage(botReply, false), 500);

                // 🔹 Clear Input Field
                messageInput.setText("");
            } else {
                Toast.makeText(ChatBotActivity.this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Function to Add Message to the List and Refresh UI
    private void addMessage(String text, boolean isUser) {
        if (chatAdapter != null && messageList != null) {
            messageList.add(new Message(text, isUser));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        } else {
            Log.e("ChatBotActivity", "Error: Adapter or Message List is NULL!");
        }
    }

    // ✅ Function to Load Predefined Responses
    private void loadPredefinedResponses() {
        predefinedResponses = new HashMap<>();

        // 🚍 Basic Greetings
        predefinedResponses.put("hello", "Hello! How can I assist you?");
        predefinedResponses.put("hi", "Hi! How can I help?");
        predefinedResponses.put("good morning", "Good morning! Hope you're doing well.");
        predefinedResponses.put("good evening", "Good evening! How can I assist?");

        // 🏫 Bus & Route Related Questions
        predefinedResponses.put("where is the bus?", "The bus is currently en route. You can track the live location via the app.");
        predefinedResponses.put("what is the current location?", "I am currently near [BUS STOP NAME]. The estimated arrival time is in 10 minutes.");
        predefinedResponses.put("is the bus on time?", "Yes, the bus is on schedule. No delays reported.");
        predefinedResponses.put("why is the bus delayed?", "There is some traffic congestion on the route. We are moving as quickly as possible.");
        predefinedResponses.put("how long will it take to reach?", "Approximately 10-15 minutes, depending on traffic.");

        // 🏡 Pickup & Drop-off Related
        predefinedResponses.put("has my child boarded the bus?", "Yes, your child has boarded the bus safely.");
        predefinedResponses.put("has my child reached school?", "Yes, your child has been safely dropped off at the school.");
        predefinedResponses.put("when will my child reach home?", "The estimated arrival at your stop is 4:30 PM.");
        predefinedResponses.put("is there a change in drop-off time?", "Currently, there is no change. Any updates will be notified.");

        // ⚠️ Emergency & Safety Questions
        predefinedResponses.put("is everything okay on the bus?", "Yes, everything is running smoothly.");
        predefinedResponses.put("is my child safe?", "Yes, all students are safe. No issues have been reported.");
        predefinedResponses.put("what if my child misses the bus?", "You can contact the school to arrange alternative transportation.");
        predefinedResponses.put("is there a medical emergency?", "If there’s an emergency, I will inform the school immediately and take necessary actions.");

        // 🏫 School Announcements & Special Cases
        predefinedResponses.put("is the bus running on a holiday?", "The bus will not operate on holidays. Please check the school calendar for details.");
        predefinedResponses.put("will the bus be available for special events?", "Yes, the bus service will be available for school-organized events.");

        // 🔧 Maintenance & Technical Issues
        predefinedResponses.put("is the bus having any issues?", "The bus is running smoothly. No technical problems.");
        predefinedResponses.put("what if the bus breaks down?", "In case of breakdown, parents will be informed, and alternative arrangements will be made.");

        // 🔚 Closing Conversations
        predefinedResponses.put("thank you", "You're welcome! Drive safely.");
        predefinedResponses.put("thanks", "You're welcome! Let me know if you need anything else.");
        predefinedResponses.put("bye", "Goodbye! Have a great day.");

        Log.d("ChatBot", "Predefined responses for parent queries loaded successfully!");
    }
}