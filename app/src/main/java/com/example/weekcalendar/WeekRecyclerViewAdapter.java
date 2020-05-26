package com.example.weekcalendar;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WeekRecyclerViewAdapter extends RecyclerView.Adapter<WeekRecyclerViewAdapter.MyViewHolder> {

    private List<Day> listOfDates;
    private MyOnDateClickListener mDateClickListener;
    private Activity a;
    private View eachDayView;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private Button date;
        private Button month;
        private RecyclerView dayEvents;

        private LinearLayout.LayoutParams timeLayoutFormat;
        private LinearLayout.LayoutParams lastTimeLayoutFormat;
        private LinearLayout.LayoutParams eventLayoutFormat;

        private MyViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_button);
            month = itemView.findViewById(R.id.month_button);
            dayEvents = itemView.findViewById(R.id.all_events_layout);

            timeLayoutFormat = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            timeLayoutFormat.setMargins(10, 0, 0, 50);

            lastTimeLayoutFormat = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            lastTimeLayoutFormat.setMargins(10, 0, 0, 0);

            eventLayoutFormat = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 100f);
            eventLayoutFormat.setMargins(5, 0, 0, 0);

//            eventLayout = eachDayView.findViewById(R.id.all_events_layout);
        }
    }

    public WeekRecyclerViewAdapter(List<Day> list, MyOnDateClickListener dateClicker, Activity a) {
        this.listOfDates = list;
        this.mDateClickListener = dateClicker;
        this.a = a;
    }

    @NonNull
    @Override
    public WeekRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        eachDayView = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_day, parent, false);
        WeekRecyclerViewAdapter.MyViewHolder holder = new MyViewHolder(eachDayView);

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
    public void onBindViewHolder(@NonNull final WeekRecyclerViewAdapter.MyViewHolder holder, int position) {
        final Day d = listOfDates.get(position);
        holder.date.setText(d.getdd().length() == 1 ? "0" + d.getdd() : d.getdd());
        holder.month.setText(d.getMMM());

        // random events
        List<MyEvent> temp = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            temp.add(new MyEvent("Event fjdsfjdksfjdfkldjsfkldjsklfdjsfkldjsfkldsjfdkljsfdkljsfds" + i, "07.30pm", ""));
        }

        EventRecylerViewAdapter e = new EventRecylerViewAdapter(temp, (MyOnEventClickListener) a);
        LinearLayoutManager LLM = new LinearLayoutManager(a);
        holder.dayEvents.setLayoutManager(LLM);
        holder.dayEvents.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.listOfDates == null ? 0 : this.listOfDates.size();
    }
}