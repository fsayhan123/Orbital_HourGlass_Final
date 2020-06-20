package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityCreateEventPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener, MyTimeDialog.MyTimeDialogListener {
    private static final String TAG = ActivityCreateEventPage.class.getSimpleName();

    // XML variables
    private EditText title;
    private TextInputLayout selectStartDateLayout;
    private Button selectStartDate;
    private TextInputLayout selectEndDateLayout;
    private Button selectEndDate;
    private TextInputLayout selectStartTimeLayout;
    private Button selectStartTime;
    private TextInputLayout selectEndTimeLayout;
    private Button selectEndTime;
    private Button createEvent;
    private EditText todo1;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference cEvents;
    private CollectionReference cToDo;

    private CustomEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.cEvents = this.fStore.collection("events");
        this.cToDo = this.fStore.collection("todo");

        // Links to XML
        this.title = findViewById(R.id.insert_event_name);

        this.selectStartDateLayout = findViewById(R.id.selectStartDateLayout);
        this.selectEndDateLayout = findViewById(R.id.selectEndDateLayout);
//        this.selectStartTimeLayout = findViewById(R.id.selectStartDateLayout);
//        this.selectEndTimeLayout = findViewById(R.id.selectStartDateLayout);

        this.selectStartDate = findViewById(R.id.select_start_date);
        this.selectStartDate.setOnClickListener(v -> openSelectDateDialog(this.selectStartDate));

        this.selectEndDate = findViewById(R.id.select_end_date);
        this.selectEndDate.setOnClickListener(v -> openSelectDateDialog(this.selectEndDate));

        this.selectStartTime = findViewById(R.id.select_start_time);
        this.selectStartTime.setOnClickListener(v -> openSelectTimeDialog(this.selectStartTime));

        this.selectEndTime = findViewById(R.id.select_end_time);
        this.selectEndTime.setOnClickListener(v -> openSelectTimeDialog(this.selectEndTime));

        this.todo1 = findViewById(R.id.todo_item);

        this.createEvent = findViewById(R.id.create_event_button);
        this.createEvent.setOnClickListener(v -> createEvent());

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.create_event_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityUpcomingPage.class));
        });

        Intent i = getIntent();
        this.event = i.getParcelableExtra("event to edit");
        if (event != null) {
            this.title.setText(event.getTitle());
            this.selectStartDate.setText(HelperMethods.formatDateForView(event.getStartDate()));
            this.selectEndDate.setText(HelperMethods.formatDateForView(event.getEndDate()));
            this.selectStartTime.setText(HelperMethods.formatTimeTo12H(event.getStartTime()));
            this.selectEndTime.setText(HelperMethods.formatTimeTo12H(event.getEndTime()));
            this.createEvent.setText("Update Event");
            this.createEvent.setOnClickListener(v -> updateEvent());
        }
    }

    // need to have a check for start day >= end day, start time >= end time
    private boolean checkFields() {
        String eventTitle = this.title.getText().toString();
        if (eventTitle.equals("")) {
            this.title.setError("Please insert event title!");
        } else if (this.selectStartDate.getText().toString().equals("Select Start Date")) {
            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectStartDate.setError("Please choose a start date!");
        } else if (this.selectEndDate.getText().toString().equals("Select End Date")) {
            this.selectEndDateLayout.setError("Please choose an end date!");
            this.selectEndDate.setError("Please choose an end date!");
        } else if (this.selectStartTime.getText().toString().equals("Select Start Time")) {
//            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectStartTime.setError("Please choose a start time!");
        } else if (this.selectEndTime.getText().toString().equals("Select End Time")) {
//            this.selectStartDateLayout.setError("Please choose a start date!");
            this.selectEndTime.setError("Please choose an end time!");
        } else {
            return true;
        }
        return false;
    }

    private Map<String, Object> getEventDetails() {
        String eventTitle = ((EditText) findViewById(R.id.insert_event_name)).getText().toString();
        String startDate = HelperMethods.formatDateForFirebase(this.selectStartDate.getText().toString());
        String endDate = HelperMethods.formatDateForFirebase(this.selectEndDate.getText().toString());
        String startTime = HelperMethods.formatTimeTo24H(this.selectStartTime.getText().toString());
        String endTime = HelperMethods.formatTimeTo24H(this.selectEndTime.getText().toString());

        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("userID", this.userID);
        eventDetails.put("eventTitle", eventTitle);
        eventDetails.put("startDate", startDate);
        eventDetails.put("endDate", endDate);
        eventDetails.put("startTime", startTime);
        eventDetails.put("endTime", endTime);

        return eventDetails;
    }

    private Map<String, Object> getToDoDetails() {
        String toDo = this.todo1.getText().toString();

        if (!toDo.equals("")) {
            Map<String, Object> toDoDetails = new HashMap<>();
            String startDate = HelperMethods.formatDateForFirebase(this.selectStartDate.getText().toString());
            toDoDetails.put("userID", this.userID);
            toDoDetails.put("date", startDate);
            toDoDetails.put("title", toDo);
            return toDoDetails;
        } else {
            return null;
        }
    }

    private void updateEvent() {
        if (checkFields()) {
            DocumentReference thisEventDoc = this.cEvents.document(this.event.getId());
//            DocumentReference thisToDoDoc = this.cToDo.document(this.event.getId());

            Map<String, Object> eventDetails = getEventDetails();
//            Map<String, Object> toDoDetails = getToDoDetails();

            thisEventDoc.update(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        // need to check if event originally had to do items
                        // if true, then we need to update this based on the existing to do's document ID
                        // if false, we need to create new document
//                        if (toDoDetails != null) {
//                            DocumentReference toDoDocRef = cToDo.document();
//                            toDoDetails.put("eventID", this.event.getId());
//                            toDoDetails.put("todoID", toDoDocRef.getId());
//                            toDoDocRef.set(toDoDetails)
//                                    .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
//                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
//                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);
        }
    }

    // need to implement multi day here
    private void createEvent() {
        if (checkFields()) {
            Map<String, Object> eventDetails = getEventDetails();
            Map<String, Object> toDoDetails = getToDoDetails();

            cEvents.add(eventDetails)
                    .addOnSuccessListener(docRef -> {
                        if (toDoDetails != null) {
                            DocumentReference toDoDocRef = cToDo.document();
                            toDoDetails.put("eventID", docRef.getId());
                            toDoDetails.put("todoID", toDoDocRef.getId());
                            toDoDocRef.set(toDoDetails)
                                    .addOnSuccessListener(docRef2 -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);
        }
    }

    private void openSelectDateDialog(Button b) {
        MyDateDialog myDateDialog = new MyDateDialog(b);
        myDateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    private void openSelectTimeDialog(Button b) {
        MyTimeDialog myTimeDialog = new MyTimeDialog(b);
        myTimeDialog.show(getSupportFragmentManager(), "time dialog");
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
    }

    @Override
    public void applyTimeText(CustomDay d, Button b) {
        b.setText(d.getTime());
    }
}