package com.example.weekcalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventRecylerViewAdapter extends RecyclerView.Adapter<EventRecylerViewAdapter.MyEventViewHolder> {
    private List<MyEvent> listOfEvents;
    private MyOnEventClickListener mEventClickListener;

    public class MyEventViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private TextView eventTitle;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            eventTitle = itemView.findViewById(R.id.event_details);
        }
    }

    public EventRecylerViewAdapter(List<MyEvent> listOfEvents, MyOnEventClickListener eventClicker) {
        this.listOfEvents = listOfEvents;
        this.mEventClickListener = eventClicker;
    }

    @NonNull
    @Override
    public EventRecylerViewAdapter.MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event, parent, false);
        EventRecylerViewAdapter.MyEventViewHolder holder = new EventRecylerViewAdapter.MyEventViewHolder(view);

        holder.time.setOnClickListener(v -> {
            String time = holder.time.getText().toString();
            String event = holder.eventTitle.getText().toString();
            mEventClickListener.onEventClickListener(time + " " + event);
        });

        holder.eventTitle.setOnClickListener(v -> {
            String time = holder.time.getText().toString();
            String event = holder.eventTitle.getText().toString();
            mEventClickListener.onEventClickListener(time + " " + event);
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventRecylerViewAdapter.MyEventViewHolder holder, int position) {
        MyEvent e = this.listOfEvents.get(position);
        holder.time.setText(e.getDate());
        holder.eventTitle.setText(e.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.listOfEvents == null ? 0 : this.listOfEvents.size();
    }
}
