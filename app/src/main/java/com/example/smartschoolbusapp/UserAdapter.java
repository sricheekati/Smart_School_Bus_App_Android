package com.example.smartschoolbusapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<UserModel> users;
    private List<UserModel> filteredUsers;
    private Context context;

    public UserAdapter(List<UserModel> users, Context context) {
        this.users = users;
        this.filteredUsers = new ArrayList<>(users);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = filteredUsers.get(position);

        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRole());

        holder.itemView.setOnClickListener(v -> {
            if (user.getUid() != null && !user.getUid().isEmpty()) { // 🔥 Ensure receiverId is valid
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("receiverId", user.getUid());
                chatIntent.putExtra("receiverName", user.getName());
                chatIntent.putExtra("receiverRole", user.getRole());

                // ✅ Debugging: Log receiverId before starting ChatActivity
                System.out.println("Opening ChatActivity with receiverId: " + user.getUid());

                context.startActivity(chatIntent);
            } else {
                Toast.makeText(context, "Error: Receiver ID is missing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    // ✅ Update user list dynamically for search
    public void updateList(List<UserModel> newUsers) {
        filteredUsers.clear();
        filteredUsers.addAll(newUsers);
        notifyDataSetChanged(); // ✅ Refresh RecyclerView
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userRole = itemView.findViewById(R.id.user_role);
        }
    }
}