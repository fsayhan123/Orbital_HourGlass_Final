package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityCreateEventPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener, MyTimeDialog.MyTimeDialogListener {
    private static final String TAG = ActivityCreateEventPage.class.getSimpleName();

    private Button selectStartDate;
    private Button selectEndDate;
    private Button selectStartTime;
    private Button selectEndTime;
    private Button createEvent;
    private EditText todo1;
    private CustomDay selectedCustomDay;
    private DatabaseHelper myDB;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_page);

        myDB = new DatabaseHelper(this);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        c = fStore.collection("events");

        this.setTitle("Create Event");

        selectStartDate = findViewById(R.id.select_start_date);
        selectStartDate.setOnClickListener(v -> openSelectDateDialog(v, selectStartDate));

        selectEndDate = findViewById(R.id.select_end_date);
        selectEndDate.setOnClickListener(v -> openSelectDateDialog(v, selectEndDate));

        selectStartTime = findViewById(R.id.select_start_time);
        selectStartTime.setOnClickListener(v -> openSelectTimeDialog(v, selectStartTime));

        selectEndTime = findViewById(R.id.select_end_time);
        selectEndTime.setOnClickListener(v -> openSelectTimeDialog(v, selectEndTime));

        todo1 = findViewById(R.id.todo_item);

        createEvent = findViewById(R.id.create_event_button);
        createEvent.setOnClickListener(v -> createEvent());

//        myDB = new DatabaseHelper(this);

        Toolbar tb = findViewById(R.id.event_creation_toolbar);
        setSupportActionBar(tb);
        // sets up back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityUpcomingPage.class));
        });
    }

    private void createEvent() {
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
            String toDo = todo1.getText().toString();
            String startDate = selectStartDate.getText().toString();
            String endDate = selectEndDate.getText().toString();
            String startTime = selectStartTime.getText().toString();
            String endTime = selectEndTime.getText().toString();

            Map<String, Object> details = new HashMap<>();
            details.put("userID", userID);
            details.put("toDo", toDo);
            details.put("startDate", myDB.formatDate(startDate));
            details.put("endDate", myDB.formatDate(endDate));
            details.put("startTime", startTime);
            details.put("endTime", endTime);
            c.add(details)
                    .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

//            boolean result = myDB.addEvent(eventTitle, startDate, endDate, startTime, endTime, toDo);
//            if (result == true) {
//                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
//            }
            Toast.makeText(this, "Event created with " + toDo, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);
        }
    }

    private void openSelectDateDialog(View v, Button b) {
        Toast.makeText(this, "opened date dialog", Toast.LENGTH_SHORT).show();
        MyDateDialog myDateDialog = new MyDateDialog(b);
        myDateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    private void openSelectTimeDialog(View v, Button b) {
        Toast.makeText(this, "opened time dialog", Toast.LENGTH_SHORT).show();
        MyTimeDialog myTimeDialog = new MyTimeDialog(b);
        myTimeDialog.show(getSupportFragmentManager(), "time dialog");
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
        selectedCustomDay = d;
    }

    @Override
    public void applyTimeText(CustomDay d, Button b) {
        b.setText(d.getTime());
        selectedCustomDay = d;
    }
}
