package com.example.smartschoolbusapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class PendingUsersAdapter extends RecyclerView.Adapter<PendingUsersAdapter.ViewHolder> {

    private List<DocumentSnapshot> pendingUsers;
    private Context context;
    private AdminDashboardActivity adminActivity;

    public PendingUsersAdapter(List<DocumentSnapshot> pendingUsers, AdminDashboardActivity adminActivity) {
        this.pendingUsers = pendingUsers;
        this.context = adminActivity;
        this.adminActivity = adminActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot user = pendingUsers.get(position);
        String userId = user.getId();
        String name = user.getString("name");
        String email = user.getString("email");
        String role = user.getString("role");

        holder.userName.setText(name);
        holder.userEmail.setText(email);
        holder.userRole.setText(role);

        holder.approveButton.setOnClickListener(v -> adminActivity.approveUser(userId));
    }

    @Override
    public int getItemCount() {
        return pendingUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole;
        Button approveButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userRole = itemView.findViewById(R.id.user_role);
            approveButton = itemView.findViewById(R.id.approve_button);
        }
    }
}