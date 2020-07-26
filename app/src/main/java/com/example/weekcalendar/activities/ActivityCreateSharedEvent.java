package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.helperclasses.DialogCreationEvent;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.graphics.Color.*;

public class ActivityCreateSharedEvent extends AppCompatActivity {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityCreateSharedEvent.class.getSimpleName();

    /**
     * UI variables
     */
    private CompactCalendarView calendarView;
    private ArrayList<CustomDay> selectedDates = new ArrayList<>();
    private ArrayList<String> emails = new ArrayList<>();
    private TextView monthYear;
    private EditText title;

    /**
     * Firebase information
     */
    public FirebaseAuth fAuth;
    public FirebaseFirestore fStore;
    public CollectionReference c;
    public String userID;

    /**
     * Used to convert Strings to Dates and vice versa
     */
    private static Date today = new Date();
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat MONTH_AND_YEAR = new SimpleDateFormat("MMMM yyyy");

    /**
     * Sets up ActivityCreateSharedEvent when it is opened.
     * First, sets up Firebase or Google account.
     * Then, sets up layout items by calling setupXMLItems();
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shared_event);

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.c = this.fStore.collection("responses");

        setupXMLItems();
    }

    /**
     * Sets up layout for ActivityCreateSharedEvent.
     */
    private void setupXMLItems() {
        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.create_shared_event));
        navDrawer.setupNavDrawerPane();

        this.userID = Objects.requireNonNull(this.fAuth.getCurrentUser()).getUid();

        this.calendarView = findViewById(R.id.compact_calendar_view_creation);
        this.calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        this.calendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        this.title = findViewById(R.id.create_shared_event_title);

        Button sendInvites = findViewById(R.id.button);
        sendInvites.setOnClickListener(v -> createResponseForm());

        this.monthYear = findViewById(R.id.month_year_shared);
        this.monthYear.setText(MONTH_AND_YEAR.format(today));

        this.calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                CustomDay date = new CustomDay(dateClicked);
                if (ActivityCreateSharedEvent.this.selectedDates.contains(date)) {
                    ActivityCreateSharedEvent.this.selectedDates.remove(date);
                    ActivityCreateSharedEvent.this.calendarView.removeEvents(dateClicked);
                } else {
                    ActivityCreateSharedEvent.this.selectedDates.add(date);
                    Event e1 = new Event(GREEN, dateClicked.getTime());
                    ActivityCreateSharedEvent.this.calendarView.addEvent(e1);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                String date = MONTH_AND_YEAR.format(firstDayOfNewMonth);
                monthYear.setText(date);
            }});
    }

    /**
     * Sends an in-app notification to invited user with email userEmail parameter
     * @param docRefID the Firebase response document ID, where invited users' responses will be collected
     *                 once they response to the invite
     * @param userEmail email of invited user
     */
    public void sendNotification(String docRefID, String userEmail) {
        this.fStore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String respondentID = "";
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                respondentID = document.getId();
                            }
                            emails.add(respondentID);
                            Map<String, Object> data = new HashMap<>();
                            data.put("dateOfNotification", HelperMethods.getCurrDate());
                            Collections.sort(ActivityCreateSharedEvent.this.selectedDates);
                            String startDate = ActivityCreateSharedEvent.this.selectedDates.get(0).toString();
                            String endDate = ActivityCreateSharedEvent.this.selectedDates.get(ActivityCreateSharedEvent.this.selectedDates.size() - 1).toString();
                            String title = ActivityCreateSharedEvent.this.title.getText().toString();

                            data.put("message", String.format("Shared Event Title: %s, Start Date: %s, End Date: %s ", title, startDate, endDate));
                            data.put("hostID", userID);
                            data.put("respondentID", respondentID);
                            data.put("responseFormID", docRefID);
                            data.put("hasResponded", false);
                            data.put("response", null);
                            ActivityCreateSharedEvent.this.fStore.collection("Notifications").add(data);
                        }
                    }
                });
    }

    /**
     * Launches a dialog which allows user to key in emails of other users to share this event with.
     */
    public void createResponseForm() {
        Map<String, Object> sharedEventDetails = getSharedEventDetails();
        sharedEventDetails.put("hostID", this.userID);
        sharedEventDetails.put("title", this.title.getText().toString());
        DialogCreationEvent dialog = new DialogCreationEvent(sharedEventDetails,ActivityCreateSharedEvent.this);
        dialog.show(getSupportFragmentManager(), "Example");

    }

    /**
     * Gets details of this shared event as input by user.
     * @return Map of details about the shared event
     */
    private Map<String, Object> getSharedEventDetails() {
        Map<String, Object> details = new HashMap<>();

        Map<String, List<String>> dates = new HashMap<>();
        for (CustomDay d : this.selectedDates) {
            String date = d.getyyyy() + "-" + HelperMethods.convertMonth(d.getMMM()) + "-" + d.getdd();
            dates.put(date, new ArrayList<>());
        }
        details.put("responses", dates);

        return details;
    }
}