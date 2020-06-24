package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityEventDetailsPage extends AppCompatActivity {
    private static final String TAG = ActivityEventDetailsPage.class.getSimpleName();

    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;

    private CustomEventFromFirebase event;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("events");

        // Get Intent of the event item selected
        Intent intent = getIntent();
        this.event = intent.getParcelableExtra("event");

        // Links to XML
        this.eventTitle = findViewById(R.id.event_title);
        this.eventDate = findViewById(R.id.event_date);
        this.eventTime = findViewById(R.id.event_time);

        this.setView(this.event);

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
        if (item.getItemId() == R.id.edit_event_topR) {
            editEvent();
        } else if (item.getItemId() == R.id.delete_event_topR) {
            deleteEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setView(CustomEventFromFirebase event) {
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

    private void editEvent() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        intent.putExtra("event to edit", this.event);
        startActivity(intent);
    }

    private void deleteEvent() {
        this.c.document(this.event.getId())
                .delete()
                .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        Intent intent = new Intent(this, ActivityUpcomingPage.class);
        startActivity(intent);
    }
}
