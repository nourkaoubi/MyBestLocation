package com.example.mybestlocation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList;
    private OnLocationActionListener onLocationActionListener;

    public LocationAdapter(List<Location> locationList, OnLocationActionListener onLocationActionListener) {
        this.locationList = locationList;
        this.onLocationActionListener = onLocationActionListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        holder.pseudoTextView.setText(location.getPseudo());

        // Set up the Edit button action
        holder.editButton.setOnClickListener(v -> {
            if (onLocationActionListener != null) {
                onLocationActionListener.onEditLocation(location);
            }
        });

        // Set up the Delete button action
        holder.deleteButton.setOnClickListener(v -> {
            if (onLocationActionListener != null) {
                onLocationActionListener.onDeleteLocation(location);
            }
        });

        // Set up the Send SMS button action
        holder.sendSmsButton.setOnClickListener(v -> {
            if (onLocationActionListener != null) {
                onLocationActionListener.onSendSms(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList != null ? locationList.size() : 0;
    }

    public void updateLocations(List<Location> newLocationList) {
        this.locationList = newLocationList;
        notifyDataSetChanged();
    }

    public interface OnLocationActionListener {
        void onEditLocation(Location location); // Edit the location
        void onDeleteLocation(Location location); // Delete the location
        void onSendSms(Location location); // Send an SMS with the location
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, pseudoTextView;
        Button editButton, deleteButton, sendSmsButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            pseudoTextView = itemView.findViewById(R.id.text_view_location_pseudo);
            editButton = itemView.findViewById(R.id.button_edit_location);
            deleteButton = itemView.findViewById(R.id.button_delete_location);
            sendSmsButton = itemView.findViewById(R.id.button_send_sms); // New "Send SMS" button
        }
    }
}
