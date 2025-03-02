package com.example.smartschoolbusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    private List<Routes> routes;
    private OnRouteClickListener routeClickListener;

    public interface OnRouteClickListener {
        void onRouteClick(Routes route);
    }

    public RoutesAdapter(List<Routes> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.routeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routes route = routes.get(position);
        holder.routeName.setText(route.getName());
        holder.startLocation.setText(route.getStartLocation());
        holder.endLocation.setText(route.getEndLocation());

        // ✅ Trigger callback when a route is clicked (for Admins)
        holder.itemView.setOnClickListener(v -> {
            if (routeClickListener != null) {
                routeClickListener.onRouteClick(route);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, startLocation, endLocation;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.route_name);
            startLocation = itemView.findViewById(R.id.start_location);
            endLocation = itemView.findViewById(R.id.end_location);
        }
    }
}