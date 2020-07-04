package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.helperclasses.MyTimeDialog;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputLayout;
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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityCreateEventPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener, MyTimeDialog.MyTimeDialogListener {
    private static final String TAG = ActivityCreateEventPage.class.getSimpleName();

    // XML variables
    private EditText title;
    private TextInputLayout selectStartDateLayout;
    private Button selectStartDate;
    private TextInputLayout selectEndDateLayout;
    private Button selectEndDate;
    private TextInputLayout selectStartTimeLayout;
    private Button selectStartTime;
    private TextInputLayout selectEndTimeLayout;
    private Button selectEndTime;
    private Button createEvent;
    private EditText todo1;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference cEvents;
    private CollectionReference cToDo;

    private CustomEvent event;

    // Get data from Google
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = CalendarScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.cEvents = this.fStore.collection("events");
        this.cToDo = this.fStore.collection("todo");

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        // Links to XML
        this.title = findViewById(R.id.insert_event_name);

        this.selectStartDateLayout = findViewById(R.id.selectStartDateLayout);
        this.selectEndDateLayout = findViewById(R.id.selectEndDateLayout);
//        this.selectStartTimeLayout = findViewById(R.id.selectStartDateLayout);
//        this.selectEndTimeLayout = findViewById(R.id.selectStartDateLayout);

        this.selectStartDate = findViewById(R.id.select_start_date);
        this.selectStartDate.setOnClickListener(v -> openSelectDateDialog(this.selectStartDate));

        this.selectEndDate = findViewById(R.id.select_end_date);
        this.selectEndDate.setOnClickListener(v -> openSelectDateDialog(this.selectEndDate));

        this.selectStartTime = findViewById(R.id.select_start_time);
        this.selectStartTime.setOnClickListener(v -> openSelectTimeDialog(this.selectStartTime));

        this.selectEndTime = findViewById(R.id.select_end_time);
        this.selectEndTime.setOnClickListener(v -> openSelectTimeDialog(this.selectEndTime));

        this.todo1 = findViewById(R.id.todo_item);

        this.createEvent = findViewById(R.id.create_event_button);
        if (acct != null) { // if logged in to a Google account
            RequestAuth task = new RequestAuth();
            this.createEvent.setOnClickListener(v -> task.execute("create"));
        } else {
            this.createEvent.setOnClickListener(v -> createFirebaseEvent());
        }

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.create_event_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityUpcomingPage.class));
        });

        Intent i = getIntent();
        this.event = i.getParcelableExtra("event to edit");
        if (this.event != null) {
            this.title.setText(event.getTitle());
            this.selectStartDate.setText(HelperMethods.formatDateForView(event.getStartDate()));
            this.selectEndDate.setText(HelperMethods.formatDateForView(event.getEndDate()));
            this.selectStartTime.setText(HelperMethods.formatTimeTo12H(event.getStartTime()));
            this.selectEndTime.setText(HelperMethods.formatTimeTo12H(event.getEndTime()));
            this.createEvent.setText("Update Event");
            if (acct != null) {
                RequestAuth task = new RequestAuth();
                Log.d(TAG, "!!!!! " + this.event.getId());
                this.createEvent.setOnClickListener(v -> task.execute("update", this.event.getId()));
            } else {
                this.createEvent.setOnClickListener(v -> updateFirebaseEvent());
            }
        }
    }

    // need to have a check for start day >= end day, start time >= end time
    private boolean checkFields() {
        String eventTitle = this.title.getText().toString();
        if (eventTitle.equals("")) {
            this.title.setError("Please insert event title!");
        } else if (this.selectStartDate.getText().toString().equals("Select Start Date")) {
            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectStartDate.setError("Please choose a start date!");
        } else if (this.selectEndDate.getText().toString().equals("Select End Date")) {
            this.selectEndDateLayout.setError("Please choose an end date!");
            this.selectEndDate.setError("Please choose an end date!");
        } else if (this.selectStartTime.getText().toString().equals("Select Start Time")) {
//            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectStartTime.setError("Please choose a start time!");
        } else if (this.selectEndTime.getText().toString().equals("Select End Time")) {
//            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectEndTime.setError("Please choose an end time!");
        } else {
            return true;
        }
        return false;
    }

    private Map<String, Object> getEventDetails() {
        String eventTitle = ((EditText) findViewById(R.id.insert_event_name)).getText().toString();
        String startDate = HelperMethods.formatDateWithDash(this.selectStartDate.getText().toString());
        String endDate = HelperMethods.formatDateWithDash(this.selectEndDate.getText().toString());
        String startTime = HelperMethods.formatTimeTo24H(this.selectStartTime.getText().toString());
        String endTime = HelperMethods.formatTimeTo24H(this.selectEndTime.getText().toString());

        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("userID", this.userID);
        eventDetails.put("eventTitle", eventTitle);
        eventDetails.put("startDate", startDate);
        eventDetails.put("endDate", endDate);
        eventDetails.put("startTime", startTime);
        eventDetails.put("endTime", endTime);

        return eventDetails;
    }

    private Map<String, Object> getToDoDetails() {
        String toDo = this.todo1.getText().toString();

        if (!toDo.equals("")) {
            Map<String, Object> toDoDetails = new HashMap<>();
            String startDate = HelperMethods.formatDateWithDash(this.selectStartDate.getText().toString());
            toDoDetails.put("userID", this.userID);
            toDoDetails.put("date", startDate);
            toDoDetails.put("title", toDo);
            return toDoDetails;
        } else {
            return null;
        }
    }

    private void updateFirebaseEvent() {
        if (checkFields()) {
            DocumentReference thisEventDoc = this.cEvents.document(this.event.getId());
//            DocumentReference thisToDoDoc = this.cToDo.document(this.event.getId());

            Map<String, Object> eventDetails = getEventDetails();
//            Map<String, Object> toDoDetails = getToDoDetails();

            thisEventDoc.update(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        // need to check if event originally had to do items
                        // if true, then we need to update this based on the existing to do's document ID
                        // if false, we need to create new document
//                        if (toDoDetails != null) {
//                            DocumentReference toDoDocRef = cToDo.document();
//                            toDoDetails.put("eventID", this.event.getId());
//                            toDoDetails.put("todoID", toDoDocRef.getId());
//                            toDoDocRef.set(toDoDetails)
//                                    .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
//                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
//                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);
        }
    }

    // need to implement multi day here
    private void createFirebaseEvent() {
        if (checkFields()) {
            Map<String, Object> eventDetails = getEventDetails();
            Map<String, Object> toDoDetails = getToDoDetails();

            cEvents.add(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        if (toDoDetails != null) {
                            toDoDetails.put("eventID", docRef.getId());
                            cToDo.add(toDoDetails)
                                    .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        //Create deep link
                        System.out.println(docRef.getId());
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);
        }
    }

    private void openSelectDateDialog(Button b) {
        MyDateDialog myDateDialog = new MyDateDialog(b);
        myDateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    private void openSelectTimeDialog(Button b) {
        MyTimeDialog myTimeDialog = new MyTimeDialog(b);
        myTimeDialog.show(getSupportFragmentManager(), "time dialog");
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
    }

    @Override
    public void applyTimeText(CustomDay d, Button b) {
        b.setText(d.getTime());
    }

    private class RequestAuth extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                pushData(strings);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private void pushData(String... strings) throws IOException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            if (strings[0].equalsIgnoreCase("create")) {
                createGoogleEvent(service);
            } else {
                updateGoogleEvent(service, strings[1]);
            }
        }

        /**
         * Creates an authorized Credential object.
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
            // Load client secrets.
            InputStream in = ActivityCreateEventPage.this.getAssets().open(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            File tokenFolder = new File(ActivityCreateEventPage.this.getFilesDir(), "tokens");

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
                    String url = authorizationUrl.build();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            };
            return ab.authorize("user");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Intent i = new Intent(ActivityCreateEventPage.this, ActivityUpcomingPage.class);
                startActivity(i);
            }
        }

        private void createGoogleEvent(Calendar service) throws IOException {
            if (checkFields()) {
                Map<String, Object> eventDetails = getEventDetails();
//            Map<String, Object> toDoDetails = getToDoDetails();

                Event e = new Event()
                        .setSummary(eventDetails.get("eventTitle").toString());
                DateTime startDT = new DateTime(HelperMethods.toGoogleDateTime(eventDetails.get("startDate").toString(),
                        eventDetails.get("startTime").toString()));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDT)
                        .setTimeZone("Asia/Singapore");
                e.setStart(start);
                DateTime endDT = new DateTime(HelperMethods.toGoogleDateTime(eventDetails.get("endDate").toString(),
                        eventDetails.get("endTime").toString()));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDT)
                        .setTimeZone("Asia/Singapore");
                e.setEnd(end);
                String calID = "primary";
                service.events().insert(calID, e).execute();
            }
        }

        private void updateGoogleEvent(Calendar service, String eventID) throws IOException {
            if (checkFields()) {
                Map<String, Object> eventDetails = getEventDetails();
//            Map<String, Object> toDoDetails = getToDoDetails();

                Event e = service
                        .events()
                        .get("primary", eventID)
                        .execute()
                        .setSummary(eventDetails.get("eventTitle").toString());
                DateTime startDT = new DateTime(HelperMethods.toGoogleDateTime(eventDetails.get("startDate").toString(),
                        eventDetails.get("startTime").toString()));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDT)
                        .setTimeZone("Asia/Singapore");
                e.setStart(start);
                DateTime endDT = new DateTime(HelperMethods.toGoogleDateTime(eventDetails.get("endDate").toString(),
                        eventDetails.get("endTime").toString()));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDT)
                        .setTimeZone("Asia/Singapore");
                e.setEnd(end);
                String calID = "primary";
                service.events().update(calID, eventID, e).execute();
            }
        }
    }
}