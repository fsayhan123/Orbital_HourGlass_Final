package com.example.weekcalendar.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.helperclasses.MyOnDateClickListener;
import com.example.weekcalendar.helperclasses.MyOnEventClickListener;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;

import java.util.List;
import java.util.Map;

public class UpcomingRecyclerViewAdapter extends RecyclerView.Adapter<UpcomingRecyclerViewAdapter.MyViewHolder> {

    private List<CustomDay> listOfDates;
    private Map<CustomDay, List<CustomEvent>> mapOfEvents;
    private MyOnDateClickListener mDateClickListener;
    private Activity a;
    private View eachDayView;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private Button date;
        private Button month;
        private RecyclerView dayEvents;

        private MyViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_button);
            month = itemView.findViewById(R.id.month_button);
            dayEvents = itemView.findViewById(R.id.all_events_layout);
        }
    }

    public UpcomingRecyclerViewAdapter(List<CustomDay> listOfDates, Map<CustomDay, List<CustomEvent>> mapOfEvents,
                                       MyOnDateClickListener dateClicker, Activity a) {
        this.listOfDates = listOfDates;
        this.mapOfEvents = mapOfEvents;
        this.mDateClickListener = dateClicker;
        this.a = a;
    }

    @NonNull
    @Override
    public UpcomingRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        eachDayView = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_day, parent, false);
        UpcomingRecyclerViewAdapter.MyViewHolder holder = new MyViewHolder(eachDayView);

        holder.date.setOnClickListener(v -> {
            String day = holder.date.getText().toString();
            String month = holder.month.getText().toString();
            mDateClickListener.onDateClickListener(day + " " + month);
        });

        holder.month.setOnClickListener(v -> {
            String day = holder.date.getText().toString();
            String month = holder.month.getText().toString();
            mDateClickListener.onDateClickListener(day + " " + month);
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UpcomingRecyclerViewAdapter.MyViewHolder holder, int position) {
        final CustomDay d = listOfDates.get(position);
        holder.date.setText(d.getdd().length() == 1 ? "0" + d.getdd() : d.getdd());
        holder.month.setText(d.getMMM());

        EventRecyclerViewAdapter e = new EventRecyclerViewAdapter(this.mapOfEvents.get(d), (MyOnEventClickListener) a); // can store in holder?
        LinearLayoutManager LLM = new LinearLayoutManager(a); // can store in holder?
        holder.dayEvents.setLayoutManager(LLM);
        holder.dayEvents.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.listOfDates == null ? 0 : this.listOfDates.size();
    }
}