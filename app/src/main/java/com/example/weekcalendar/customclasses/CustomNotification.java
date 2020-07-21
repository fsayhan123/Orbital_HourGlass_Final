package com.example.weekcalendar.customclasses;

import com.example.weekcalendar.helperclasses.HelperMethods;

public class CustomNotification {
    String date;
    String message;
    String ID;
    String dateForView;

    public CustomNotification(String date, String message, String ID) {
        this.date = date;
        this.message = message;
        this.ID = ID;
        this.dateForView = HelperMethods.formatDateForView(this.date);
    }

    public String getDate() {
        return this.date;
    }

    public String getDateForView() {
        return this.dateForView;
    }

    public String getMessage() {
        return this.message;
    }

    public String getID() {
        return this.ID;
    }
}
