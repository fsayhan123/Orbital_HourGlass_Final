package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.weekcalendar.customclasses.event.CustomEvent;
import com.example.weekcalendar.helperclasses.Dialog;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityEventDetailsPage extends AppCompatActivity {
    private static final String TAG = ActivityEventDetailsPage.class.getSimpleName();

    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;

    private CustomEvent event;

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
        this.event = intent.getParcelableExtra("event");

        // Links to XML
        this.eventTitle = findViewById(R.id.event_title);
        this.eventDate = findViewById(R.id.event_date);
        this.eventTime = findViewById(R.id.event_time);

        this.setView(this.event);

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.event_details);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityUpcomingPage.class));
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
                ActivityEventDetailsPage.RequestAuth task = new ActivityEventDetailsPage.RequestAuth();
                task.execute(this.event.getId());
            } else {
                deleteEvent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setView(CustomEvent event) {
        eventTitle.setText(event.getTitle());
        String startDate = HelperMethods.formatDateForView(event.getStartDate());
        String endDate = HelperMethods.formatDateForView(event.getEndDate());
        if (startDate.equalsIgnoreCase(endDate)) {
            eventDate.setText("Date: " + startDate);
        } else {
            eventDate.setText("Date: " + startDate + " to " + endDate);
        }
        eventTime.setText("Time: " + event.getStartTime() + " to " + event.getEndTime());
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

    public void eventInvite(View view) {
        String link = "https://www.example.com/?id=" + this.event.getId();
        System.out.println(link);
        //Generate the dynamic link
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/"))
                .setDomainUriPrefix("https://orbitalweekcalendar.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.example.weekcalendar")
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {

                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            Dialog dialog = new Dialog(shortLink.toString());
                            dialog.show(getSupportFragmentManager(), "Example");
                            System.out.println(dialog.getStatus());
                            if (dialog.getStatus()) {
                                String userEmail = dialog.getEmail();
                                ActivityEventDetailsPage.this.sendNotification(userEmail, shortLink.toString());
                            }
                        } else {
                            System.out.println("error");
                        }
                    }
                });
    }

    private void sendNotification(String userEmail, String url) {
        this.fStore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String userID = "";
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                userID = document.getId();
                            }
                            //need to for loop this to accept multiple, leave it as is for now
                            Map<String, Object> data = new HashMap<>();
                            data.put("Date", "2020-06-05");
                            data.put("Message", url);
                            data.put("userID", userID);
                            ActivityEventDetailsPage.this.fStore.collection("Notifications").add(data);
                        }
                    }
                });
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
            if (aBoolean) {
                Intent i = new Intent(ActivityEventDetailsPage.this, ActivityUpcomingPage.class);
                startActivity(i);
            }
        }

        private void deleteGoogleEvent(Calendar service, String eventID) throws IOException {
            service.events().delete("primary", eventID).execute();
        }
    }
}
