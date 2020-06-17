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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private static final String TAG = ActivityUpcomingPage.class.getSimpleName();

    // RecyclerView and associated adapter, and List<CustomDay> to populate outer RecyclerView (just the dates)
    private List<CustomDay> daysWithEvents;
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // FloatingActionButton to link to ActivityCreateEvent
    private FloatingActionButton floatingCreateEvent;

    // Database handler
    private DatabaseHelper myDB;

    private SetupNavDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        myDB = new DatabaseHelper(this);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        c = fStore.collection("events");

        // reference to document "users" in the database
        DocumentReference docRefUser = fStore.collection("users").document(userID);
        docRefUser.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                // accessing via key value pairs
                String username = documentSnapshot.getString("fName");
                Toast.makeText(ActivityUpcomingPage.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
            }
        });

        // reference to document "events" in the database
        try {
            Toast.makeText(this, "before fetch", Toast.LENGTH_SHORT).show();
            daysWithEvents = fetchDaysWithEvents();
            //daysWithEvents.sort((d1, d2) -> d1.compareTo(d2));
            Toast.makeText(this, "after fetch", Toast.LENGTH_SHORT).show();
        }
        catch(ParseException e) {
            Log.d("hello", "Hello");
            daysWithEvents = new ArrayList<>();
        }
        Toast.makeText(this, Arrays.toString(daysWithEvents.toArray()), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, Integer.toString(daysWithEvents.size()), Toast.LENGTH_SHORT).show();

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
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        Cursor query = this.myDB.getEventData();
//        if (query.getCount() == 0) {
////            Toast.makeText(this, "empty", Toast.LENGTH_SHORT).show();
//            return daysWithEvents;
//        }
//
//        for (int i = 0; i < 30; i++) { // further: fetch on demand
//            if (i >= query.getCount()) {
//                break;
//            }
//            query.moveToNext();
//            String result = query.getString(0);
//            Date date = dateFormatter.parse(result);
//            CustomDay customDay = new CustomDay(date);
//            daysWithEvents.add(customDay);
//        }
        Toast.makeText(this, "before firebase", Toast.LENGTH_SHORT).show();
        c.whereEqualTo("userID", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = (String) document.get("startDate");
                            Date d = null;
                            try {
                                d = dateFormatter.parse(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            CustomDay day = new CustomDay(d);
                            Toast.makeText(this, day.toString(), Toast.LENGTH_SHORT).show();
                            daysWithEvents.add(day);
                            Toast.makeText(this, Integer.toString(daysWithEvents.size()), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        Toast.makeText(this, "hi" + Integer.toString(daysWithEvents.size()), Toast.LENGTH_SHORT).show();
        return daysWithEvents;
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
    public void onEventClickListener(String eventId) {
        Toast.makeText(this, "clicked " + eventId, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ActivityEventDetailsPage.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
    }
}