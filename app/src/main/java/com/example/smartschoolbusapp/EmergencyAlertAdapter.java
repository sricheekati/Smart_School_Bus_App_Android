package com.example.smartschoolbusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyAlertAdapter extends RecyclerView.Adapter<EmergencyAlertAdapter.AlertViewHolder> {

    private List<EmergencyAlertModel> alertList;

    public EmergencyAlertAdapter(List<EmergencyAlertModel> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        EmergencyAlertModel alert = alertList.get(position);
        holder.alertMessage.setText(alert.getMessage());
        holder.alertSender.setText("Sent by: " + alert.getSenderRole());

        // ✅ Convert timestamp (long) to a readable date format
        String formattedTime = convertTimestampToDate(alert.getTimestamp());
        holder.alertTime.setText("Time: " + formattedTime);
    }

    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp)); // Convert long to Date & format
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView alertMessage, alertSender, alertTime;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            alertMessage = itemView.findViewById(R.id.alertMessage);
            alertSender = itemView.findViewById(R.id.alertSender);
            alertTime = itemView.findViewById(R.id.alertTime);
        }
    }
}