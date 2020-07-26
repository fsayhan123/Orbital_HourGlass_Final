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

import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.MyDateDialog;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityCreateToDoPage extends AppCompatActivity implements MyDateDialog.MyDateDialogEventListener {
    private static final String TAG = ActivityCreateToDoPage.class.getSimpleName();

    // XML variables
    private Button selectDate;
    private DatePickerDialog datePickerDialog;
    private TextView toDoTitle;
    private Button createToDo;
    private TextInputLayout dateLayout;

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    private CustomToDo toDo;
    private ArrayList<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do_page);

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.c = this.fStore.collection("todo");

        setupXMLItems();

        Intent i = getIntent();
        this.toDo = i.getParcelableExtra("todo");
        if (this.toDo != null) {
            setupUpdateInterface();
        }
    }

    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.create_todo_toolbar);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityToDoListPage.class));
        });

        this.selectDate = findViewById(R.id.select_date);
        this.selectDate.setOnClickListener(v -> openSelectDateDialog(selectDate));

        this.toDoTitle = findViewById(R.id.todo_description);

        this.dateLayout = findViewById(R.id.date_layout);

        this.createToDo = findViewById(R.id.create_todo_button);
        this.createToDo.setOnClickListener(v -> createToDo());
    }

    private void setupUpdateInterface() {
        this.toDoTitle.setText(this.toDo.getTitle());
        this.selectDate.setText(HelperMethods.formatDateForView(this.toDo.getDate()));
        this.createToDo.setOnClickListener(v -> updateToDo());
        this.createToDo.setText("Update To Do");
    }

    private boolean checkFields() {
        if (this.toDoTitle.getText().toString().equals("")) {
            this.toDoTitle.setError("Please insert a description for the to do item!");
        } else if (this.selectDate.getText().toString().equals("Select Date")) {
            this.dateLayout.setError("Please choose a date!");
        } else {
            return true;
        }
        return false;
    }

    private void createToDo() {
         if (checkFields()) {
             Map<String, Object> details = customToDoToMap(false);
             this.c.add(details)
                     .addOnSuccessListener(v -> {
                         Log.d(TAG, "DocumentSnapshot successfully written!");
                         Intent i = new Intent(this, ActivityToDoListPage.class);
                         startActivity(i);
                     })
                     .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
    }

    public void updateToDo() {
        if (checkFields()) {
            Map<String, Object> details = customToDoToMap(this.toDo.getCompleted());
            this.fStore.collection("todo")
                    .document(this.toDo.getID())
                    .update(details)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            Intent intent = new Intent(ActivityCreateToDoPage.this, ActivityToDoListPage.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        }
    }

    public Map<String, Object> customToDoToMap(boolean completed) {
        String date = this.selectDate.getText().toString();
        String title = this.toDoTitle.getText().toString();
        Map<String, Object> details = new HashMap<>();
        details.put("userID", this.userID);
        details.put("date", HelperMethods.formatDateWithDash(date));
        details.put("title", title);
        details.put("completed", completed);
        details.put("timestamp", System.currentTimeMillis());
        if (this.toDo != null) {
            details.put("eventID", this.toDo.getEventID());
        }
        return details;
    }

    @Override
    public void applyDateText(CustomDay d, Button b) {
        b.setText(d.getFullDateForView());
    }

    private void openSelectDateDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);
        int month = c.get(java.util.Calendar.MONTH);
        int year = c.get(java.util.Calendar.YEAR);
        this.datePickerDialog = new DatePickerDialog(ActivityCreateToDoPage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                b.setText(String.format("%02d", dayOfMonth) + " " + HelperMethods.numToStringMonth[month + 1].substring(0, 3) + " " + year);
            }
        }, year, month, day);
        this.datePickerDialog.show();
    }
}