package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ActivityEventDetailsPage extends AppCompatActivity {

    private DatabaseHelper myDB;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_page);

        myDB = new DatabaseHelper(this);

        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventId");
        Log.d("EventID", eventID);

        eventTitle = findViewById(R.id.event_title);
        eventDate = findViewById(R.id.event_date);
        eventTime = findViewById(R.id.event_time);

        this.setView(eventID);

    }

    public void setView(String eventID) {
        Cursor result = this.myDB.getEventDataByID(eventID);
        if (result.getCount() != 0) {
            result.moveToLast();
            eventTitle.setText(result.getString(5));
            eventDate.setText("Date: " + result.getString(1));
            eventTime.setText("Time: " + result.getString(3));
        }
    }
}
