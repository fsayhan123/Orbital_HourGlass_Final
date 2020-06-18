package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private static final String TAG = ActivityUpcomingPage.class.getSimpleName();

    // RecyclerView and associated adapter, and List<CustomDay> to populate outer RecyclerView (just the dates)
    private List<CustomDay> listOfDays;
    private Map<CustomDay, List<CustomEvent>> mapOfEvents;
    private RecyclerView mRecyclerView;
    private WeekRecyclerViewAdapter mAdapter;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // FloatingActionButton to link to ActivityCreateEvent
    private FloatingActionButton floatingCreateEvent;

    private SetupNavDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        // Setup link to Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        c = fStore.collection("events");

        DocumentReference docRefUser = fStore.collection("users").document(userID);
        docRefUser.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String username = documentSnapshot.getString("fName");
                Toast.makeText(ActivityUpcomingPage.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
            }
        });

        try {
            fetchEvents();
        } catch (ParseException e) {
            listOfDays = new ArrayList<>();
        }

        mRecyclerView = findViewById(R.id.week_view);

        LinearLayoutManager manager = new LinearLayoutManager(ActivityUpcomingPage.this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

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

    private void fetchEvents() throws ParseException {
        listOfDays = new ArrayList<>();
        mapOfEvents = new HashMap<>();
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
                                listOfDays.add(day);
                                if (mapOfEvents.get(day) == null) {
                                    List<CustomEvent> temp = new ArrayList<>();
                                    temp.add(event);
                                    mapOfEvents.put(day, temp);
                                } else {
                                    mapOfEvents.get(day).add(event);
                                }
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