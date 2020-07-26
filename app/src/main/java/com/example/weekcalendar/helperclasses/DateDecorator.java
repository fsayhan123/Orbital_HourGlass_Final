package com.example.weekcalendar.helperclasses;

import android.content.Context;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;

import java.util.Date;
import java.util.Set;

public class DateDecorator implements CalendarCellDecorator {

    private Context context;
    private Set<CustomDay> customDayArrayList;

    public DateDecorator(Context context, Set<CustomDay> customDayArrayList) {
        this.customDayArrayList = customDayArrayList;
        this.context = context;
    }

    @Override
    public void decorate(CalendarCellView calendarCellView, Date date) {
        CustomDay customDay = new CustomDay(date);
        if (customDayArrayList.contains(customDay)) {
            calendarCellView.setBackgroundColor(context.getResources().getColor(R.color.green));
        } else {
            calendarCellView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
    }
}