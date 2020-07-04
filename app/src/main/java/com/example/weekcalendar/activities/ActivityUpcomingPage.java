package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
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
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.DAYS;

public class ActivityUpcomingPage extends AppCompatActivity implements MyOnDateClickListener, MyOnEventClickListener {
    private static final String TAG = ActivityUpcomingPage.class.getSimpleName();

    // RecyclerView variables
    private List<CustomDay> listOfDays;
    private Set<CustomDay> setOfDays;
    private Map<CustomDay, List<CustomEvent>> mapOfEvents;
    private RecyclerView mRecyclerView;
    private UpcomingRecyclerViewAdapter mAdapter;

    // Store local copy of events data
    private Map<String, CustomEvent> cache;

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

    // Get data from Google
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

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

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        // logging in to google will also result in this.fAuth.getCurrentUser() != null being true
        if (acct != null) {
            List<Event> fetchedEvents;
            try {
                fetchedEvents = new ActivityUpcomingPage.RequestAuth().execute().get();
                Log.d(TAG, "from google: " + fetchedEvents.isEmpty());
                this.listOfDays = new ArrayList<>();
                this.mapOfEvents = new HashMap<>();
                this.setOfDays = new HashSet<>();
                this.cache = new HashMap<>();
            } catch (Exception e) {
                e.printStackTrace();
                fetchedEvents = new ArrayList<>();
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            for (Event e : fetchedEvents) {
                processGoogleEvent(e);
            }
            Log.d(TAG, "TESTTTTTTT " + listOfDays.isEmpty());
            this.mAdapter = new UpcomingRecyclerViewAdapter(listOfDays, mapOfEvents,
                    ActivityUpcomingPage.this, ActivityUpcomingPage.this);
            this.mRecyclerView.setAdapter(mAdapter);
        } else if (this.fAuth.getCurrentUser() != null){
            this.userID = this.fAuth.getCurrentUser().getUid();
            this.c = this.fStore.collection("events");
            fetchEventsFromTodayFromFirebase();
        } else {
            Toast.makeText(this, "Not logged in to any account!", Toast.LENGTH_SHORT).show();
        }
        //testing firebase dynamic links
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                System.out.println("Hello World");

                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.getLink();
                }

                if (deepLink != null) {
                    String documentID = deepLink.getQueryParameter("id");
                    fStore.collection("events").document(documentID)
                            .update("sharedUserID", FieldValue.arrayUnion(ActivityUpcomingPage.this.userID));
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error unable to retrieve link");
            }
        });
    }

    private void moveToCreateEventPage() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        startActivity(intent);
    }

    private void addToMap(CustomEvent event) {
        Date startD = null;
        try {
            startD = dateFormatter.parse(event.getStartDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(startD);
        if (!this.setOfDays.contains(day)) {
            this.setOfDays.add(day);
            this.listOfDays.add(day);
            List<CustomEvent> temp = new ArrayList<>();
            temp.add(event);
            this.mapOfEvents.put(day, temp);
        } else {
            this.mapOfEvents.get(day).add(event);
        }
        if (this.cache.get(event.getId()) == null) {
            this.cache.put(event.getId(), event);
        }
    }

    private void processFirebaseDocument(QueryDocumentSnapshot document) {
        String title = (String) document.get("eventTitle");
        String startDate = (String) document.get("startDate");
        String endDate = (String) document.get("endDate");
        String startTime = (String) document.get("startTime");
        String endTime = (String) document.get("endTime");
        String docID = document.getId();
        if (startDate.equals(endDate)) { // just one day
            CustomEventFromFirebase event = new CustomEventFromFirebase(title, startDate, endDate, startTime, endTime, docID);
            addToMap(event);
        } else { // > 1 day
            LocalDate first = LocalDate.parse(startDate);
            LocalDate last = LocalDate.parse(endDate);
            long numDays = DAYS.between(first, last);
            for (int i = 0; i <= numDays; i++) {
                String newDate;
                if (i == 0) {
                    newDate = startDate;
                } else {
                    newDate = first.plusDays(i).toString();
                    startTime = "All Day"; // change later to support end time
                }
                CustomEventFromFirebase event = new CustomEventFromFirebase(title, newDate, endDate, startTime, endTime, docID);
                addToMap(event);
            }
        }
    }

    private void processGoogleEvent(Event e) {
        String title = e.getSummary();
        String startDateTime = e.getStart().get("dateTime").toString();
        String[] startDateAndTimeSplit = startDateTime.split("T");
        String startDate = startDateAndTimeSplit[0];
        String startTime = startDateAndTimeSplit[1].substring(0, 5); // getOriginalStart?
        String endDateTime = e.getEnd().get("dateTime").toString();
        String[] endDateAndTimeSplit = endDateTime.split("T");
        String endDate = endDateAndTimeSplit[0];
        String endTime = endDateAndTimeSplit[1].substring(0, 5);
        String eventID = e.getId();
        if (startDate.equals(endDate)) { // just one day
            CustomEventFromGoogle event = new CustomEventFromGoogle(title, startDate, endDate, startTime, endTime, eventID);
            addToMap(event);
        } else { // > 1 day
            LocalDate first = LocalDate.parse(startDate);
            LocalDate last = LocalDate.parse(endDate);
            long numDays = DAYS.between(first, last);
            for (int i = 0; i <= numDays; i++) {
                String newDate;
                if (i == 0) {
                    newDate = startDate;
                } else {
                    newDate = first.plusDays(i).toString();
                    startTime = "All Day"; // change later to support end time
                }
                CustomEventFromFirebase event = new CustomEventFromFirebase(title, newDate, endDate, startTime, endTime, eventID);
                addToMap(event);
                Log.d(TAG, "TESTTTTTTT inside" + listOfDays.isEmpty());
            }
        }
    }

    // Fetches events from Firebase
    private void fetchEventsFromTodayFromFirebase() {
        this.listOfDays = new ArrayList<>();
        this.mapOfEvents = new HashMap<>();
        this.setOfDays = new HashSet<>();
        String today = dateFormatter.format(java.util.Calendar.getInstance().getTime());
        this.c.whereEqualTo("userID", userID)
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
                            cache = new HashMap<>();
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
        i.putExtra("event", this.cache.get(eventId));
        startActivity(i);
    }

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