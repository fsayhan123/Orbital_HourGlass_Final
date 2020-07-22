package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityIndividualNotification extends AppCompatActivity {
    private static final String TAG = ActivityIndividualNotification.class.getSimpleName();

    //Firebase Classes
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    //Notification Content
    private LinearLayout notificationContents;
    private TextView date;
    private TextView message;
    private TextView from;
    private TextView response;
    private Button respondButton;
    private LinearLayout buttonLayout;
    private Button rejectButton;

    private String notifID;
    private String responseFormID = null;
    private String eventID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_notification);

        //Setup Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();

        //Setup local activity variables
        this.notificationContents = findViewById(R.id.notification_contents);
        this.date = findViewById(R.id.notification_date);
        this.message = findViewById(R.id.notification_message);
        this.from = findViewById(R.id.from);
        this.response = findViewById(R.id.response);

        this.respondButton = findViewById(R.id.respond_button);
        this.respondButton.setOnClickListener(v -> respond());

        this.buttonLayout = findViewById(R.id.button_layouts);

        //get the document then set the text
        Intent intent = getIntent();
        this.notifID = intent.getStringExtra("notificationID");
        this.fStore.collection("Notifications")
                .document(this.notifID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        setText(documentSnapshot);
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
                    .update("participants", FieldValue.arrayUnion(fAuth.getCurrentUser().getUid()))
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

    private void setText(DocumentSnapshot doc) {
        Log.d(TAG, "!!!!!!!!!!!!!!!!" + doc.getId());
        String date = HelperMethods.formatDateForView((String) doc.get("dateOfNotification"));
        String message = (String) doc.get("message");
        String hostID = (String) doc.get("hostID");
        this.fStore.collection("users")
                .document(hostID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        from.setText("From: " + (String) documentSnapshot.get("fName"));
                    }
                });
        boolean hasResponded = (boolean) doc.get("hasResponded");
        this.date.setText(date);
        this.message.setText(message);
        if (hasResponded) {
            if (doc.get("responseFormID") != null) {
                List<String> userResponses = (List<String>) doc.get("response");
                Collections.sort(userResponses);
                response.setText(userResponses.toString());
            } else {
                String userResponse = (String) doc.get("response");
                response.setText(userResponse);
            }
        }
    }
}