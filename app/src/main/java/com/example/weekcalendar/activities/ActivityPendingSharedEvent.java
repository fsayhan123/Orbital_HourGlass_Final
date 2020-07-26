package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.PendingSharedEventsRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomPendingShared;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ActivityPendingSharedEvent extends AppCompatActivity implements PendingSharedEventsRecyclerViewAdapter.OnSharedEventListener {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityPendingSharedEvent.class.getSimpleName();

    /**
     * Firebase information
     */
    private FirebaseFirestore fStore;
    private String userID;

    /**
     * UI variables
     */
    private ArrayList<CustomPendingShared> customPendingSharedArrayList;
    private RecyclerView mRecyclerView;
    private PendingSharedEventsRecyclerViewAdapter mAdapter;

    /**
     * Sets up ActivityPendingSharedEvent when it is opened.
     * First, sets up Firebase or Google account.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling getPendingSharedEvents() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_shared_event);

        // Setup firebase Variables
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        setupXMLItems();

        getPendingSharedEvents();
    }


    /**
     * Sets up layout for ActivityPendingSharedEvent.
     */
    private void setupXMLItems() {
        // Set up nav drawer
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.pending_shared_event_toolbar));
        navDrawer.setupNavDrawerPane();

        // Set up recycler View
        this.mRecyclerView = findViewById(R.id.pending_shared_event_view);
        this.mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Fetches the list of pending shared events and adds them to a list
     * Create a recyclerViewAdapter from this list and generate the recycler view for the page.
     */
    protected void getPendingSharedEvents() {
        this.customPendingSharedArrayList = new ArrayList<>();
        this.fStore.collection("responses")
                .whereEqualTo("hostID", this.userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String responseID = document.getId();
                                String title = (String) document.get("title");
                                CustomPendingShared shared = new CustomPendingShared(responseID, title);
                                customPendingSharedArrayList.add(shared);
                            }
                            mAdapter = new PendingSharedEventsRecyclerViewAdapter(customPendingSharedArrayList, ActivityPendingSharedEvent.this);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }

    /**
     *Once event is clicked, it brings user to the ActivitySelectSharedEvent page
     * based on which pending shared event was chosen.
     * @param position position of recycler view which was clicked
     */

    @Override
    public void onEventClick(int position) {
        CustomPendingShared customPendingSharedEvent = customPendingSharedArrayList.get(position);
        Intent intent = new Intent(this, ActivitySelectSharedEvent.class);
        intent.putExtra("responseID", customPendingSharedEvent.getID());
        startActivity(intent);
    }
}