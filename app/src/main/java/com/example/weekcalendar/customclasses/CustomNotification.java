package com.example.weekcalendar.customclasses;

import com.example.weekcalendar.helperclasses.HelperMethods;

public class CustomNotification {
    private String date;
    private String message;
    private String ID;
    private String eventTitle;
    private String dateForView;
    private boolean responseStatus;

    public CustomNotification(String date, String message, String ID, /*String eventTitle,*/ boolean responseStatus) {
        this.date = date;
        this.message = message;
        this.ID = ID;
//        this.eventTitle = eventTitle;
        this.dateForView = HelperMethods.formatDateForView(this.date);
        this.responseStatus = responseStatus;
    }

//    public String getEventTitle() {
//        return this.eventTitle;
//    }

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

    public boolean getResponseStatus() {
        return this.responseStatus;
    }
}
