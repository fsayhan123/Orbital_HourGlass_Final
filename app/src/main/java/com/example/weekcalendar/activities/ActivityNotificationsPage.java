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
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityNotificationsPage.class.getSimpleName();

    /**
     * Firebase infromation
     */
    private String userID;
    private CollectionReference notification;

    /**
     * UI variables
     */
    private ArrayList<CustomNotification> customNotificationArrayList;
    private RecyclerView mRecyclerView;
    private NotificationsRecyclerViewAdapter mAdapter;

    /**
     * Sets up ActivityNotificationsPage when it is opened.
     * First, sets up Firebase account.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling getNotificationFromFirebase() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_page);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        this.notification = fStore.collection("Notifications");

        setupXMLItems();

        getNotificationFromFirebase();
    }

    /**
     * Sets up layout for ActivityNotificationsPage.
     */
    private void setupXMLItems() {
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.notifications_toolbar));
        navDrawer.setupNavDrawerPane();

        this.mRecyclerView = findViewById(R.id.notification_view);
        this.mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Queries data from Firebase Notifications collection involving the user.
     */
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

    /**
     * Links to ActivityIndividualNotification when a specific notification view is clicked.
     * @param position integer position of notification clicked in the RecyclerView's adapter
     */
    @Override
    public void onNotificationClick(int position) {
        CustomNotification customNotification = customNotificationArrayList.get(position);
        Intent intent = new Intent(this, ActivityIndividualNotification.class);
        intent.putExtra("notificationID", customNotification.getID());
        startActivity(intent);
    }
}