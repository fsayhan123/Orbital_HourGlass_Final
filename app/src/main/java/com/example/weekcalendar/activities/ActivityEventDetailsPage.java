package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.adapters.EventDetailsToDoAdapter;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.customclasses.event.CustomEventFromFirebase;
import com.example.weekcalendar.customclasses.event.CustomEventFromGoogle;
import com.example.weekcalendar.helperclasses.Dialog;
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
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.DAYS;

public class ActivityEventDetailsPage extends AppCompatActivity {
    private static final String TAG = ActivityEventDetailsPage.class.getSimpleName();

    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventDescription;
    private Button inviteEvent;
    private RecyclerView allToDos;
    private EventDetailsToDoAdapter e;

    private CustomEvent event;
    private String eventID;
    private List<CustomToDo> listOfToDos;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    // Get data from Google
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = CalendarScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private GoogleSignInAccount acct;
    private ActivityEventDetailsPage.RequestAuth task = new ActivityEventDetailsPage.RequestAuth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("events");

        this.acct = GoogleSignIn.getLastSignedInAccount(this);

        // Get Intent of the event item selected
        Intent intent = getIntent();
        this.eventID = intent.getStringExtra("eventID");

        // Links to XML
        this.eventTitle = findViewById(R.id.event_title);
        this.eventDate = findViewById(R.id.event_date);
        this.eventTime = findViewById(R.id.event_time);
        this.eventDescription = findViewById(R.id.event_description);
        this.inviteEvent = findViewById(R.id.invite_event);
        this.inviteEvent.setOnClickListener(v -> eventInvite());
        this.allToDos = findViewById(R.id.all_todo);
        LinearLayoutManager manager = new LinearLayoutManager(ActivityEventDetailsPage.this);
        this.allToDos.setHasFixedSize(true);
        this.allToDos.setLayoutManager(manager);
        this.listOfToDos = new ArrayList<>();

        try {
            this.fetchEventDetails(this.eventID);
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        fetchToDos(this.eventID);

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.event_details);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityMainCalendar.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.three_dot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_event_topR) {
            editEvent();
        } else if (item.getItemId() == R.id.delete_event_topR) {
            if (acct != null) { // if logged in to a Google account
                this.task.execute(this.event.getId(), "delete");
            } else {
                deleteEvent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

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

    private void fetchEventDetails(String eventID) throws ExecutionException, InterruptedException {
        if (this.acct != null) {
            Toast.makeText(this, "in a google account", Toast.LENGTH_SHORT).show();
            this.task.execute(eventID, "fetch").get();
        } else {
            this.fStore.collection("events")
                    .document(eventID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            assert documentSnapshot != null;
                            Log.d(TAG, "document found");
                            event = documentSnapshotToCustomEventFromFirebase(documentSnapshot);
                            setView(event);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "error reading document");
                        }
                    });
        }
    }

    private CustomEvent processGoogleEvent(com.google.api.services.calendar.model.Event e) {
        String title = e.getSummary();
        Log.d(TAG, "********" + title);
        String startDate;
        String startTime;
        String endDate;
        String endTime;
        String eventID = e.getId();
        String eventDescription = e.getDescription() == null ? "" : e.getDescription();
        CustomEvent event = null;
        if (e.getStart().get("dateTime") == null) { // full day event
            startDate = e.getStart().get("date").toString();
            startTime = "All day";
            endDate = startDate; // on Google it is instantiated as the next day
            endTime = "23:59";
        } else {
            String[] startDateAndTimeSplit = e.getStart().get("dateTime").toString().split("T");
            startDate = startDateAndTimeSplit[0];
            startTime = startDateAndTimeSplit[1].substring(0, 5); // getOriginalStart?
            String[] endDateAndTimeSplit = e.getEnd().get("dateTime").toString().split("T");
            endDate = endDateAndTimeSplit[0];
            endTime = endDateAndTimeSplit[1].substring(0, 5);
        }
        if (startDate.equals(endDate)) { // <= 1 day
            event = new CustomEventFromGoogle(title, startDate, endDate, startTime, endTime, eventID);
            event.setDescription(eventDescription);
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
                event = new CustomEventFromGoogle(title, newDate, endDate, startTime, endTime, eventID);
                event.setDescription(eventDescription);
            }
        }
        assert event != null;
        return event;
    }

    private CustomEventFromFirebase documentSnapshotToCustomEventFromFirebase(DocumentSnapshot doc) {
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

    private void editEvent() {
        Intent intent = new Intent(this, ActivityCreateEventPage.class);
        intent.putExtra("event to edit", this.event);
        startActivity(intent);
    }

    private void deleteEvent() {
        this.c.document(this.event.getId())
                .delete()
                .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        Intent intent = new Intent(this, ActivityUpcomingPage.class);
        startActivity(intent);
    }

    public void eventInvite() {
        Dialog dialog = new Dialog(this);
        dialog.show(getSupportFragmentManager(), "Example");
    }

    public void sendNotification(String userEmail) {
        this.fStore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String respondentID = "";
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                respondentID = document.getId();
                            }
                            //need to for loop this to accept multiple, leave it as is for now
                            Map<String, Object> data = new HashMap<>();
                            data.put("hostID", userID);
                            data.put("respondentID", respondentID);
                            data.put("dateOfNotification", HelperMethods.getCurrDate());
                            data.put("message", event.getAllDetails());
                            data.put("eventID", event.getId());
                            data.put("hasResponded", false);
                            data.put("response", null);
                            ActivityEventDetailsPage.this.fStore.collection("Notifications").add(data);
                        }
                    }
                });
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
    }

    public void fetchToDos(String eventID) {
        this.fStore.collection("todo")
                .whereEqualTo("eventID", eventID)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String todoID = doc.getId();
                            String title = (String) doc.get("title");
                            String date = (String) doc.get("date");
                            boolean completed = (boolean) doc.get("completed");
                            CustomToDo todo = new CustomToDo(todoID, title, date, completed);
                            listOfToDos.add(todo);
                        }
                        e = new EventDetailsToDoAdapter(listOfToDos);
                        allToDos.setAdapter(e);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.e != null) {
            pushToDo();
        }
    }

    private void pushToDo() {
        Set<CustomToDo> toggled = this.e.getMyToggledToDos();
        for (CustomToDo todo : toggled) {
            this.fStore.collection("todo").document(todo.getID()).set(customToDoToMap(todo));
        }
    }

    public Map<String, Object> customToDoToMap(CustomToDo todo) {
        Map<String, Object> todoDetails = new HashMap<>();
        todoDetails.put("userID", userID);
        todoDetails.put("date", todo.getDate());
        todoDetails.put("eventID", this.event.getId());
        todoDetails.put("title", todo.getTitle());
        todoDetails.put("completed", todo.getCompleted());
        return todoDetails;
    }

    private class RequestAuth extends AsyncTask<String, Void, Boolean> {
        private Event asyncEvent;
        private String command;

        @Override
        protected Boolean doInBackground(String... strings) {
            this.command = strings[1];
            if (this.command.equals("delete")) {
                try {
                    pushData(strings);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            } else {
                assert this.command.equals("fetch");
                Log.d(TAG, "*********" + "fetching");
                try {
                    this.asyncEvent = pullData(strings);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }

        private Event pullData(String... strings) throws IOException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return fetchGoogleEvent(service, strings[0]);
        }

        private void pushData(String... strings) throws IOException {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            deleteGoogleEvent(service, strings[0]);
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

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean && this.command.equals("delete")) {
                Intent i = new Intent(ActivityEventDetailsPage.this, ActivityUpcomingPage.class);
                startActivity(i);
            } else if (aBoolean && this.command.equals("fetch")) {
                ActivityEventDetailsPage.this.event = processGoogleEvent(this.asyncEvent);
                setView(ActivityEventDetailsPage.this.event);
            }
        }

        private void deleteGoogleEvent(Calendar service, String eventID) throws IOException {
            service.events().delete("primary", eventID).execute();
        }

        private Event fetchGoogleEvent(Calendar service, String eventID) throws IOException {
            return service.events().get("primary", eventID).execute();
        }
    }
}