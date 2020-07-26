package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.customclasses.event.CustomEventFromGoogle;
import com.example.weekcalendar.helperclasses.MyOnDateClickListener;
import com.example.weekcalendar.helperclasses.MyOnEventClickListener;
import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.example.weekcalendar.adapters.UpcomingRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomDay;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityUpcomingPage.class.getSimpleName();

    /**
     * UI variables
     */
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<CustomEvent>> mapOfEvents;
    private RecyclerView mRecyclerView;
    private UpcomingRecyclerViewAdapter mAdapter;

    /**
     * Firebase information
     */
    private String userID;
    private CollectionReference c;

    /**
     * Google information
     */
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Converts Strings to Dates and vice versa
     */
    @SuppressLint("SimpleDateFormat")
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Sets up ActivityUpcomingPage when it is opened.
     * First, sets up Firebase or Google account.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data by calling respective methods depending on the type of account.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        setupXMLItems();

        if (acct != null) {
            prepareGoogleInterface();
        } else if (fAuth.getCurrentUser() != null){
            this.userID = fAuth.getCurrentUser().getUid();
            this.c = fStore.collection("events");
            fetchEventsFromTodayFromFirebase();
        } else {
            Toast.makeText(this, "Not logged in to any account!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up layout for ActivityUpcomingPage.
     */
    private void setupXMLItems() {
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.upcoming_toolbar));
        navDrawer.setupNavDrawerPane();

        this.mRecyclerView = findViewById(R.id.week_view);
        LinearLayoutManager manager = new LinearLayoutManager(ActivityUpcomingPage.this);
        this.mRecyclerView.setHasFixedSize(true);
        this.mRecyclerView.setLayoutManager(manager);

        FloatingActionButton floatingCreateEvent = findViewById(R.id.create_event);
        floatingCreateEvent.setOnClickListener(v -> moveToCreateEventPage());
    }

    /**
     * Sets up interface by populating views with data fetched from Google Calendar.
     */
    private void prepareGoogleInterface() {
        List<Event> fetchedEvents;
        try {
            fetchedEvents = new ActivityUpcomingPage.RequestAuth().execute().get();
            this.listOfDays = new ArrayList<>();
            this.mapOfEvents = new HashMap<>();
            this.setOfDays = new HashSet<>();
        } catch (Exception e) {
            e.printStackTrace();
            fetchedEvents = new ArrayList<>();
            Log.d(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
        }
        for (Event e : fetchedEvents) {
            processGoogleEvent(e);
        }
        this.mAdapter = new UpcomingRecyclerViewAdapter(listOfDays, mapOfEvents,
                ActivityUpcomingPage.this, ActivityUpcomingPage.this);
        this.mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Links to ActivityCreateEventPage when button is clicked.
     */
    private void moveToCreateEventPage() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        startActivity(intent);
    }

    /**
     * Stores details of to do item in a Map. Used to display details in view adapters.
     * @param event CustomEvent encapsulating details required to display
     */
    private void addToMap(CustomEvent event) {
        Date startD = null;
        try {
            startD = dateFormatter.parse(event.getStartDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(Objects.requireNonNull(startD));
        if (!this.setOfDays.contains(day)) {
            this.setOfDays.add(day);
            this.listOfDays.add(day);
            List<CustomEvent> temp = new ArrayList<>();
            temp.add(event);
            this.mapOfEvents.put(day, temp);
        } else {
            Objects.requireNonNull(this.mapOfEvents.get(day)).add(event);
        }
    }

    /**
     * Processes Firebase document from events collection into a CustomEventFromFirebase.
     * @param document Firebase document from events collection
     */
    private void processFirebaseDocument(QueryDocumentSnapshot document) {
        String title = (String) document.get("eventTitle");
        String startDate = (String) document.get("startDate");
        String endDate = (String) document.get("endDate");
        String startTime = (String) document.get("startTime");
        String endTime = (String) document.get("endTime");
        String description = (String) document.get("description");
        String docID = document.getId();
        if (startDate.equals(endDate)) {
            CustomEventFromFirebase event = new CustomEventFromFirebase(title, startDate, endDate, startTime, endTime, docID);
            event.setDescription(description);
            addToMap(event);
        } else {
            LocalDate first = LocalDate.parse(startDate);
            LocalDate last = LocalDate.parse(endDate);
            long numDays = DAYS.between(first, last);
            for (int i = 0; i <= numDays; i++) {
                String newDate;
                if (i == 0) {
                    newDate = startDate;
                } else {
                    newDate = first.plusDays(i).toString();
                    startTime = "All Day";
                }
                CustomEventFromFirebase event = new CustomEventFromFirebase(title, newDate, endDate, startTime, endTime, docID);
                event.setDescription(description);
                addToMap(event);
            }
        }
    }

    /**
     * Processes Google Calendar Event into a CustomEventFromGoogle.
     * @param e Google Calendar Event to be converted
     */
    private void processGoogleEvent(com.google.api.services.calendar.model.Event e) {
        String title = e.getSummary();
        String startDate;
        String startTime;
        String endDate;
        String endTime;
        String eventID = e.getId();
        String eventDescription = e.getDescription() == null ? "" : e.getDescription();
        CustomEvent event;
        if (e.getStart().get("dateTime") == null) {
            startDate = Objects.requireNonNull(e.getStart().get("date")).toString();
            startTime = "All day";
            endDate = startDate;
            endTime = "23:59";
        } else {
            String[] startDateAndTimeSplit = Objects.requireNonNull(e.getStart().get("dateTime")).toString().split("T");
            startDate = startDateAndTimeSplit[0];
            startTime = startDateAndTimeSplit[1].substring(0, 5);
            String[] endDateAndTimeSplit = Objects.requireNonNull(e.getEnd().get("dateTime")).toString().split("T");
            endDate = endDateAndTimeSplit[0];
            endTime = endDateAndTimeSplit[1].substring(0, 5);
        }
        if (startDate.equals(endDate)) {
            event = new CustomEventFromGoogle(title, startDate, endDate, startTime, endTime, eventID);
            event.setDescription(eventDescription);
            addToMap(event);
        } else {
            LocalDate first = LocalDate.parse(startDate);
            LocalDate last = LocalDate.parse(endDate);
            long numDays = DAYS.between(first, last);
            for (int i = 0; i <= numDays; i++) {
                String newDate;
                if (i == 0) {
                    newDate = startDate;
                } else {
                    newDate = first.plusDays(i).toString();
                    startTime = "All Day";
                }
                event = new CustomEventFromGoogle(title, newDate, endDate, startTime, endTime, eventID);
                event.setDescription(eventDescription);
                addToMap(event);
            }
        }
    }

    /**
     * Queries event data from Firebase events collection, with event dates greater than or equal to today.
     */
    private void fetchEventsFromTodayFromFirebase() {
        this.listOfDays = new ArrayList<>();
        this.mapOfEvents = new HashMap<>();
        this.setOfDays = new HashSet<>();
        String today = dateFormatter.format(java.util.Calendar.getInstance().getTime());
        this.c.whereArrayContains("participants", userID)
                .whereGreaterThanOrEqualTo("startDate", today)
                .orderBy("startDate")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            Log.d(TAG, "onSuccess: LIST NOT EMPTY");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processFirebaseDocument(document);
                            }
                            mAdapter = new UpcomingRecyclerViewAdapter(listOfDays, mapOfEvents,
                                    ActivityUpcomingPage.this, ActivityUpcomingPage.this);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    }
                });
    }

    /**
     * Links to ActivityCreateEventPage where date is set to the date clicked.
     * @param date date clicked
     */
    @Override
    public void onDateClickListener(String date) {
        Intent i = new Intent(this, ActivityCreateEventPage.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }

    /**
     * Links to ActivityEventDetailsPage to display details of event clicked.
     * @param eventId String event ID of event clicked
     */
    @Override
    public void onEventClickListener(String eventId) {
        Intent i = new Intent(this, ActivityEventDetailsPage.class);
        i.putExtra("eventID", eventId);
        startActivity(i);
    }

    /**
     * Google Calendar API handler class for asynchronous task handling.
     */
    @SuppressLint("StaticFieldLeak")
    private class RequestAuth extends AsyncTask<String, Void, List<Event>> {
        private List<Event> myList;

        @Override
        protected List<Event> doInBackground(String... strings) {
            try {
                pullData();
                return myList;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void pullData() throws IOException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = service.events().list("primary")
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            myList = events.getItems();
        }

        /**
         * Creates an authorized Credential object.
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
            // Load client secrets.
            InputStream in = ActivityUpcomingPage.this.getAssets().open(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            File tokenFolder = new File(ActivityUpcomingPage.this.getFilesDir(), "tokens");

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(tokenFolder))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver();
            AuthorizationCodeInstalledApp ab = new AuthorizationCodeInstalledApp(flow, receiver){
                @Override
                protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                    String url = (authorizationUrl.build());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            };
            return ab.authorize("user");
        }
    }
}