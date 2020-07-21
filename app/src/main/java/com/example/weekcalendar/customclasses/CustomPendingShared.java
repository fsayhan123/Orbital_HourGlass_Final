package com.example.weekcalendar.customclasses;

public class CustomPendingShared {
    private String responseID;
    private String title;

    public CustomPendingShared(String responseID, String title) {
        this.title = title;
        this.responseID = responseID;
    }

    public String getID() {
        return this.responseID;
    }

    public String getTitle() {
        return this.title;
    }
}
