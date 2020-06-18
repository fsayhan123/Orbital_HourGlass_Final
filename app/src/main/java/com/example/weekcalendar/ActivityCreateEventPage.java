package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityCreateEventPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener, MyTimeDialog.MyTimeDialogListener {
    private static final String TAG = ActivityCreateEventPage.class.getSimpleName();

    // XML variables
    private Button selectStartDate;
    private Button selectEndDate;
    private Button selectStartTime;
    private Button selectEndTime;
    private Button createEvent;
    private EditText todo1;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("events");

        // Links to XML
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
    }

    private boolean checkFields() {
        String eventTitle = ((EditText) findViewById(R.id.insert_event_name)).getText().toString();
        if (eventTitle.equals("")) {
            Toast.makeText(this, "Please insert event title!", Toast.LENGTH_SHORT).show();
        } else if (selectStartDate.getText().toString().equals("Select Start Date")) {
            Toast.makeText(this, "Please choose a start date!", Toast.LENGTH_SHORT).show();
        } else if (selectEndDate.getText().toString().equals("Select End Date")) {
            Toast.makeText(this, "Please choose an end date!", Toast.LENGTH_SHORT).show();
        } else if (selectStartTime.getText().toString().equals("Select Start Time")) {
            Toast.makeText(this, "Please choose a start time!", Toast.LENGTH_SHORT).show();
        } else if (selectEndTime.getText().toString().equals("Select End Time")) {
            Toast.makeText(this, "Please choose an end time!", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

    private void createEvent() {
        if (checkFields()) {
            String eventTitle = ((EditText) findViewById(R.id.insert_event_name)).getText().toString();
            String toDo = todo1.getText().toString();
            String startDate = selectStartDate.getText().toString();
            String endDate = selectEndDate.getText().toString();
            String startTime = selectStartTime.getText().toString();
            String endTime = selectEndTime.getText().toString();

            Map<String, Object> details = new HashMap<>();
            details.put("userID", userID);
            details.put("eventTitle", eventTitle);
            details.put("toDo", toDo);
            details.put("startDate", HelperMethods.formatDate(startDate));
            details.put("endDate", HelperMethods.formatDate(endDate));
            details.put("startTime", startTime);
            details.put("endTime", endTime);

            c.add(details)
                    .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully written!"))
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
