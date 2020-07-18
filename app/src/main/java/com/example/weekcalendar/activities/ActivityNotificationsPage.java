package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.NotificationsRecyclerViewAdapter;
import com.example.weekcalendar.adapters.UpcomingRecyclerViewAdapter;
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
import java.util.HashMap;

public class ActivityNotificationsPage extends AppCompatActivity implements NotificationsRecyclerViewAdapter.OnNotificationListener {

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference notification;

    //Nav Drawer
    private SetupNavDrawer navDrawer;

    //Recycler View Variables
    private ArrayList<CustomNotification> customNotificationArrayList;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NotificationsRecyclerViewAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_page);

        //Set up nav drawer
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.notifications_toolbar));
        this.navDrawer.setupNavDrawerPane();

        //Set up recycler View
        this.mRecyclerView = findViewById(R.id.notification_view);
        this.mRecyclerView.setHasFixedSize(true);
        this.layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);




        //Setup firebase Variables
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.notification = fStore.collection("Notifications");

        getNotificationFromFirebase();
    }

    protected void getNotificationFromFirebase() {
        this.customNotificationArrayList = new ArrayList<>();
        this.notification.whereEqualTo("userID", this.userID)
                .orderBy("Date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("TAG", "onSuccess: LIST EMPTY");
                        } else {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String ID = document.getId();
                                String date = (String) document.get("Date");
                                String message = (String) document.get("Message");
                                CustomNotification notif = new CustomNotification(date, message, ID);
                                customNotificationArrayList.add(notif);
                            }
                            System.out.print(customNotificationArrayList);
                            mAdapter = new NotificationsRecyclerViewAdapter(customNotificationArrayList, ActivityNotificationsPage.this);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "hello!!!!!!!!!!!!!!!!!!! " + e.getLocalizedMessage());
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
