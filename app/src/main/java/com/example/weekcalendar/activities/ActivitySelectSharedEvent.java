package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.ResponseRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomResponse;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivitySelectSharedEvent extends AppCompatActivity {

    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivitySelectSharedEvent.class.getSimpleName();

    /**
     * Firebase information
     */
    private FirebaseFirestore fStore;
    private String userID;

    /**
     * UI variables
     */
    private String responseID;
    private TextView title;
    private HashMap<String, ArrayList<String>> responses;
    private ArrayList<String> selectedDates = new ArrayList<>();
    private TextInputLayout startTimelayout;
    private Button selectStartTime;
    private TextInputLayout endTimeLayout;
    private Button selectEndTime;
    private ArrayList<CustomResponse> CustomResponseArrayList;
    private RecyclerView mRecyclerView;
    private ResponseRecyclerViewAdapter mAdapter;


    /**
     * Sets up ActivitySelectSharedEvent when it is opened.
     * First, sets up Firebase or Google account.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling fetchResponses() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shared_event);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        Intent intent = getIntent();
        this.responseID = intent.getStringExtra("responseID");

        setupXMlItems();

        fetchResponses();
    }

    /**
     * Sets up layout for ActivitySelectSharedEvent.
     */
    private void setupXMlItems() {
        // Setup toolbar
        Toolbar tb = findViewById(R.id.create_shared_event_toolbar);
        setSupportActionBar(tb);

        // sets up back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityPendingSharedEvent.class));
        });

        this.title = findViewById(R.id.select_shared_event_title);

        this.startTimelayout = findViewById(R.id.select_start_time_layout);

        this.selectStartTime = findViewById(R.id.select_start_time);
        this.selectStartTime.setOnClickListener(v -> openSelectTimeDialog(this.selectStartTime));

        this.endTimeLayout = findViewById(R.id.select_end_time_layout);

        this.selectEndTime = findViewById(R.id.select_end_time);
        this.selectEndTime.setOnClickListener(v -> openSelectTimeDialog(this.selectEndTime));

        Button confirmEventButton = findViewById(R.id.confirm_event_button);
        confirmEventButton.setOnClickListener(v -> confirmEvent());

        // Recycler View Setup
        this.mRecyclerView = findViewById(R.id.selecting_shared_event_date);
        this.mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);

        this.CustomResponseArrayList = new ArrayList<>();
    }

    /**
     * Fetches the responses associated with the specific responseID for the activity.
     * On success of pulling the data, recycler view with dates will be created
     */
    private void fetchResponses() {
        this.fStore.collection("responses")
                .document(this.responseID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ActivitySelectSharedEvent.this.title.setText(Objects.requireNonNull(documentSnapshot.get("title")).toString());

                        // Get the responses, order by array length, then put into recycler view
                        HashMap<String, ArrayList<String>> hashMap = (HashMap<String, ArrayList<String>>) documentSnapshot.get("responses");
                        for (Map.Entry<String, ArrayList<String>> entry : Objects.requireNonNull(hashMap).entrySet()) {
                            String date = entry.getKey();
                            int count = entry.getValue().size();
                            CustomResponse response = new CustomResponse(date, count);
                            ActivitySelectSharedEvent.this.CustomResponseArrayList.add(response);
                        }

                        ActivitySelectSharedEvent.this.responses = hashMap;

                        mAdapter = new ResponseRecyclerViewAdapter(ActivitySelectSharedEvent.this.CustomResponseArrayList, new ResponseRecyclerViewAdapter.OnDateCheckListener() {
                            @Override
                            public void onDateCheck(String item) {
                                selectedDates.add(item);
                            }

                            @Override
                            public void onDateUncheck(String item) {
                                selectedDates.remove(item);
                            }
                        });
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
    }

    /**
     * If inputs for the events are valid, will create a firebase event
     * with the given information for all users which are indicated available to attend.
     */

    private void confirmEvent() {
        if (this.selectedDates.size() > 1)  {
            Toast.makeText(this, "Please select only one date!", Toast.LENGTH_SHORT).show();
        } else if (this.selectedDates.size() == 0) {
            Toast.makeText(this, "Please select a date!", Toast.LENGTH_SHORT).show();
        } else if (checkFields()) {
            String date = this.selectedDates.get(0);
            String dateFormatted = HelperMethods.formatDateWithDash(date);
            ArrayList<String> users = this.responses.get(dateFormatted);
            Objects.requireNonNull(users).add(this.userID);

            String startTime = HelperMethods.formatTimeTo24H(this.selectStartTime.getText().toString());
            String endTime = HelperMethods.formatTimeTo24H(this.selectEndTime.getText().toString());

            Map<String, Object> eventDetails = new HashMap<>();
            eventDetails.put("userID", this.userID);
            eventDetails.put("eventTitle", "test shared event");
            eventDetails.put("startDate", dateFormatted);
            eventDetails.put("endDate", dateFormatted);
            eventDetails.put("startTime", startTime);
            eventDetails.put("endTime", endTime);
            eventDetails.put("description", "");
            eventDetails.put("participants", users);

            this.fStore.collection("responses").document(this.responseID).delete();

            this.fStore.collection("events")
                    .add(eventDetails)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Intent i = new Intent(ActivitySelectSharedEvent.this, ActivityUpcomingPage.class);
                            startActivity(i);
                        }
                    });
        }
    }

    /**
     * Opens a time picker interface in a dialog for user to select the time.
     * @param b Button object reference
     */

    private void openSelectTimeDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int amPM = c.get(java.util.Calendar.AM_PM);
        int minute = c.get(java.util.Calendar.MINUTE);
        int hour = amPM == 1 ? c.get(java.util.Calendar.HOUR) + 12 : c.get(java.util.Calendar.HOUR);
        TimePickerDialog timePickerDialog = new TimePickerDialog(ActivitySelectSharedEvent.this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String end = "AM";
                if (hourOfDay >= 12) {
                    hourOfDay -= 12;
                    end = "PM";
                }
                b.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + " " + end);
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    /**
     * Returns whether all the times have been filled up properly.
     *
     * @return true if all fields are filled to create event
     */

    private boolean checkFields() {
        if (this.selectStartTime.getText().toString().equals("Select Start Time")) {
            this.startTimelayout.setError("Please choose a start time!");
        } else if (this.selectEndTime.getText().toString().equals("Select End Time")) {
            this.endTimeLayout.setError("Please choose an end time!");
        } else if (!HelperMethods.compareTimes(this.selectStartTime.getText().toString(),
                this.selectEndTime.getText().toString())) {
            this.startTimelayout.setError("Start time must be before end time!");
            this.endTimeLayout.setError("End time must be after start time!");
        } else {
            return true;
        }
        return false;
    }
}