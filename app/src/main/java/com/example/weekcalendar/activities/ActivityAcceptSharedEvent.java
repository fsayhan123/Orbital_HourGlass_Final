package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityAcceptSharedEvent extends AppCompatActivity {
    private static final String TAG = ActivityAcceptSharedEvent.class.getSimpleName();

    private SetupNavDrawer navDrawer;
    private Button submitButton;

    private CalendarPickerView datePicker;
    private Set<CustomDay> selectedDates = new HashSet<>();
    private Map<String, List<String>> data;

    //Firebase Variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;
    private String docID;

    private Date firstDate = null;
    private Date lastDate = null;

    // To transform String to Date
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_shared_event);

//        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.create_shared_event));
//        this.navDrawer.setupNavDrawerPane();

        this.fAuth = FirebaseAuth.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();
        this.fStore = FirebaseFirestore.getInstance();
        this.c = this.fStore.collection("responses");

        Date today = new Date();
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.WEEK_OF_MONTH, 1);

        Intent i = getIntent();
        this.docID = i.getStringExtra("response form ID");

        fetchDataFromFirebase(this.docID);

        this.submitButton = findViewById(R.id.submit_button);
        this.submitButton.setOnClickListener(v -> submitChoices());

        this.datePicker = findViewById(R.id.picker_calendar);
        this.datePicker.init(today, nextDay.getTime()).withSelectedDate(today);

        this.datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                CustomDay selectedDate = new CustomDay(date);
                if (ActivityAcceptSharedEvent.this.selectedDates.contains(selectedDate)) {
                    ActivityAcceptSharedEvent.this.selectedDates.remove(selectedDate);
                } else {
                    ActivityAcceptSharedEvent.this.selectedDates.add(selectedDate);
                }
            }

            @Override
            public void onDateUnselected(Date date) { return; }
        });
    }

    private void fetchDataFromFirebase(String responseFormID) {
        this.c.document(responseFormID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        data = (Map<String, List<String>>) doc.get("responses");
                        TreeMap<String, List<String>> sortedData = new TreeMap<>(data);
                        String first = sortedData.firstKey();
                        String last = sortedData.lastKey();
                        try {
                            firstDate = dateFormatter.parse(first);
                            lastDate = dateFormatter.parse(last);
                            Calendar c = Calendar.getInstance();
                            c.setTime(lastDate);
                            c.add(Calendar.DAY_OF_MONTH, 1);
                            lastDate = c.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        datePicker.init(firstDate, lastDate).withSelectedDate(firstDate);
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, e.getLocalizedMessage()));
    }

    public void submitChoices() {
        Map<String, Object> responses = getUserResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("responses", responses);
        this.c.document(this.docID)
                .update(data)
                .addOnSuccessListener(docRef -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    public Map<String, Object> getUserResponse() {
        Map<String, Object> responses = new HashMap<>();
        for (CustomDay d : this.selectedDates) {
            responses.put(d.getDateForFirebase(), this.userID);
        }
        return responses;
    }
}

