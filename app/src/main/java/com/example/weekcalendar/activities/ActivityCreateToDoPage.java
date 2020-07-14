package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityCreateToDoPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener {
    private static final String TAG = ActivityCreateToDoPage.class.getSimpleName();

    // XML variables
    private Button selectDate;
    private DatePickerDialog datePickerDialog;
    private TextView toDoTitle;
    private Button createToDo;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    private CustomToDo toDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do_page);

        // Setup link to Firebase
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("todo");

        // Links to XML
        this.selectDate = findViewById(R.id.select_date);
        this.selectDate.setOnClickListener(v -> openSelectDateDialog(selectDate));

        this.toDoTitle = findViewById(R.id.todo_description);

        this.createToDo = findViewById(R.id.create_todo_button);
        this.createToDo.setOnClickListener(v -> createToDo());

        // Setup toolbar with working back button
        Toolbar tb = findViewById(R.id.create_todo_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityToDoListPage.class));
        });

        Intent i = getIntent();
        this.toDo = i.getParcelableExtra("todo");
        if (toDo != null) {
            this.toDoTitle.setText(this.toDo.getDetails());
            this.selectDate.setText(HelperMethods.formatDateForView(this.toDo.getDate()));
            this.createToDo.setOnClickListener(v -> updateToDo());
            this.createToDo.setText("Update To Do");
        }
    }

    private boolean checkFields() {
        if (this.selectDate.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
        } else if (this.toDoTitle.getText().toString().equals("")) {
            Toast.makeText(this, "Please insert to do title", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

    private void createToDo() {
         if (checkFields()) {
            String date = selectDate.getText().toString();
            String title = toDoTitle.getText().toString();

             Map<String, Object> details = new HashMap<>();
             details.put("userID", userID);
             details.put("date", HelperMethods.formatDateWithDash(date));
             details.put("title", title);

             c.add(details)
                     .addOnSuccessListener(v -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                     .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
             Intent i = new Intent(this, ActivityToDoListPage.class);
             startActivity(i);
        }
    }

    private void openSelectDateDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);
        int month = c.get(java.util.Calendar.MONTH);
        int year = c.get(java.util.Calendar.YEAR);
        this.datePickerDialog = new DatePickerDialog(ActivityCreateToDoPage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                b.setText(dayOfMonth + " " + HelperMethods.numToStringMonth[month + 1].substring(0, 3) + " " + year);
            }
        }, year, month, day);
        this.datePickerDialog.show();
    }

    public void updateToDo() {
        if (checkFields()) {
            String date = this.selectDate.getText().toString();
            String title = this.toDoTitle.getText().toString();

            Map<String, Object> details = new HashMap<>();
            details.put("userID", userID);
            details.put("date", HelperMethods.formatDateWithDash(date));
            details.put("title", title);

            fStore.collection("todo").document(this.toDo.getID()).set(details);
            Intent intent = new Intent(this, ActivityToDoListPage.class);
            startActivity(intent);
        }
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getDate());
    }
}