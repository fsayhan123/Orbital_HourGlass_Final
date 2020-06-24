package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
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
import java.security.GeneralSecurityException;
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
    private Map<CustomDay, List<CustomEventFromFirebase>> mapOfEvents;
    private RecyclerView mRecyclerView;
    private UpcomingRecyclerViewAdapter mAdapter;

    // Store local copy of events data
    private Map<String, CustomEventFromFirebase> cache;

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

    // Get dataa from Google
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        if (this.fAuth.getCurrentUser() != null) {
            this.userID = this.fAuth.getCurrentUser().getUid();
            this.c = this.fStore.collection("events");
            fetchEvents();
        }
        else {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

            if (acct != null) {
                List<Event> fetchedEvents;
                try {
                    fetchedEvents = new ActivityUpcomingPage.RequestAuth().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    fetchedEvents = new ArrayList<>();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fetchedEvents = new ArrayList<>();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                for (Event e : fetchedEvents) {
                    try {
                        Toast.makeText(this, e.getStart().toPrettyString() + " " + e.getEnd().toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(this, "Not logged in to any account!", Toast.LENGTH_SHORT).show();
            }
        }

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

    private void eventToCustomEvent(Event e) {
//        return new CustomEventFromFirebase();
    }

    private void moveToCreateEventPage() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        startActivity(intent);
    }

    private void addToMap(CustomEventFromFirebase event) {
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
            List<CustomEventFromFirebase> temp = new ArrayList<>();
            temp.add(event);
            this.mapOfEvents.put(day, temp);
        } else {
            this.mapOfEvents.get(day).add(event);
        }
        if (this.cache.get(event.getId()) == null) {
            this.cache.put(event.getId(), event);
        }
    }

    private void processDocument(QueryDocumentSnapshot document) {
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

    // Fetches events from Firebase
    private void fetchEvents() {
        this.listOfDays = new ArrayList<>();
        this.mapOfEvents = new HashMap<>();
        this.setOfDays = new HashSet<>();
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
                            cache = new HashMap<>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
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
                test();
                return myList;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            return null;
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

            if (!tokenFolder.exists()) {
//                Toast.makeText(FetchGoogleCalendarEvents.this, "folder does not exist", Toast.LENGTH_SHORT).show();
                tokenFolder.setWritable(true, true);
                tokenFolder.setReadable(true, true);
                tokenFolder.setExecutable(true, false);
                boolean b = tokenFolder.mkdirs();
//                Toast.makeText(FetchGoogleCalendarEvents.this, "created directory is " + b, Toast.LENGTH_SHORT).show();
            }

            // https://stackoverflow.com/questions/59925288/filedatastorefactory-and-posix-on-android

            FileDataStoreFactory temp;
            try {
                tokenFolder.setWritable(true);
                tokenFolder.setReadable(true);
                tokenFolder.setExecutable(true);
                temp = new FileDataStoreFactory((tokenFolder));
            } catch (IOException e) {
//                Toast.makeText(FetchGoogleCalendarEvents.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                Toast.makeText(FetchGoogleCalendarEvents.this, Boolean.toString(tokenFolder.mkdirs()), Toast.LENGTH_SHORT).show();
                temp = new FileDataStoreFactory((tokenFolder));
            }

//            Toast.makeText(FetchGoogleCalendarEvents.this, "datastore created", Toast.LENGTH_SHORT).show();

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(temp)
                    .setAccessType("offline")
                    .build();
//            Toast.makeText(FetchGoogleCalendarEvents.this, "flowing", Toast.LENGTH_SHORT).show();
            LocalServerReceiver receiver = new LocalServerReceiver();
            AuthorizationCodeInstalledApp ab = new AuthorizationCodeInstalledApp(flow, receiver){
                @Override
                protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
//                    Toast.makeText(FetchGoogleCalendarEvents.this, "a", Toast.LENGTH_SHORT).show();
                    String url = (authorizationUrl.build());
//                    Toast.makeText(FetchGoogleCalendarEvents.this, "b", Toast.LENGTH_SHORT).show();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(FetchGoogleCalendarEvents.this, "c", Toast.LENGTH_SHORT).show();
                    startActivity(browserIntent);
                }
            };
            return ab.authorize("user");
//        return null;
        }

        private void test() throws IOException, GeneralSecurityException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
//            Toast.makeText(FetchGoogleCalendarEvents.this, "authorised", Toast.LENGTH_SHORT).show();

            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = service.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            myList = events.getItems();
        }
    }
}