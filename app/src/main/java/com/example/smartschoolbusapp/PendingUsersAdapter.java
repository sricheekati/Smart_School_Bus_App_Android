package com.example.smartschoolbusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PendingUsersAdapter extends RecyclerView.Adapter<PendingUsersAdapter.ViewHolder> {

    private List<UserModel> pendingUsers;
    private OnUserApprovedListener listener;

    public interface OnUserApprovedListener {
        void onUserApproved(String userId);
        void onUserRejected(String userId);
    }

    public PendingUsersAdapter(List<UserModel> pendingUsers, OnUserApprovedListener listener) {
        this.pendingUsers = pendingUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = pendingUsers.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRole());

        holder.approveButton.setOnClickListener(v -> listener.onUserApproved(user.getUid()));
        holder.rejectButton.setOnClickListener(v -> listener.onUserRejected(user.getUid()));
    }

    @Override
    public int getItemCount() {
        return pendingUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole;
        Button approveButton,rejectButton;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userRole = itemView.findViewById(R.id.user_role);
            approveButton = itemView.findViewById(R.id.approve_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}