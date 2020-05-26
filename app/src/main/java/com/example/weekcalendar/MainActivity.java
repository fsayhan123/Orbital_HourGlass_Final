package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private List<Day> daysOfTheMonth; // to change name
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;
    private FloatingActionButton floatingCreateEvent;
    private FloatingActionButton linkToExpense; // random name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        daysOfTheMonth = prepareMonth(); // should return a list of only days with events
        mRecyclerView = findViewById(R.id.week_view);
        mAdapter = new WeekRecyclerViewAdapter(daysOfTheMonth, this, MainActivity.this);

        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        floatingCreateEvent = findViewById(R.id.create_event);
        floatingCreateEvent.setOnClickListener(v -> createEvent());

        linkToExpense = findViewById(R.id.random_button_link_expense);
        linkToExpense.setOnClickListener(v -> moveToExpense());
    }

    private void moveToExpense() {
        Intent i = new Intent(this, ExpenseTracker.class);
        startActivity(i);
    }

    private void createEvent() {
        Intent intent = new Intent(this, EventCreationPage.class);
        startActivity(intent);
    }

    private List<Day> prepareMonth() {
        // only to add Days where there are events
        daysOfTheMonth = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        daysOfTheMonth.add(new Day(c.getTime()));
        for (int i = 0; i < 30; i++) {
            c.add(Calendar.DATE, 2);
            // if Day has a >= 1 Event, add to list to send to Recycler View
            daysOfTheMonth.add(new Day(c.getTime()));
        }
        return daysOfTheMonth;
    }

    @Override
    public void onDateClickListener(String date) {
        Toast.makeText(this, "clicked " + date, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, EventCreationPage.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }

    @Override
    public void onEventClickListener(String event) {
        Toast.makeText(this, "clicked " + event, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, IndividualEventPage.class);
        i.putExtra("event details", event);
        startActivity(i);
    }


}