package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.MainCalendarAdapter;
import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.customclasses.event.CustomEventFromGoogle;
import com.example.weekcalendar.helperclasses.MyOnEventClickListener;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.api.services.calendar.CalendarScopes;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.DAYS;

public class ActivityMainCalendar extends AppCompatActivity implements MyOnEventClickListener {
    private static final String TAG = ActivityMainCalendar.class.getSimpleName();
    private static boolean first = true;

    private TextView monthYear;

    private static Date today = new Date();
    private String[] splitDate = FULL_DATE.format(today).split("-");
    private String month = splitDate[1];
    private String day = splitDate[2];
    private String year = splitDate[0];

    // first day of previous month
    private Calendar calPrev = Calendar.getInstance();
    private Calendar calNext = Calendar.getInstance();
    private Date datePrev;
    private Date dateNext;

    // RecyclerView variables
    private Map<String, Map<String, List<CustomEvent>>> mapOfMonths = new HashMap<>(); // MM -> dd -> list
    private RecyclerView mRecyclerView;
    private MainCalendarAdapter mAdapter;
    private Map<String, MainCalendarAdapter> existingAdapters = new HashMap<>();
    private List<CustomEvent> LIST_WITH_EMPTY_CUSTOMEVENT = Arrays.asList(new CustomEventFromFirebase("No Events Today!", "", "", "", "", ""));
    private final MainCalendarAdapter EMPTY_ADAPTER = new MainCalendarAdapter(LIST_WITH_EMPTY_CUSTOMEVENT, this);

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // Set up UI
    private SetupNavDrawer navDrawer;
    private CompactCalendarView compactCalendarView;

    // To transform String to Date
    private static final DateFormat DATE_AND_TIME = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
    private static final DateFormat MONTH = new SimpleDateFormat("MM");
    private static final DateFormat MONTH_AND_YEAR = new SimpleDateFormat("MMMM yyyy");
    private static final DateFormat FULL_DATE = new SimpleDateFormat("yyyy-MM-dd");

    // Get data from Google
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final List<com.google.api.services.calendar.model.Event> EMPTY_EVENTS_LIST = new ArrayList<>();
    private GoogleSignInAccount acct;

    // Store reference copy
    private Map<String, CustomEvent> cache = new HashMap<>();

    // Months

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_calendar);

        // Set up navigation drawer START
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.main_calendar_toolbar));
        this.navDrawer.setupNavDrawerPane();

        this.monthYear = findViewById(R.id.month_year);
        this.monthYear.setText(MONTH_AND_YEAR.format(today));

        this.mRecyclerView = findViewById(R.id.today_events);
        LinearLayoutManager manager = new LinearLayoutManager(ActivityMainCalendar.this);
        this.mRecyclerView.setHasFixedSize(true);
        this.mRecyclerView.setLayoutManager(manager);

        calPrev.add(Calendar.MONTH, -2);
        calPrev.set(Calendar.DATE, calPrev.getActualMaximum(Calendar.DAY_OF_MONTH));
        datePrev = calPrev.getTime();
        calNext.add(Calendar.MONTH, 2);
        calNext.set(Calendar.DATE, 1);
        dateNext = calNext.getTime();

        this.compactCalendarView = findViewById(R.id.compact_calendar_view);
        this.compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        this.compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        this.compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                String clicked = FULL_DATE.format(dateClicked);
                if (existingAdapters.get(clicked) == null) {
                    mRecyclerView.setAdapter(EMPTY_ADAPTER);
                } else {
                    mRecyclerView.setAdapter(existingAdapters.get(clicked));
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                String date = MONTH_AND_YEAR.format(firstDayOfNewMonth);
                String fullDate = FULL_DATE.format(firstDayOfNewMonth);

                monthYear.setText(date);
                if (existingAdapters.get(fullDate) == null) {
                    mRecyclerView.setAdapter(EMPTY_ADAPTER);
                } else {
                    mRecyclerView.setAdapter(existingAdapters.get(fullDate));
                }

                if (acct != null) {
                    try {
                        new RequestAuth().execute(firstDayOfNewMonth).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else { // is a Firebase account
                    fetch3MonthEventsFromFirebase(firstDayOfNewMonth);
                }
            }
        });
        // Set up UI END

        // Fetching data START
            // logging in to google will also result in this.fAuth.getCurrentUser() != null being true
        this.acct = GoogleSignIn.getLastSignedInAccount(this);
        this.fAuth = FirebaseAuth.getInstance();
        if (this.acct != null) {
            try {
                new ActivityMainCalendar.RequestAuth().execute(today).get();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (this.fAuth.getCurrentUser() != null){
            this.fStore = FirebaseFirestore.getInstance();
            this.userID = this.fAuth.getCurrentUser().getUid();
            this.c = this.fStore.collection("events");
            fetch3MonthEventsFromFirebase(today);
        } else {
            Toast.makeText(this, "Not logged in to any account!", Toast.LENGTH_SHORT).show();
        }

        if (first) {
            String todayDate = FULL_DATE.format(today);
            if (this.existingAdapters.containsKey(todayDate)) {
                this.mRecyclerView.setAdapter(this.existingAdapters.get(todayDate));
            } else {
                this.mRecyclerView.setAdapter(EMPTY_ADAPTER);
            }
            first = false;
        }
        // Firebase end

        //testing firebase dynamic links

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                System.out.println("Hello World dyanmic links");

                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.getLink();
                }

                if (deepLink != null) {
                    String documentID = deepLink.getQueryParameter("id");
                    fStore.collection("events").document(documentID)
                            .update("participants", FieldValue.arrayUnion(ActivityMainCalendar.this.userID));
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error unable to retrieve link");
            }
        });
    }

    private void fetch3MonthEventsFromFirebase(Date curr) {
        Calendar[] neighbouringMonths = getNeighbourMonths(curr);
        String prev = FULL_DATE.format(neighbouringMonths[0].getTime());
        String next = FULL_DATE.format(neighbouringMonths[1].getTime());
        String prevMonth = MONTH.format(neighbouringMonths[0].getTime());
        String nextMonth = MONTH.format(neighbouringMonths[1].getTime());
        if (this.mapOfMonths.containsKey(prevMonth) && this.mapOfMonths.containsKey(nextMonth)) {
            return;
        }
        if (!this.mapOfMonths.containsKey(prevMonth) && !this.mapOfMonths.containsKey(nextMonth)) {
            Log.d(TAG, "both missing");
            firebaseQuery(prev, next);
        } else if (!this.mapOfMonths.containsKey(prevMonth)) {
            Log.d(TAG, "prev missing " + prevMonth + " " + mapOfMonths);
            Date[] missingMonth = getRequiredMonth(neighbouringMonths, -1);
            prev = FULL_DATE.format(missingMonth[0].getTime());
            next = FULL_DATE.format(missingMonth[1].getTime());
            firebaseQuery(prev, next);
        } else if (!this.mapOfMonths.containsKey(nextMonth)) {
            Log.d(TAG, "next missing " + nextMonth);
            Date[] missingMonth = getRequiredMonth(neighbouringMonths, 1);
            prev = FULL_DATE.format(missingMonth[0].getTime());
            next = FULL_DATE.format(missingMonth[1].getTime());
            firebaseQuery(prev, next);
        }
    }

    private Calendar[] getNeighbourMonths(Date current) { // start inclusive and end exclusive as per google calendar api
        Calendar calPrev = Calendar.getInstance();
        calPrev.setTime(current);
        calPrev.add(Calendar.MONTH, -1);
        calPrev.set(Calendar.DATE, 1);

        Calendar calNext = Calendar.getInstance();
        calNext.setTime(current);
        calNext.add(Calendar.MONTH, 2);
        calNext.set(Calendar.DATE, 1);

        return new Calendar[] { calPrev, calNext };
    }

    private Date[] getRequiredMonth(Calendar[] neighbouringMonths, int addMonth) { // start inclusive and end exclusive as per google calendar api
        neighbouringMonths[0].set(Calendar.DATE, 1);
        neighbouringMonths[1].add(Calendar.MONTH, addMonth);
        neighbouringMonths[1].set(Calendar.DATE, neighbouringMonths[1].getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Date[] { neighbouringMonths[0].getTime(), neighbouringMonths[1].getTime() };
    }

    private void firebaseQuery(String min, String max) {
        this.c.whereArrayContains("participants", userID)
                .whereGreaterThanOrEqualTo("startDate", min)
                .whereLessThan("startDate", max)
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
                                try {
                                    processFirebaseDocument(document);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            // optimise to only loop through those that are fetched
                            for (String month : mapOfMonths.keySet()) {
                                for (String day : mapOfMonths.get(month).keySet()) {
                                    String date = new StringBuilder(year).append("-").append(month).append("-").append(day).toString();
                                    Log.d(TAG, "date is " + date);
                                    existingAdapters.put(date, new MainCalendarAdapter(mapOfMonths.get(month).get(day), ActivityMainCalendar.this));
                                }
                            }
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

    private void addToCalendarWidget(CustomEvent e) throws ParseException {
        Date d = FULL_DATE.parse(e.getStartDate());
        Event event = new Event(Color.LTGRAY, d.getTime(), e.getTitle());
        this.compactCalendarView.addEvent(event);
    }

    private void processFirebaseDocument(QueryDocumentSnapshot document) throws ParseException {
        String title = (String) document.get("eventTitle");
        String startDate = (String) document.get("startDate");
        String endDate = (String) document.get("endDate");
        String startTime = (String) document.get("startTime");
        String endTime = (String) document.get("endTime");
        String docID = document.getId();
        CustomEvent event;
        if (startDate.equals(endDate)) { // just one day
            event = new CustomEventFromFirebase(title, startDate, endDate, startTime, endTime, docID);
            addToMap(event);
            addToCalendarWidget(event);
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
                event = new CustomEventFromFirebase(title, newDate, endDate, startTime, endTime, docID);
                addToMap(event);
                addToCalendarWidget(event);
            }
        }
    }

    private void processGoogleEvent(com.google.api.services.calendar.model.Event e) throws ParseException {
        String title = e.getSummary();
        String startDate;
        String startTime;
        String endDate;
        String endTime;
        String eventID = e.getId();
        CustomEvent event;
        if (e.getStart().get("dateTime") == null) { // full day event of any number of days
            try {
                Log.d(TAG, "HIIIIIIIIIIIIII" + e.toPrettyString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            startDate = e.getStart().get("date").toString();
            startTime = "All day";
            String fullEndDate = e.getEnd().get("date").toString();
            int endDateNum = Integer.parseInt(fullEndDate.substring(fullEndDate.length() - 2)) - 1;
            endDate = fullEndDate.substring(0, fullEndDate.length() - 2) + String.format("%02d", endDateNum); // on Google it is instantiated as the next day;
            endTime = "23:59";
        } else { // < 1 day events, as well as non-full day events spanning multiple days
            String[] startDateAndTimeSplit = e.getStart().get("dateTime").toString().split("T");
            startDate = startDateAndTimeSplit[0];
            startTime = startDateAndTimeSplit[1].substring(0, 5); // getOriginalStart?
            String[] endDateAndTimeSplit = e.getEnd().get("dateTime").toString().split("T");
            endDate = endDateAndTimeSplit[0];
            endTime = endDateAndTimeSplit[1].substring(0, 5);
        }
        if (startDate.equals(endDate)) { // <= 1 day
            event = new CustomEventFromGoogle(title, startDate, endDate, startTime, endTime, eventID);
            addToMap(event);
            addToCalendarWidget(event);
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
                event = new CustomEventFromFirebase(title, newDate, endDate, startTime, endTime, eventID);
                addToMap(event);
                addToCalendarWidget(event);
            }
        }
    }

    private void addToMap(CustomEvent event) {
        String[] splitDate = event.getStartDate().split("-");
        String month = splitDate[1];
        String day = splitDate[2];
        if (this.mapOfMonths.containsKey(month)) {
            Map<String, List<CustomEvent>> currMonth = this.mapOfMonths.get(month);
            if (currMonth.containsKey(day)) {
                currMonth.get(day).add(event);
            } else {
                List<CustomEvent> temp = new ArrayList<>();
                temp.add(event);
                currMonth.put(day, temp);
            }
        } else {
            List<CustomEvent> temp = new ArrayList<>();
            temp.add(event);
            Map<String, List<CustomEvent>> currMonth = new HashMap<>();
            currMonth.put(day, temp);
            this.mapOfMonths.put(month, currMonth);
            Log.d(TAG, "added " + month);
        }
        if (this.cache.get(event.getId()) == null) {
            this.cache.put(event.getId(), event);
        }
//        Log.d(TAG, "added " + event.getTitle() + " to map");
    }

    @Override
    public void onEventClickListener(String eventID) {
        Intent i = new Intent(this, ActivityEventDetailsPage.class);
        i.putExtra("event", this.cache.get(eventID));
        startActivity(i);
    }

    private class RequestAuth extends AsyncTask<Date, Void, Void> {
        private List<com.google.api.services.calendar.model.Event> myList;
        private Set<com.google.api.services.calendar.model.Event> fetched;

        @Override
        protected Void doInBackground(Date... dates) {
            try {
                pullData(dates[0]);
                for (com.google.api.services.calendar.model.Event e : myList) {
                    try {
                        processGoogleEvent(e);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
                // optimise to only loop through those that are fetched
                for (String month : mapOfMonths.keySet()) {
                    for (String day : mapOfMonths.get(month).keySet()) {
                        String date = new StringBuilder(year).append("-").append(month).append("-").append(day).toString();
                        existingAdapters.put(date, new MainCalendarAdapter(mapOfMonths.get(month).get(day), ActivityMainCalendar.this));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void pullData(Date curr) throws IOException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            // current month will always be fetched
            Calendar[] neighbouringMonths = getNeighbourMonths(curr);
            Log.d(TAG, curr.toString());
            neighbouringMonths[1].add(Calendar.MONTH, -1);
            String prevMonth = MONTH.format(neighbouringMonths[0].getTime());
            String nextMonth = MONTH.format(neighbouringMonths[1].getTime());
            neighbouringMonths[1].add(Calendar.MONTH, 1);
            if (mapOfMonths.containsKey(prevMonth) && mapOfMonths.containsKey(nextMonth)) {
                myList = EMPTY_EVENTS_LIST;
                return;
            }

            if (!mapOfMonths.containsKey(prevMonth) && !mapOfMonths.containsKey(nextMonth)) {
                googleQuery(service, neighbouringMonths[0].getTime(), neighbouringMonths[1].getTime());
            } else if (!mapOfMonths.containsKey(prevMonth)) {
                Date[] missingMonth = getRequiredMonth(neighbouringMonths, -1);
                googleQuery(service, missingMonth[0], missingMonth[1]);
            } else if (!mapOfMonths.containsKey(nextMonth)) {
                Date[] missingMonth = getRequiredMonth(neighbouringMonths, 1);
                googleQuery(service, missingMonth[0], missingMonth[1]);
            }
        }

        private void googleQuery(com.google.api.services.calendar.Calendar service, Date first, Date second) throws IOException {
            DateTime min = new DateTime(first);
            DateTime max = new DateTime(second);
            Log.d(TAG, "querying from " + min + " to " + max);
            Events events = service.events().list("primary")
                    .setTimeMin(min)
                    .setTimeMax(max)
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
            InputStream in = ActivityMainCalendar.this.getAssets().open(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            File tokenFolder = new File(ActivityMainCalendar.this.getFilesDir(), "tokens");

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