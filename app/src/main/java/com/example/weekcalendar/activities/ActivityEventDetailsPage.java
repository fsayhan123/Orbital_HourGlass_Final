package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.adapters.EventDetailsToDoAdapter;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.customclasses.event.CustomEventFromGoogle;
import com.example.weekcalendar.helperclasses.InviteDialog;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.DAYS;

public class ActivityEventDetailsPage extends AppCompatActivity {
    private static final String TAG = ActivityEventDetailsPage.class.getSimpleName();

    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventDescription;
    private RecyclerView allToDos;
    private EventDetailsToDoAdapter toDoAdapter;

    private CustomEvent event;
    private List<CustomToDo> listOfToDos;

    /**
     * Firebase information
     */
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    /**
     * Google information
     */
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = CalendarScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private GoogleSignInAccount googleAcct;
    private ActivityEventDetailsPage.RequestAuth task = new ActivityEventDetailsPage.RequestAuth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_page);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        this.c = this.fStore.collection("events");

        this.googleAcct = GoogleSignIn.getLastSignedInAccount(this);

        setupXMLItems();

        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");

        fetchFirebaseEventDetails(eventID);
        fetchToDos(eventID);
    }

    /**
     * Sets up layout for ActivityEventDetails.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.event_details);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityMainCalendar.class));
        });

        this.eventTitle = findViewById(R.id.event_title);

        this.eventDate = findViewById(R.id.event_date);

        this.eventTime = findViewById(R.id.event_time);

        this.eventDescription = findViewById(R.id.event_description);

        Button inviteEvent = findViewById(R.id.invite_event);
        inviteEvent.setOnClickListener(v -> eventInvite());

        this.allToDos = findViewById(R.id.all_todo);
        LinearLayoutManager manager = new LinearLayoutManager(ActivityEventDetailsPage.this);
        this.allToDos.setHasFixedSize(true);
        this.allToDos.setLayoutManager(manager);
        this.listOfToDos = new ArrayList<>();
    }

    /**
     * Launches a dialog which allows user to invite other users to this event.
     */
    public void eventInvite() {
        InviteDialog dialog = new InviteDialog(this);
        dialog.show(getSupportFragmentManager(), "Example");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.three_dot_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_event_topR) {
            editEvent();
        } else if (item.getItemId() == R.id.delete_event_topR) {
            if (this.googleAcct != null) {
                this.task.execute(this.event.getId(), "delete");
            } else {
                deleteFirebaseEvent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When edit button in menu is clicked, this method is called to move to ActivityCreateEventPage
     * where user can edit the event information.
     */
    private void editEvent() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        intent.putExtra("event to edit", this.event);
        startActivity(intent);
    }

    /**
     * When delete button in menu is clicked, this method is called to delete the document of this
     * event as shown on this page from Firebase events collection. Thereafter, returns to
     * ActivityUpcomingPage.
     */
    private void deleteFirebaseEvent() {
        this.c.document(this.event.getId())
                .delete()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    Intent intent = new Intent(this, ActivityUpcomingPage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
    }

    /**
     * Fetches the event document with ID as specified by parameter.
     * @param eventID String ID of the Firebase document in events collection
     */
    private void fetchFirebaseEventDetails(String eventID) {
        if (this.googleAcct != null) {
            try {
                this.task.execute(eventID, "query").get();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            this.fStore.collection("events")
                    .document(eventID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.d(TAG, "Document found");
                            event = processFirebaseEventDocument(documentSnapshot);
                            setView(event);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error reading document");
                        }
                    });
        }
    }

    /**
     * Processes Firebase document from events collection into a CustomEventFromFirebase
     * @param doc Firebase document from events collection
     * @return CustomEventFromFirebase with details as specified in parameter doc
     */
    private CustomEventFromFirebase processFirebaseEventDocument(DocumentSnapshot doc) {
        String eventTitle = (String) doc.get("eventTitle");
        String startDate = (String) doc.get("startDate");
        String endDate = (String) doc.get("endDate");
        String startTime = (String) doc.get("startTime");
        String endTime = (String) doc.get("endTime");
        String docID = doc.getId();
        String description = (String) doc.get("description");
        CustomEventFromFirebase e = new CustomEventFromFirebase(eventTitle, startDate, endDate, startTime, endTime, docID);
        e.setDescription(description);
        return e;
    }

    /**
     * Displays data from CustomEvent into the respective fields in the interface.
     * @param event CustomEvent which encapsulates the data representing an event in the calendar
     */
    @SuppressLint("SetTextI18n")
    private void setView(CustomEvent event) {
        this.eventTitle.setText(event.getTitle());
        String startDate = HelperMethods.formatDateForView(event.getStartDate());
        String endDate = HelperMethods.formatDateForView(event.getEndDate());
        if (startDate.equalsIgnoreCase(endDate)) {
            this.eventDate.setText("Date: " + startDate);
        } else {
            this.eventDate.setText("Date: " + startDate + " to " + endDate);
        }
        this.eventTime.setText("Time: " + event.getStartTime() + " to " + event.getEndTime());
        if (event.getDescription().equals("")) {
            this.eventDescription.setText("No event description.");
        } else {
            this.eventDescription.setText(event.getDescription());
        }
    }

    /**
     * Queries to do items from Firebase to do collection.
     * @param eventID String ID of the Firebase document in events collection
     */
    private void fetchToDos(String eventID) {
        this.fStore.collection("todo")
                .whereEqualTo("eventID", eventID)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            processFirebaseToDoDocument(doc);
                        }
                        toDoAdapter = new EventDetailsToDoAdapter(listOfToDos);
                        allToDos.setAdapter(toDoAdapter);
                    }
                });
    }

    /**
     * Processes Firebase document from to do collection by adding to a list of to dos associated
     * with this event.
     * @param doc Firebase document from to do collection
     */
    private void processFirebaseToDoDocument(QueryDocumentSnapshot doc) {
        String todoID = doc.getId();
        String title = (String) doc.get("title");
        String date = (String) doc.get("date");
        boolean completed = (boolean) doc.get("completed");
        CustomToDo todo = new CustomToDo(todoID, title, date, completed);
        this.listOfToDos.add(todo);
    }

    /**
     * Converts Google's Event object from Google Calendar API into a CustomEventFromGoogle.
     * @param e Event from Google Calendar
     * @return CustomEventFromGoogle encapsulating required data
     */
    private CustomEventFromGoogle processGoogleEvent(com.google.api.services.calendar.model.Event e) {
        String title = e.getSummary();
        String startDate;
        String startTime;
        String endDate;
        String endTime;
        String eventID = e.getId();
        String eventDescription = e.getDescription() == null ? "" : e.getDescription();
        CustomEventFromGoogle event = null;
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
                    startTime = "All day";
                }
                event = new CustomEventFromGoogle(title, newDate, endDate, startTime, endTime, eventID);
                event.setDescription(eventDescription);
            }
        }
        return event;
    }

    /**
     * Sends a notification invite to another user to invite to this event.
     * @param userEmail email of the user current user wants to invite to this event
     */
    public void sendNotification(String userEmail) {
        this.fStore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String respondentID = "";
                            for (QueryDocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                                respondentID = document.getId();
                            }
                            Map<String, Object> data = new HashMap<>();
                            data.put("hostID", userID);
                            data.put("respondentID", respondentID);
                            data.put("dateOfNotification", HelperMethods.getCurrDate());
                            data.put("message", event.getAllDetails());
                            data.put("eventID", event.getId());
                            data.put("hasResponded", false);
                            data.put("response", null);
                            fStore.collection("Notifications").add(data);
                            Toast.makeText(ActivityEventDetailsPage.this, "Done!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Pushes changes in toggling to do items to Firebase, if any.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (this.toDoAdapter != null) {
            pushToDo();
        }
    }

    /**
     * Gets to do items whose completed states have been changed and updates their states on Firebase
     * to do collection.
     */
    private void pushToDo() {
        Set<CustomToDo> toggled = this.toDoAdapter.getMyToggledToDos();
        for (CustomToDo todo : toggled) {
            this.fStore.collection("todo").document(todo.getID()).set(customToDoToMap(todo));
        }
    }

    /**
     * Converts CustomToDo into a Map where keys are Strings and values are Objects representing the
     * data of the to do item.
     * @param customToDo CustomToDo item to convert
     * @return Map containing details of the CustomToDo item
     */
    private Map<String, Object> customToDoToMap(CustomToDo customToDo) {
        Map<String, Object> todoDetails = new HashMap<>();
        todoDetails.put("userID", userID);
        todoDetails.put("date", customToDo.getDate());
        todoDetails.put("eventID", this.event.getId());
        todoDetails.put("title", customToDo.getTitle());
        todoDetails.put("completed", customToDo.getCompleted());
        return todoDetails;
    }

    /**
     * Google Calendar API handler class for asynchronous task handling.
     */
    @SuppressLint("StaticFieldLeak")
    private class RequestAuth extends AsyncTask<String, Void, Boolean> {
        /**
         * The Google Calendar Event.
         */
        private Event asyncEvent;

        /**
         * The task to execute.
         */
        private String command;

        /**
         * Calls respective methods depending on the String action passed as parameter.
         * @param strings String[] containing String eventID of the event to be queried or deleted,
         *                and String command whether to query or delete the event
         * @return Boolean indicating if task was successful
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            this.command = strings[1];
            if (this.command.equals("delete")) {
                try {
                    deleteGoogleEvent(calendarAuth(), strings[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            } else {
                try {
                    this.asyncEvent = fetchGoogleEvent(calendarAuth(), strings[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }

        /**
         * Builds a new authorized API client service.
         * @return authorised Google Calendar service
         * @throws IOException if credentials cannot be found
         */
        private Calendar calendarAuth() throws IOException {
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return service;
        }

        /**
         * Creates an authorized Credential object.
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
            // Load client secrets.
            InputStream in = ActivityEventDetailsPage.this.getAssets().open(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            File tokenFolder = new File(ActivityEventDetailsPage.this.getFilesDir(), "tokens");

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

        /**
         * Method which handles what to do after task is successful, depending on the String action specified.
         * @param aBoolean indicating if task is successful
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean && this.command.equals("delete")) {
                Intent i = new Intent(ActivityEventDetailsPage.this, ActivityUpcomingPage.class);
                startActivity(i);
            } else if (aBoolean && this.command.equals("query")) {
                ActivityEventDetailsPage.this.event = processGoogleEvent(this.asyncEvent);
                setView(ActivityEventDetailsPage.this.event);
            }
        }

        /**
         * Deletes current event from Google Calendar.
         * @param service Google Calendar service authorised using calendarAuth() method
         * @param eventID ID of Google Event to be deleted
         * @throws IOException Exception that might occur during the deletion of current event
         */
        private void deleteGoogleEvent(Calendar service, String eventID) throws IOException {
            service.events().delete("primary", eventID).execute();
        }

        /**
         * Queries Google Event from Google Calendar.
         * @param service Google Calendar service authorised using calendarAuth() method
         * @param eventID ID of Google Event to be queried
         * @return Google Event as specified by String eventID parameter
         * @throws IOException Exception that might occur during the querying of current event
         */
        private Event fetchGoogleEvent(Calendar service, String eventID) throws IOException {
            return service.events().get("primary", eventID).execute();
        }
    }
}