package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weekcalendar.helperclasses.SetupNavDrawer;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class FetchGoogleCalendarEvents extends AppCompatActivity {
    private static final String APPLICATION_NAME = "WeekCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private SetupNavDrawer navDrawer;

    private static List<Event> fetchedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_google_calendar_events);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        try {
            this.fetchedEvents = new RequestAuth().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            this.fetchedEvents = new ArrayList<>();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.fetchedEvents = new ArrayList<>();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        for (Event e : this.fetchedEvents) {
            try {
                Toast.makeText(this, e.getStart().toPrettyString() + " " + e.getEnd().toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Set up navigation drawer
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.fetch_toolbar));
        this.navDrawer.setupNavDrawerPane();
    }


    // copied
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
            InputStream in = FetchGoogleCalendarEvents.this.getAssets().open(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            File tokenFolder = new File(FetchGoogleCalendarEvents.this.getFilesDir(), "tokens");

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