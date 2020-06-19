package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityEventDetailsPage extends AppCompatActivity {

    private DatabaseHelper myDB;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private String eventFieldID;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_page);

        myDB = new DatabaseHelper(this);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("events");

        // Get Intent of the event item selected
        Intent intent = getIntent();
        CustomEvent event = intent.getParcelableExtra("event");

        // Links to XML
        eventTitle = findViewById(R.id.event_title);
        eventDate = findViewById(R.id.event_date);
        eventTime = findViewById(R.id.event_time);

        this.setView(event);

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.event_details_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityUpcomingPage.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.three_dot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void setView(CustomEvent event) {
        eventTitle.setText(event.getTitle());
        String startDate = HelperMethods.formatDateForView(event.getStartDate());
        String endDate = HelperMethods.formatDateForView(event.getEndDate());
        if (startDate.equalsIgnoreCase(endDate)) {
            eventDate.setText("Date: " + startDate);
        } else {
            eventDate.setText("Date: " + startDate + " to " + endDate);
        }
        eventTime.setText("Time: " + event.getStartTime() + " to " + event.getEndTime());
    }

    private void deleteEvent(View view) {
        myDB.deleteEvent(this.eventFieldID);
        Intent intent = new Intent(this, ActivityUpcomingPage.class);
        startActivity(intent);
    }
}
