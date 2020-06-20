package com.example.weekcalendar;

public class CustomToDo {
    private String ID;
    private String details;
    private String date;

    public CustomToDo(String ID, String details, String date) {
        this.ID = ID;
        this.details = details;
        this.date = date;
    }

    protected String getID() {
        return this.ID;
    }

    protected String getDetails() {
        return this.details;
    }

    protected String getDate() {
        return this.date;
    }
}
