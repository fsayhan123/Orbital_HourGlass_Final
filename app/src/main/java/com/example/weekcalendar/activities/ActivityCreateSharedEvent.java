package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.graphics.Color.*;


public class ActivityCreateSharedEvent extends AppCompatActivity {
    private static final String TAG = ActivityCreateSharedEvent.class.getSimpleName();

    private SetupNavDrawer navDrawer;
    private CompactCalendarView calendarView;
    private ArrayList<CustomDay> selectedDates = new ArrayList<>();
    private ArrayList<String> emails = new ArrayList<>();
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private Button sendInvites;
    private TextView monthYear;
    private EditText title;

    //Firebase Variables
    public FirebaseAuth fAuth;
    public FirebaseFirestore fStore;
    public CollectionReference c;
    public String userID;

    //Date variables
    private static Date today = new Date();
    private static final DateFormat MONTH_AND_YEAR = new SimpleDateFormat("MMMM yyyy");
    private static final DateFormat FULL_DATE = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shared_event);

        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.create_shared_event));
        this.navDrawer.setupNavDrawerPane();

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.c = this.fStore.collection("responses");

        this.userID = this.fAuth.getCurrentUser().getUid();

        this.calendarView = findViewById(R.id.compact_calendar_view_creation);
        this.calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        this.calendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        this.title = findViewById(R.id.create_shared_event_title);

        this.sendInvites = findViewById(R.id.button);
        this.sendInvites.setOnClickListener(v -> sendInvite());

        this.monthYear = findViewById(R.id.month_year_shared);
        this.monthYear.setText(MONTH_AND_YEAR.format(today));

        this.calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                CustomDay date = new CustomDay(dateClicked);
                if (ActivityCreateSharedEvent.this.selectedDates.contains(date)) {
                    ActivityCreateSharedEvent.this.selectedDates.remove(date);
                    ActivityCreateSharedEvent.this.calendarView.removeEvents(dateClicked);
//                    Toast.makeText(ActivityCreateSharedEvent.this, "removed " + date.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCreateSharedEvent.this.selectedDates.add(date);
                    Event e1 = new Event(GREEN, dateClicked.getTime());
                    ActivityCreateSharedEvent.this.calendarView.addEvent(e1);
//                    Toast.makeText(ActivityCreateSharedEvent.this, "Added " + date.toString(), Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                String date = MONTH_AND_YEAR.format(firstDayOfNewMonth);
                String fullDate = FULL_DATE.format(firstDayOfNewMonth);

                monthYear.setText(date);
            }});

    }

    // change to String[]
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

                            // TO CHANGE
                            data.put("message", "Hello, this is an invitation to a shared event"); // change with the necessary details
                            // TO CHANGE END

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

    public void sendInvite() {
        Log.d(TAG, "creating!!!!!!!!");
        createResponseForm();
    }

    public void createResponseForm() {
        Map<String, Object> sharedEventDetails = getSharedEventDetails();
        sharedEventDetails.put("hostID", this.userID);
        sharedEventDetails.put("title", this.title.getText().toString());
        DialogCreationEvent dialog = new DialogCreationEvent(sharedEventDetails,ActivityCreateSharedEvent.this);
        dialog.show(getSupportFragmentManager(), "Example");

    }

    private Map<String, Object> getSharedEventDetails() {
        Map<String, Object> details = new HashMap<>();

//        String[] arr = this.emails.toArray(new String[this.emails.size()]);
//        details.put("emails", arr);

        Map<String, List<String>> dates = new HashMap<>();
        for (CustomDay d : this.selectedDates) {
            String date = d.getyyyy() + "-" + HelperMethods.convertMonth(d.getMMM()) + "-" + d.getdd();
            dates.put(date, new ArrayList<>()); // change limit to number of users
            Log.d(TAG, date + " HIIIIIIIII");
        }
        details.put("responses", dates);

        return details;
    }
}