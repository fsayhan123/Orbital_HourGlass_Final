package com.example.weekcalendar.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ActivityAcceptSharedEvent extends AppCompatActivity {
    private CalendarPickerView datePicker;
    private ArrayList<CustomDay> selectedDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_shared_event);

        Date today = new Date();
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.WEEK_OF_MONTH, 1);


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
            public void onDateUnselected(Date date) {
                CustomDay selectedDate = new CustomDay(date);
                ActivityAcceptSharedEvent.this.selectedDates.remove(selectedDate);
            }
        });
    }

    public void testing(View view) {
        System.out.println(this.selectedDates);
    }
}

