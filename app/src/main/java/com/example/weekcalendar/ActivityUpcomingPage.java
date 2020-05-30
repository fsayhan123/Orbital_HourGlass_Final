package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    // RecyclerView and associated adapter, and List<CustomDay> to populate outer RecyclerView (just the dates)
    private List<CustomDay> daysWithEvents;
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;

    // FloatingActionButton to link to ActivityCreateEvent
    private FloatingActionButton floatingCreateEvent;

    // Database handler
    private DatabaseHelper myDB;

    // Navigation drawer pane
//    private DrawerLayout dl;
//    private ActionBarDrawerToggle t;
//    private NavigationView nv;
    private SetupNavDrawer navDrawer;

    // to be edited
    private FloatingActionButton linkToExpense;
    private Button randomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);
        myDB = new DatabaseHelper(this);

        try {
            daysWithEvents = fetchDaysWithEvents();
        }
        catch(ParseException e) {
            Log.d("hello", "Hello");
            daysWithEvents = new ArrayList<>();
        }

        mRecyclerView = findViewById(R.id.week_view);
        mAdapter = new WeekRecyclerViewAdapter(daysWithEvents, this, ActivityUpcomingPage.this, getApplicationContext());

        LinearLayoutManager manager = new LinearLayoutManager(ActivityUpcomingPage.this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        floatingCreateEvent = findViewById(R.id.create_event);
        floatingCreateEvent.setOnClickListener(v -> moveToCreateEventPage());

        // Navigation pane drawer setup
        navDrawer = new SetupNavDrawer(this, findViewById(R.id.upcoming_toolbar));
        navDrawer.setupNavDrawerPane();
    }

    private void moveToCreateEventPage() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        startActivity(intent);
    }

    private List<CustomDay> fetchDaysWithEvents() throws ParseException {
        daysWithEvents = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
        Cursor query = this.myDB.getEventData();
        if (query.getCount() == 0) {
            Toast.makeText(this, "empty", Toast.LENGTH_SHORT).show();
            return daysWithEvents;
        }
        for (int i = 0; i < 30; i++) { // further: fetch on demand
            if (i >= query.getCount()) {
                break;
            }
            query.moveToNext();
            String result = query.getString(1);
            Date date = dateFormatter.parse(result);
            CustomDay customDay = new CustomDay(date);
            if (!daysWithEvents.contains(customDay)) {
                daysWithEvents.add(customDay);
            }
        } return daysWithEvents;
    }

    // To link to ActivityCreateEventPage upon clicking a date in the RecyclerView
    @Override
    public void onDateClickListener(String date) {
        Toast.makeText(this, "clicked " + date, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ActivityCreateEventPage.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }

    // To link to ActivityEventDetailsPage upon clicking an event in the RecyclerView
    @Override
    public void onEventClickListener(String event) {
        Toast.makeText(this, "clicked " + event, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ActivityEventDetailsPage.class);
        i.putExtra("event details", event);
        startActivity(i);
    }
}