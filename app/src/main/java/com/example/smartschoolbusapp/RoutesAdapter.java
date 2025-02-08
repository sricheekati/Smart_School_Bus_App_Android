package com.example.smartschoolbusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    private List<Routes> routes;

    public RoutesAdapter(List<Routes> routes) {
        this.routes = routes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routes route = routes.get(position);
        holder.routeName.setText("Route: " + route.getName());
        holder.startLocation.setText("Start: " + route.getStartLocation());
        holder.endLocation.setText("End: " + route.getEndLocation());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, startLocation, endLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.route_name_text);
            startLocation = itemView.findViewById(R.id.start_location_text);
            endLocation = itemView.findViewById(R.id.end_location_text);
        }
    }
}