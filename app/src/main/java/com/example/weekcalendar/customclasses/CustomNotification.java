package com.example.weekcalendar.customclasses;

public class CustomNotification {
    String date;
    String message;
    String ID;

    public CustomNotification(String date, String message, String ID) {
        this.date = date;
        this.message = message;
        this.ID = ID;
    }

    public String getDate() {
        return this.date;
    }

    public String getMessage() {
        return this.message;
    }

    public String getID() {
        return this.ID;
    }
}
