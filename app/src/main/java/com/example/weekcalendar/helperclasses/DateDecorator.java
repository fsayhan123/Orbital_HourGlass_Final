package com.example.weekcalendar.helperclasses;

import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;

import java.util.Date;

public class DateDecorator implements CalendarCellDecorator {
    private int backgroundColor;

    public DateDecorator(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void decorate(CalendarCellView cellView, Date date) {
        cellView.setBackgroundColor(backgroundColor);
    }
}