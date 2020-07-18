package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ActivityCreateSharedEvent extends AppCompatActivity {

    private SetupNavDrawer navDrawer;
    private CalendarView calendarView;
    private ArrayList<CustomDay> selectedDates = new ArrayList<>();
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    //Firebase Variables
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shared_event);

        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.create_shared_event));
        this.navDrawer.setupNavDrawerPane();

        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        this.calendarView = findViewById(R.id.date_select_calendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Date jDate = new Date();
                String strMonth = String.valueOf(month + 1);
                String strDay = String.valueOf(dayOfMonth);
                if (strDay.length() < 2) {
                    strDay = "0" + strDay;
                }
                if (strMonth.length() < 2) {
                    strMonth = "0" + strMonth;
                }

                String strDate = year + "-" + strMonth + "-" + strDay;
                System.out.println(strDate);
                try {
                    jDate = dateFormatter.parse(strDate);
                } catch (ParseException e){
                    System.out.println("dead");
                }
                CustomDay date = new CustomDay(jDate);
                if (ActivityCreateSharedEvent.this.selectedDates.contains(date)) {
                    ActivityCreateSharedEvent.this.selectedDates.remove(date);
                    Toast.makeText(ActivityCreateSharedEvent.this, "removed " + date.toString(), Toast.LENGTH_LONG).show();
                } else {
                    ActivityCreateSharedEvent.this.selectedDates.add(date);
                    Toast.makeText(ActivityCreateSharedEvent.this, "Added " + date.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void sendInvite(View v) {
        System.out.println(selectedDates);
    }
}