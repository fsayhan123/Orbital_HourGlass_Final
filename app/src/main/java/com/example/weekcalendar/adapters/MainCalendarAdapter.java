package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.helperclasses.MyOnEventClickListener;

import java.util.List;

public class MainCalendarAdapter extends RecyclerView.Adapter<MainCalendarAdapter.MyViewHolder> {
    private List<CustomEvent> listOfEvents;
    private MyOnEventClickListener clicker;

    public MainCalendarAdapter(List<CustomEvent> listOfEvents, MyOnEventClickListener clicker) {
        this.listOfEvents = listOfEvents;
        this.clicker = clicker;
    }

    @NonNull
    @Override
    public MainCalendarAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View eachEvent = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event, parent, false);
        MainCalendarAdapter.MyViewHolder holder = new MainCalendarAdapter.MyViewHolder(eachEvent);
        holder.time.setOnClickListener(v -> clicker.onEventClickListener(holder.eventID));
        holder.eventDetails.setOnClickListener(v -> clicker.onEventClickListener(holder.eventID));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainCalendarAdapter.MyViewHolder holder, int position) {
        final CustomEvent e = this.listOfEvents.get(position);
        holder.eventID = e.getId();
        holder.time.setText(e.getStartTime());
        holder.eventDetails.setText(e.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.listOfEvents == null ? 0 : this.listOfEvents.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private String eventID;
        private TextView time;
        private TextView eventDetails;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.time = itemView.findViewById(R.id.time);
            this.eventDetails = itemView.findViewById(R.id.event_details);
        }
    }

    public void changeList(List<CustomEvent> newList) {
        this.listOfEvents.clear();
        this.listOfEvents.addAll(newList);
        notifyDataSetChanged();
    }
}
