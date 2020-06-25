package com.example.weekcalendar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.helperclasses.MyOnEventClickListener;
import com.example.weekcalendar.R;

import java.util.List;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.MyEventViewHolder> {
    private List<CustomEvent> listOfEvents;
    private MyOnEventClickListener mEventClickListener;

    public class MyEventViewHolder extends RecyclerView.ViewHolder {
        private String id;
        private TextView time;
        private TextView eventTitle;
        private LinearLayout eachEventlayout;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            eventTitle = itemView.findViewById(R.id.event_details_toolbar);
            eachEventlayout = itemView.findViewById(R.id.each_event_layout);
        }
    }

    public EventRecyclerViewAdapter(List<CustomEvent> listOfEvents, MyOnEventClickListener eventClicker) {
        this.listOfEvents = listOfEvents;
        this.mEventClickListener = eventClicker;
    }

    @NonNull
    @Override
    public EventRecyclerViewAdapter.MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event, parent, false);
        EventRecyclerViewAdapter.MyEventViewHolder holder = new EventRecyclerViewAdapter.MyEventViewHolder(view);

        holder.eachEventlayout.setOnClickListener(v -> {

        });

        holder.time.setOnClickListener(v -> mEventClickListener.onEventClickListener(holder.id));

        holder.eventTitle.setOnClickListener(v -> mEventClickListener.onEventClickListener(holder.id));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventRecyclerViewAdapter.MyEventViewHolder holder, int position) {
        CustomEvent e = this.listOfEvents.get(position);
        holder.id = e.getId();
        holder.time.setText(e.getStartTime());
        holder.eventTitle.setText(e.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.listOfEvents == null ? 0 : this.listOfEvents.size();
    }
}
