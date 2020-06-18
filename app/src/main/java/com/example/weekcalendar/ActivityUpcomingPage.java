package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private static final String TAG = ActivityUpcomingPage.class.getSimpleName();

    // RecyclerView variables
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<CustomEvent>> mapOfEvents;
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // FloatingActionButton to link to ActivityCreateEvent
    private FloatingActionButton floatingCreateEvent;

    // Set up navigation drawer
    private SetupNavDrawer navDrawer;

    // To transform String to Date
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("events");

        // Fetches data from Firebase
        fetchEvents();

        // Links to XML
        this.mRecyclerView = findViewById(R.id.week_view);
        LinearLayoutManager manager = new LinearLayoutManager(ActivityUpcomingPage.this);
        this.mRecyclerView.setHasFixedSize(true);
        this.mRecyclerView.setLayoutManager(manager);

        this.floatingCreateEvent = findViewById(R.id.create_event);
        this.floatingCreateEvent.setOnClickListener(v -> moveToCreateEventPage());

        // Set up navigation drawer
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.upcoming_toolbar));
        this.navDrawer.setupNavDrawerPane();
    }

    private void moveToCreateEventPage() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        startActivity(intent);
    }

    private void processDocument(QueryDocumentSnapshot document) {
        String title = (String) document.get("eventTitle");
        String date = (String) document.get("startDate");
        String time = (String) document.get("startTime");
        CustomEvent event = new CustomEvent(title, date, time);
        Date d = null;
        try {
            d = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(d);
        if (!this.setOfDays.contains(day)) {
            this.setOfDays.add(day);
            this.listOfDays.add(day);
            List<CustomEvent> temp = new ArrayList<>();
            temp.add(event);
            this.mapOfEvents.put(day, temp);
        } else {
            this.mapOfEvents.get(day).add(event);
        }
    }

    // Fetches events from Firebase
    private void fetchEvents() {
        this.listOfDays = new ArrayList<>();
        this.mapOfEvents = new HashMap<>();
        this.setOfDays = new HashSet<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        c.whereEqualTo("userID", userID)
                .orderBy("startDate")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
                            }
                            mAdapter = new WeekRecyclerViewAdapter(listOfDays, mapOfEvents,
                                    ActivityUpcomingPage.this, ActivityUpcomingPage.this);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "hello!!!!!!!!!!!!!!!!!!! " + e.getLocalizedMessage());
                    }
                });
    }

    // To link to ActivityCreateEventPage upon clicking a date in the RecyclerView
    @Override
    public void onDateClickListener(String date) {
        Intent i = new Intent(this, ActivityCreateEventPage.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }

    // To link to ActivityEventDetailsPage upon clicking an event in the RecyclerView
    @Override
    public void onEventClickListener(String eventId) {
        Intent i = new Intent(this, ActivityEventDetailsPage.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
    }
}