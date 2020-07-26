package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.NotificationsRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomNotification;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ActivityNotificationsPage extends AppCompatActivity implements NotificationsRecyclerViewAdapter.OnNotificationListener {
    private static final String TAG = ActivityNotificationsPage.class.getSimpleName();

    private String userID;
    private CollectionReference notification;

    //Recycler View Variables
    private ArrayList<CustomNotification> customNotificationArrayList;
    private RecyclerView mRecyclerView;
    private NotificationsRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_page);

        // Setup firebase Variables
        // Firebase variables
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        this.notification = fStore.collection("Notifications");

        setupXMLItems();

        getNotificationFromFirebase();
    }

    private void setupXMLItems() {
        // Nav Drawer
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.notifications_toolbar));
        navDrawer.setupNavDrawerPane();

        // Set up recycler View
        this.mRecyclerView = findViewById(R.id.notification_view);
        this.mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);
    }

    private void getNotificationFromFirebase() {
        this.customNotificationArrayList = new ArrayList<>();
        this.notification.whereEqualTo("respondentID", this.userID)
                .orderBy("dateOfNotification")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            Log.d(TAG, "onSuccess: LIST NOT EMPTY");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String notifID = document.getId();
                                String date = (String) document.get("dateOfNotification");
                                String message = (String) document.get("message");
                                boolean responseStatus = (boolean) document.get("hasResponded");
                                CustomNotification notif = new CustomNotification(date, message, notifID, responseStatus);
                                customNotificationArrayList.add(notif);
                            }
                            mAdapter = new NotificationsRecyclerViewAdapter(customNotificationArrayList, ActivityNotificationsPage.this);
                            mRecyclerView.setAdapter(mAdapter);
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

    @Override
    public void onNotificationClick(int position) {
        CustomNotification customNotification = customNotificationArrayList.get(position);
        Intent intent = new Intent(this, ActivityIndividualNotification.class);
        intent.putExtra("notificationID", customNotification.getID());
        startActivity(intent);
    }
}