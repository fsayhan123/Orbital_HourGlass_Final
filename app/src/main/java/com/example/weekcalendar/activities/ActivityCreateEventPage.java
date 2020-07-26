package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.example.weekcalendar.customclasses.CustomToDo;
import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.helperclasses.MyTimeDialog;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ActivityCreateEventPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener, MyTimeDialog.MyTimeDialogListener {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityCreateEventPage.class.getSimpleName();

    /**
     * UI variables
     */
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
    private EditText description;
    private LinearLayout todoListLayout;
    private List<EditText> todos;

    /**
     * Firebase information
     */
    private String userID;
    private CollectionReference cEvents;
    private CollectionReference cToDo;

    /**
     * Google information
     */
    private GoogleSignInAccount acct;
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = CalendarScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * If updating an event, stores data that needs to be displayed
     */
    private CustomEvent event;
    private String eventID;
    private List<CustomToDo> originalToDos;

    /**
     * Sets up ActivityCreateEventPage when it is opened.
     * First, sets up Firebase or Google account.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, sets up user interface depending if we are creating or updating an event.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_page);

        // Setup link to Firebase
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        this.cEvents = fStore.collection("events");
        this.cToDo = fStore.collection("todo");

        this.acct = GoogleSignIn.getLastSignedInAccount(this);

        setupXMLItems();

        Intent i = getIntent();
        this.event = i.getParcelableExtra("event to edit");
        String dateClicked = i.getStringExtra("date clicked");
        if (this.event != null) {
            setupUpdateInterface();
        } else if (dateClicked != null) {
            setupCreateInterface(dateClicked);
        }
    }

    /**
     * Sets up layout for ActivityCreateEventPage.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.create_event_toolbar);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityMainCalendar.class));
        });

        this.title = findViewById(R.id.insert_event_name);

        this.selectStartDateLayout = findViewById(R.id.selectStartDateLayout);
        this.selectEndDateLayout = findViewById(R.id.selectEndDateLayout);
        this.selectStartTimeLayout = findViewById(R.id.selectStartTimeLayout);
        this.selectEndTimeLayout = findViewById(R.id.selectEndTimeLayout);

        this.selectStartDate = findViewById(R.id.select_start_date);
        this.selectStartDate.setOnClickListener(v -> openSelectDateDialog(this.selectStartDate));

        this.selectEndDate = findViewById(R.id.select_end_date);
        this.selectEndDate.setOnClickListener(v -> openSelectDateDialog(this.selectEndDate));

        this.selectStartTime = findViewById(R.id.select_start_time);
        this.selectStartTime.setOnClickListener(v -> openSelectTimeDialog(this.selectStartTime));

        this.selectEndTime = findViewById(R.id.select_end_time);
        this.selectEndTime.setOnClickListener(v -> openSelectTimeDialog(this.selectEndTime));

        this.description = findViewById(R.id.insert_event_description);

        this.todoListLayout = findViewById(R.id.fill_in_todos);

        EditText todo1 = findViewById(R.id.todo_item);
        this.todos = new ArrayList<>();
        this.todos.add(todo1);

        Button addToDoButton = findViewById(R.id.add_todo);
        addToDoButton.setOnClickListener(v -> addToDoEditText());

        this.originalToDos = new ArrayList<>();

        this.createEvent = findViewById(R.id.create_event_button);

        if (this.acct != null) {
            RequestAuth task = new RequestAuth();
            this.createEvent.setOnClickListener(v -> task.execute("create"));
        } else {
            this.createEvent.setOnClickListener(v -> createFirebaseEvent());
        }
    }

    /**
     * Sets up interface to create event on specified dateClicked if user clicked a date in
     * AcitvityUpcomingPage.
     * @param dateClicked date user clicked
     */
    private void setupCreateInterface(String dateClicked) {
        this.selectStartDate.setText(dateClicked);
    }

    /**
     * Sets up interface to update an event if user clicked an event.
     */
    private void setupUpdateInterface() {
        this.eventID = this.event.getId();
        this.title.setText(this.event.getTitle());
        this.selectStartDate.setText(HelperMethods.formatDateForView(this.event.getStartDate()));
        this.selectEndDate.setText(HelperMethods.formatDateForView(this.event.getEndDate()));
        this.selectStartTime.setText(HelperMethods.formatTimeTo12H(this.event.getStartTime()));
        this.selectEndTime.setText(HelperMethods.formatTimeTo12H(this.event.getEndTime()));
        if (this.event.getDescription() == null || !this.event.getDescription().equals("")) {
            this.description.setText(this.event.getDescription());
        }
        this.createEvent.setText("Update Event");
        fetchToDos(this.event.getId());
        if (acct != null) {
            RequestAuth task = new RequestAuth();
            Log.d(TAG, "!!!!! " + this.event.getId());
            this.createEvent.setOnClickListener(v -> task.execute("update", this.event.getId()));
        } else {
            this.createEvent.setOnClickListener(v -> updateFirebaseEvent());
        }
    }

    /**
     * Queries to do items associated with this event from Firebase.
     * @param eventID ID of Firebase or Google event
     */
    private void fetchToDos(String eventID) {
        this.cToDo.whereEqualTo("eventID", eventID)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: NO TODO");
                        } else {
                            Log.d(TAG, "onSuccess: TODO EXISTS");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processToDoDocument(document);
                            }
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
     * Processes Firebase document from to do collection into a CustomToDo
     * @param doc Firebase document from to do collection
     */
    private void processToDoDocument(QueryDocumentSnapshot doc) {
        String date = (String) doc.get("date");
        String title = (String) doc.get("title");
        String id = doc.getId();
        boolean completed = (boolean) doc.get("completed");
        CustomToDo todo = new CustomToDo(id, title, date, completed);
        this.originalToDos.add(todo);

        EditText e = this.todos.get(this.todos.size() - 1);
        e.setText(title);
        addToDoEditText();
    }

    /**
     * Adds more to do input fields when clicked.
     */
    private void addToDoEditText() {
        if (!this.todos.get(this.todos.size() - 1).getText().toString().equals("")) {
            LayoutInflater inflater = this.getLayoutInflater();
            @SuppressLint("InflateParams") View eachToDo = inflater.inflate(R.layout.each_todo_create_event, null);
            this.todoListLayout.addView(eachToDo);
            this.todos.add(eachToDo.findViewById(R.id.todo_item));
        }
    }

    /**
     * Checks if all required fields are filled in by user. Otherwise, displays error.
     * @return boolean indicating if all required fields are filled in
     */
    private boolean checkFields() {
        String eventTitle = this.title.getText().toString();
        if (eventTitle.equals("")) {
            this.title.setError("Please insert event title!");
        } else if (this.selectStartDate.getText().toString().equals("Select Start Date")) {
            this.selectStartDateLayout.setError("Please choose a start date!");
        } else if (this.selectEndDate.getText().toString().equals("Select End Date")) {
            this.selectEndDateLayout.setError("Please choose an end date!");
        } else if (this.selectStartTime.getText().toString().equals("Select Start Time")) {
            this.selectStartTimeLayout.setError("Please choose a start time!");
        } else if (this.selectEndTime.getText().toString().equals("Select End Time")) {
            this.selectEndTimeLayout.setError("Please choose a end time!");
        } else if (!HelperMethods.compareDates(this.selectStartDate.getText().toString(),
                this.selectEndDate.getText().toString())) {
            this.selectStartDateLayout.setError("Start date must be before end date!");
            this.selectEndDateLayout.setError("End date must be after start date!");
        } else if (!HelperMethods.compareTimes(this.selectStartTime.getText().toString(),
                this.selectEndTime.getText().toString())) {
            this.selectStartTimeLayout.setError("Start time must be before end time!");
            this.selectEndTimeLayout.setError("End time must be after start time!");
        } else {
            return true;
        }
        return false;
    }

    /**
     * Gets details of the event as filled in by the user.
     * @return Map containing the details of the event, to be pushed to Firebase events collection
     */
    private Map<String, Object> getEventDetails() {
        String eventTitle = this.title.getText().toString();
        String startDate = HelperMethods.formatDateWithDash(this.selectStartDate.getText().toString());
        String endDate = HelperMethods.formatDateWithDash(this.selectEndDate.getText().toString());
        String startTime = HelperMethods.formatTimeTo24H(this.selectStartTime.getText().toString());
        String endTime = HelperMethods.formatTimeTo24H(this.selectEndTime.getText().toString());
        String eventDescription = this.description.getText().toString();

        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("userID", this.userID);
        eventDetails.put("eventTitle", eventTitle);
        eventDetails.put("startDate", startDate);
        eventDetails.put("endDate", endDate);
        eventDetails.put("startTime", startTime);
        eventDetails.put("endTime", endTime);
        eventDetails.put("description", eventDescription);
        if (this.event == null) {
            eventDetails.put("participants", Collections.singletonList(this.userID));
        }
        return eventDetails;
    }

    /**
     * Gets details of all the to do items as filled in by the user.
     * @return a List of Maps, where each Map represents details of 1 to do item, to be pushed to Firebase
     * to do collection
     */
    private List<Map<String, Object>> getToDoDetails() {
        List<Map<String, Object>> allToDoDetails = new ArrayList<>();
        long offset = 0;
        for (EditText e : this.todos) {
            String toDo = e.getText().toString();
            if (!toDo.equals("")) {
                Map<String, Object> toDoDetails = new HashMap<>();
                String startDate = HelperMethods.formatDateWithDash(this.selectStartDate.getText().toString());
                toDoDetails.put("userID", this.userID);
                toDoDetails.put("date", startDate);
                toDoDetails.put("title", toDo);
                toDoDetails.put("completed", false);
                toDoDetails.put("timestamp", System.currentTimeMillis() + offset);
                allToDoDetails.add(toDoDetails);
                offset++;
            }
        }
        return allToDoDetails;
    }

    /**
     * Creates a Firebase event document in events collection based on details input by user.
     */
    private void createFirebaseEvent() {
        if (checkFields()) {
            Map<String, Object> eventDetails = getEventDetails();
            List<Map<String, Object>> allToDoDetails = getToDoDetails();

            this.cEvents.add(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        eventID = docRef.getId();
                        for (Map<String, Object> todo : allToDoDetails) {
                            todo.put("eventID", docRef.getId());
                            cToDo.add(todo)
                                    .addOnSuccessListener(docRef2 -> {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent i = new Intent(ActivityCreateEventPage.this, ActivityEventDetailsPage.class);
                        i.putExtra("eventID", this.eventID);
                        startActivity(i);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
    }

    /**
     * Updates Firebase event document in events collection based on updated details input by user.
     */
    private void updateFirebaseEvent() {
        if (checkFields()) {
            DocumentReference thisEventDoc = this.cEvents.document(this.event.getId());

            Map<String, Object> eventDetails = getEventDetails();
            List<Map<String, Object>> todoDetails = getToDoDetails();

            thisEventDoc.update(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        for (int i = 0; i < originalToDos.size(); i++) {
                            CustomToDo todo = originalToDos.get(i);
                            DocumentReference toDoDocRef = cToDo.document(todo.getID());
                            Map<String, Object> thisToDoDetails = new HashMap<>();
                            thisToDoDetails.put("eventID", this.event.getId());
                            thisToDoDetails.put("todoID", toDoDocRef.getId());
                            thisToDoDetails.put("date", eventDetails.get("startDate"));
                            String newTitle = (String) todoDetails.get(i).get("title");
                            thisToDoDetails.put("title", newTitle);
                            toDoDocRef.update(thisToDoDetails)
                                    .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }

                        for (int i = originalToDos.size(); i < todoDetails.size(); i++) {
                            Map<String, Object> nextToDo = getToDoDetails().get(i);
                            nextToDo.put("eventID", this.event.getId());
                            cToDo.add(nextToDo)
                                    .addOnSuccessListener(docRef2 -> {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(ActivityCreateEventPage.this, ActivityEventDetailsPage.class);
                        intent.putExtra("eventID", event.getId());
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
    }

    /**
     * Opens a date picker interface in a dialog for user to select the date.
     * @param b Button object reference
     */
    private void openSelectDateDialog(Button b) {
        int day, month, year;
        if (b.getText().toString().contains("SELECT")) {
            String givenDate = b.getText().toString();
            String[] split = givenDate.split(" ");
            day = Integer.parseInt(split[0]);
            month = Integer.parseInt(HelperMethods.convertMonth(split[1]));
            year = Integer.parseInt(split[2]);
        } else {
            java.util.Calendar c = java.util.Calendar.getInstance();
            day = c.get(java.util.Calendar.DAY_OF_MONTH);
            month = c.get(java.util.Calendar.MONTH);
            year = c.get(java.util.Calendar.YEAR);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityCreateEventPage.this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                b.setText(String.format("%02d", dayOfMonth) + " " + HelperMethods.numToStringMonth[month + 1].substring(0, 3) + " " + year);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Opens a date picker interface in a dialog for user to select the time.
     * @param b Button object reference
     */
    private void openSelectTimeDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int amPM = c.get(java.util.Calendar.AM_PM);
        int minute = c.get(java.util.Calendar.MINUTE);
        int hour = amPM == 1 ? c.get(java.util.Calendar.HOUR) + 12 : c.get(java.util.Calendar.HOUR);
        TimePickerDialog timePickerDialog = new TimePickerDialog(ActivityCreateEventPage.this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String end = "AM";
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    end = "PM";
                } else if (hourOfDay == 0) {
                    hourOfDay = 12;
                }
                b.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + " " + end);
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    /**
     * Displays selected date by user on the Button of reference.
     * @param d CustomDay user chose
     * @param b button to set text on
     */
    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getFullDateForView());
    }

    /**
     * Displays selected time by user on the Button of reference.
     * @param d CustomDay user chose
     * @param b button to set text on
     */
    @Override
    public void applyTimeText(CustomDay d, Button b) {
        b.setText(d.getTime());
    }

    /**
     * Google Calendar API handler class for asynchronous task handling.
     */
    @SuppressLint("StaticFieldLeak")
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
                Intent i = new Intent(ActivityCreateEventPage.this, ActivityEventDetailsPage.class);
                assert eventID != null;
                i.putExtra("eventID", eventID);
                startActivity(i);
            }
        }

        private void createGoogleEvent(Calendar service) throws IOException {
            if (checkFields()) {
                Map<String, Object> eventDetails = getEventDetails();
                Event e = new Event()
                        .setSummary(Objects.requireNonNull(eventDetails.get("eventTitle")).toString())
                        .setDescription(Objects.requireNonNull(eventDetails.get("description")).toString());
                DateTime startDT = new DateTime(HelperMethods.toGoogleDateTime(Objects.requireNonNull(eventDetails.get("startDate")).toString(),
                        Objects.requireNonNull(eventDetails.get("startTime")).toString()));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDT)
                        .setTimeZone("Asia/Singapore");
                e.setStart(start);
                DateTime endDT = new DateTime(HelperMethods.toGoogleDateTime(Objects.requireNonNull(eventDetails.get("endDate")).toString(),
                        Objects.requireNonNull(eventDetails.get("endTime")).toString()));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDT)
                        .setTimeZone("Asia/Singapore");
                e.setEnd(end);
                String calID = "primary";
                Event response = service.events().insert(calID, e).execute();
                eventID = response.getId();

                List<Map<String, Object>> allToDoDetails = getToDoDetails();
                for (Map<String, Object> todo : allToDoDetails) {
                    todo.put("eventID", response.getId());
                    cToDo.add(todo)
                            .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                            .addOnFailureListener(td -> Log.w(TAG, "Error writing todo document", td));
                }
            }
        }

        private void updateGoogleEvent(Calendar service, String eventID) throws IOException {
            if (checkFields()) {
                Map<String, Object> eventDetails = getEventDetails();
                Event e = service
                        .events()
                        .get("primary", eventID)
                        .execute()
                        .setSummary(Objects.requireNonNull(eventDetails.get("eventTitle")).toString())
                        .setDescription(Objects.requireNonNull(eventDetails.get("description")).toString());
                DateTime startDT = new DateTime(HelperMethods.toGoogleDateTime(Objects.requireNonNull(eventDetails.get("startDate")).toString(),
                        Objects.requireNonNull(eventDetails.get("startTime")).toString()));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDT)
                        .setTimeZone("Asia/Singapore");
                e.setStart(start);
                DateTime endDT = new DateTime(HelperMethods.toGoogleDateTime(Objects.requireNonNull(eventDetails.get("endDate")).toString(),
                        Objects.requireNonNull(eventDetails.get("endTime")).toString()));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDT)
                        .setTimeZone("Asia/Singapore");
                e.setEnd(end);
                String calID = "primary";
                service.events().update(calID, eventID, e).execute();

                List<Map<String, Object>> todoDetails = getToDoDetails();

                for (int i = 0; i < originalToDos.size(); i++) {
                    CustomToDo todo = originalToDos.get(i);
                    DocumentReference toDoDocRef = cToDo.document(todo.getID());
                    Map<String, Object> thisToDoDetails = new HashMap<>();
                    thisToDoDetails.put("eventID", event.getId());
                    thisToDoDetails.put("todoID", toDoDocRef.getId());
                    thisToDoDetails.put("date", eventDetails.get("startDate"));
                    String newTitle = (String) todoDetails.get(i).get("title");
                    thisToDoDetails.put("title", newTitle);
                    toDoDocRef.update(thisToDoDetails)
                            .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                            .addOnFailureListener(doc -> Log.w(TAG, "Error writing document", doc));
                }

                for (int i = originalToDos.size(); i < todoDetails.size(); i++) {
                    Map<String, Object> nextToDo = getToDoDetails().get(i);
                    nextToDo.put("eventID", event.getId());
                    cToDo.add(nextToDo)
                            .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                            .addOnFailureListener(doc -> Log.w(TAG, "Error writing document", doc));
                }
            }
        }
    }
}