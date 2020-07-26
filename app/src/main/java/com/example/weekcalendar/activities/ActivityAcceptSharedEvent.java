package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.helperclasses.DateDecorator;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class ActivityAcceptSharedEvent extends AppCompatActivity {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityAcceptSharedEvent.class.getSimpleName();

    /**
     * UI variables
     */
    private TextView title;
    private CalendarPickerView datePicker;
    private Set<CustomDay> selectedDates = new HashSet<>();
    private Map<String, List<String>> data;

    /**
     * Firebase information
     */
    private FirebaseFirestore fStore;
    private String userID;
    private CollectionReference c;

    /**
     * ID of notification and shared event form to display
     */
    private String docID;
    private String notifID;

    /**
     * Set up the range of date to display on the calendar picker
     */
    private Date firstDate = null;
    private Date lastDate = null;

    /**
     * To convert Strings to Dates and vice versa
     */
    @SuppressLint("SimpleDateFormat")
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Sets up ActivityAcceptSharedEvent when it is opened.
     * First, sets up Firebase information.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling fetchDataFromFirebase() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_shared_event);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.userID = fAuth.getCurrentUser().getUid();
        this.fStore = FirebaseFirestore.getInstance();
        this.c = this.fStore.collection("responses");

        setupXMLItems();

        Intent i = getIntent();
        this.docID = i.getStringExtra("response form ID");
        this.notifID = i.getStringExtra("notification ID");

        fetchDataFromFirebase(this.docID);
    }

    /**
     * Sets up layout for ActivityAcceptSharedEvent.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.accept_shared_event);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, ActivityNotificationsPage.class));
        });

        this.title = findViewById(R.id.accept_shared_event_title);

        Date today = new Date();
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.WEEK_OF_MONTH, 1);

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> submitChoices());

        this.datePicker = findViewById(R.id.picker_calendar);
        this.datePicker.init(today, nextDay.getTime()).withSelectedDate(today);
        this.datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                CustomDay selectedDate = new CustomDay(date);
                if (ActivityAcceptSharedEvent.this.selectedDates.contains(selectedDate)) {
                    ActivityAcceptSharedEvent.this.selectedDates.remove(selectedDate);
                    CalendarCellDecorator decorator= new DateDecorator(ActivityAcceptSharedEvent.this, ActivityAcceptSharedEvent.this.selectedDates);
                    List<CalendarCellDecorator> decoratorList = new ArrayList<>();
                    decoratorList.add(decorator);

                    datePicker.setDecorators(decoratorList);
                } else {
                    ActivityAcceptSharedEvent.this.selectedDates.add(selectedDate);
                    CalendarCellDecorator decorator= new DateDecorator(ActivityAcceptSharedEvent.this, ActivityAcceptSharedEvent.this.selectedDates);
                    List<CalendarCellDecorator> decoratorList = new ArrayList<>();
                    decoratorList.add(decorator);

                    datePicker.setDecorators(decoratorList);
                }
            }

            @Override
            public void onDateUnselected(Date date) { return; }
        });
    }

    /**
     * Queries data about the this shared event from Firebase response collection, to get the range
     * of applicable dates to display in the calendar picker.
     * @param responseFormID Firebase document on the responses about this shared event
     */
    private void fetchDataFromFirebase(String responseFormID) {
        this.c.document(responseFormID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        String title = Objects.requireNonNull(doc.get("title")).toString();
                        ActivityAcceptSharedEvent.this.title.setText(title);
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
                .addOnFailureListener(e -> Log.d(TAG, Objects.requireNonNull(e.getLocalizedMessage())));
    }

    /**
     * Submits choices of dates selected by adding the user's ID to the list of users who indicated availability
     * for each selected date.
     */
    public void submitChoices() {
        Map<String, Object> responses = getUserResponse();

        for (CustomDay d : this.selectedDates) {
            this.c.document(this.docID)
                    .update(String.format("responses.%s", d.getDateForFirebase()), FieldValue.arrayUnion(userID))
                    .addOnSuccessListener(docRef -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("hasResponded", true);
        data.put("response", new ArrayList<>(responses.keySet()));
        this.fStore.collection("Notifications")
                .document(this.notifID)
                .update(data)
                .addOnSuccessListener(v -> Log.d(TAG, "Normal event invite accepted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error accepting normal event invite.", e));

        Intent i = new Intent(this, ActivityIndividualNotification.class);
        i.putExtra("notificationID", this.notifID);
        startActivity(i);
    }

    /**
     * Gets the dates selected by user.
     * @return Map where the keys are selected dates and the values as the user's ID
     */
    public Map<String, Object> getUserResponse() {
        Map<String, Object> responses = new HashMap<>();
        for (CustomDay d : this.selectedDates) {
            responses.put(d.getDateForFirebase(), this.userID);
        }
        return responses;
    }
}