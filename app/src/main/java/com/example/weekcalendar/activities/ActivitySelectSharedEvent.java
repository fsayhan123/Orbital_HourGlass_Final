package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.adapters.PendingSharedEventsRecyclerViewAdapter;
import com.example.weekcalendar.adapters.ResponseRecyclerViewAdapter;
import com.example.weekcalendar.customclasses.CustomPendingShared;
import com.example.weekcalendar.customclasses.CustomResponse;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mortbay.jetty.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivitySelectSharedEvent extends AppCompatActivity {

    // Firebase variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    //Local Variables
    private String responseID;
    private TextView title;
    private HashMap<String, ArrayList<String>> responses;
    private ArrayList<String> selectedDates = new ArrayList<>();
    private TimePickerDialog timePickerDialog;
    private Button selectStartTime;
    private Button selectEndTime;

    //Recycler View Variables
    private ArrayList<CustomResponse> CustomResponseArrayList;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ResponseRecyclerViewAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shared_event);

        System.out.println("Hello 1");
        this.title = findViewById(R.id.select_shared_event_title);

        Intent intent = getIntent();
        this.responseID = intent.getStringExtra("responseID");

        this.selectStartTime = findViewById(R.id.select_start_time);
        this.selectStartTime.setOnClickListener(v -> openSelectTimeDialog(this.selectStartTime));

        this.selectEndTime = findViewById(R.id.select_end_time);
        this.selectEndTime.setOnClickListener(v -> openSelectTimeDialog(this.selectEndTime));

        //Setup firebase Variables
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        //Recycler View Setup
        this.mRecyclerView = findViewById(R.id.selecting_shared_event_date);
        this.mRecyclerView.setHasFixedSize(true);
        this.layoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(layoutManager);

        System.out.println("Hello 2");
        this.CustomResponseArrayList = new ArrayList<>();
        this.fStore.collection("responses")
                .document(this.responseID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ActivitySelectSharedEvent.this.title.setText(documentSnapshot.get("title").toString());

                        //Get the responses, order by array length, then put into recycler view
                        HashMap<String, ArrayList<String>> hashMap = (HashMap<String, ArrayList<String>>) documentSnapshot.get("responses");
                        for (Map.Entry<String, ArrayList<String>> entry : hashMap.entrySet()) {
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

    public void confirmEvent(View v) {
        if (this.selectedDates.size() != 1)  {
            Toast.makeText(this, "Please select only one date", Toast.LENGTH_SHORT).show();
        }
        else {
            String date = this.selectedDates.get(0);
            String dateFormatted = HelperMethods.formatDateWithDash(date);
            ArrayList<String> users = this.responses.get(dateFormatted);
            users.add(this.userID);

            String startTime = HelperMethods.formatTimeTo24H(this.selectStartTime.getText().toString());
            String endTime = HelperMethods.formatTimeTo24H(this.selectEndTime.getText().toString());

            Map<String, Object> eventDetails = new HashMap<>();
            eventDetails.put("userID", this.userID);
            eventDetails.put("eventTitle", "test shared event");
            eventDetails.put("startDate", dateFormatted);
            eventDetails.put("endDate", dateFormatted);
            eventDetails.put("startTime", startTime);
            eventDetails.put("endTime", endTime);
            eventDetails.put("description", "Description");
            eventDetails.put("participants", users);
            this.fStore.collection("events").add(eventDetails);
            Intent i = new Intent(this, ActivityUpcomingPage.class);
            startActivity(i);

        };
    }

    private void openSelectTimeDialog(Button b) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int amPM = c.get(java.util.Calendar.AM_PM);
        int minute = c.get(java.util.Calendar.MINUTE);
        int hour = amPM == 1 ? c.get(java.util.Calendar.HOUR) + 12 : c.get(java.util.Calendar.HOUR);
        this.timePickerDialog = new TimePickerDialog(ActivitySelectSharedEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
        this.timePickerDialog.show();
    }
}