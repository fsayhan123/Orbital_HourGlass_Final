package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityIndividualNotification extends AppCompatActivity {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityIndividualNotification.class.getSimpleName();

    /**
     * Firebase information
     */
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    /**
     * UI variables
     */
    private TextView date;
    private TextView message;
    private TextView from;
    private TextView response;
    private Button respondButton;
    private LinearLayout buttonLayout;
    private Button rejectButton;

    /**
     * Firebase document references
     */
    private String notifID;
    private String responseFormID = null;
    private String eventID = null;

    /**
     * Sets up ActivityIndividualNotification when it is opened.
     * First, sets up Firebase.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling setViews() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_notification);

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        setupXMLItems();

        Intent intent = getIntent();
        this.notifID = intent.getStringExtra("notificationID");
        fetchData();
    }

    /**
     * Sets up layout for ActivityIndividualNotification.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.notifications_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityNotificationsPage.class));
        });

        LinearLayout notificationContents = findViewById(R.id.notification_contents);
        this.date = findViewById(R.id.notification_date);
        this.message = findViewById(R.id.notification_message);
        this.from = findViewById(R.id.from);
        this.response = findViewById(R.id.response);

        this.respondButton = findViewById(R.id.respond_button);
        this.respondButton.setOnClickListener(v -> respond());

        this.buttonLayout = findViewById(R.id.button_layouts);
    }

    /**
     * Queries for data from Firebase Notifications collection and sets up user interface
     * accordingly with the details.
     */
    private void fetchData() {
        this.fStore.collection("Notifications")
                .document(this.notifID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        setViews(documentSnapshot);
                        if ((boolean) documentSnapshot.get("hasResponded")) {
                            respondButton.setText("Delete Notification");
                            respondButton.setOnClickListener(v -> delete());
                            return;
                        }

                        if (documentSnapshot.get("responseFormID") != null) {
                            assert documentSnapshot.get("eventID") == null;
                            responseFormID = (String) documentSnapshot.get("responseFormID");
                        }
                        if (documentSnapshot.get("eventID") != null) {
                            assert documentSnapshot.get("responseFormID") == null;
                            eventID = (String) documentSnapshot.get("eventID");
                            respondButton.setText("Accept");
                            rejectButton = new Button(ActivityIndividualNotification.this);
                            rejectButton.setText("Reject");
                            buttonLayout.addView(rejectButton);
                            rejectButton.setOnClickListener(v -> reject());
                        }
                    }
                });
    }

    /**
     * Sets up user interface with data from Firebase document doc passed as parameter.
     * @param doc Firebase Notifications document
     */
    private void setViews(DocumentSnapshot doc) {
        String date = HelperMethods.formatDateForView((String) Objects.requireNonNull(doc.get("dateOfNotification")));
        String message = (String) doc.get("message");
        String hostID = (String) doc.get("hostID");
        this.fStore.collection("users")
                .document(Objects.requireNonNull(hostID))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        from.setText("From: " + documentSnapshot.get("fName"));
                    }
                });
        boolean hasResponded = (boolean) doc.get("hasResponded");
        this.date.setText(date);
        this.message.setText(message);
        if (hasResponded) {
            if (doc.get("responseFormID") != null) {
                List<String> userResponses = (List<String>) doc.get("response");
                Collections.sort(Objects.requireNonNull(userResponses));
                StringBuilder s = new StringBuilder();
                s.append("Responses: ");
                for (String response : userResponses) {
                    s.append(HelperMethods.formatDateForView(response)).append(", ");
                }
                response.setText(s.substring(0, s.length() - 2));
            } else {
                String userResponse = (String) doc.get("response");
                response.setText(userResponse);
            }
        }
    }

    /**
     * Deletes this notification from Firebase Notifications collection.
     */
    private void delete() {
        this.fStore.collection("Notifications")
                .document(this.notifID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(ActivityIndividualNotification.this, ActivityNotificationsPage.class));
                    }
                });
    }

    /**
     * Handles different response situations.
     * If responding to a shared event, links to ActivityAcceptSharedEvent.
     * Else, acts as an accept button.
     */
    private void respond() {
        Intent i;
        if (this.responseFormID != null) {
            i = new Intent(this, ActivityAcceptSharedEvent.class);
            i.putExtra("response form ID", this.responseFormID);
            i.putExtra("notification ID", this.notifID);
        } else {
            i = new Intent(this, ActivityNotificationsPage.class);
            this.fStore.collection("events")
                    .document(this.eventID)
                    .update("participants", FieldValue.arrayUnion(Objects.requireNonNull(fAuth.getCurrentUser()).getUid()))
                    .addOnSuccessListener(v -> Log.d(TAG, "Added user to event!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding user to event.", e));
            Map<String, Object> data = new HashMap<>();
            data.put("hasResponded", true);
            data.put("response", "accepted");
            this.fStore.collection("Notifications")
                    .document(this.notifID)
                    .update(data)
                    .addOnSuccessListener(v -> Log.d(TAG, "Normal event invite accepted!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error accepting normal event invite.", e));
            i.putExtra("eventID", this.eventID);
        }
        startActivity(i);
    }

    /**
     * Called when reject button is clicked, to reject invitation to a normal, personal event.
     */
    private void reject() {
        Toast.makeText(this, "Event invite rejected.", Toast.LENGTH_SHORT).show();
        Map<String, Object> data = new HashMap<>();
        data.put("hasResponded", true);
        data.put("response", "rejected");
        Intent i = new Intent(this, ActivityNotificationsPage.class);
        this.fStore.collection("Notifications")
                .document(this.notifID)
                .update(data)
                .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
        startActivity(i);
    }
}