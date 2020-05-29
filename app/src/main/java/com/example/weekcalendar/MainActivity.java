package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private List<Day> daysOfTheMonth; // to change name
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;
    private FloatingActionButton floatingCreateEvent;
    private FloatingActionButton linkToExpense; // random name
    private Button randomButton;
    private DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDB = new DatabaseHelper(this);
        SQLiteDatabase db = this.myDB.getWritableDatabase();

        try {
            daysOfTheMonth = prepareMonth();
        }
        catch(ParseException e) {
            Log.d("hello", "Hello");
            daysOfTheMonth = new ArrayList<Day>();
        }// should return a list of only days with events

        mRecyclerView = findViewById(R.id.week_view);
        mAdapter = new WeekRecyclerViewAdapter(daysOfTheMonth, this, MainActivity.this, getApplicationContext());

        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        floatingCreateEvent = findViewById(R.id.create_event);
        floatingCreateEvent.setOnClickListener(v -> createEvent());

        linkToExpense = findViewById(R.id.random_button_link_expense);
        linkToExpense.setOnClickListener(v -> moveToExpense());

        randomButton = findViewById(R.id.test_button);
        randomButton.setOnClickListener(v -> moveToDrawer());
    }

    private void moveToDrawer() {
        Intent i = new Intent(this,  testDrawer.class);
        startActivity(i);
    }

    private void moveToExpense() {
        Intent i = new Intent(this,  ExpenseHomePage.class);
        startActivity(i);
    }

    private void createEvent() {
        Intent intent = new Intent(this, EventCreationPage.class);
        startActivity(intent);
    }

    /*private List<Day> prepareMonth() {
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
    }*/

    private List<Day> prepareMonth() throws ParseException {
        daysOfTheMonth = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
        Cursor query = this.myDB.getEventData();
        if (query.getCount() == 0) {
            Toast.makeText(this, "empty", Toast.LENGTH_SHORT).show();
            return daysOfTheMonth;
        }

        for (int i =0; i < 30; i++) {
            if (i >= query.getCount()) {
                break;
            }
            query.moveToNext();
            String result = query.getString(1);
            Date date = dateFormatter.parse(result);
            Day day = new Day(date);
            if (daysOfTheMonth.contains(day)) {
                continue;
            } else{
                daysOfTheMonth.add(day);
            }
        } return daysOfTheMonth;
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