package com.example.weekcalendar;

/*
 to store data relevant to each event
 */

public class CustomEvent {
    private String title;
    private String date;
    private String description;

    public CustomEvent(String title, String d, String description) {
        this.title = title;
        this.date = d;
        this.description = description;
    }

    public String getDate() {
        return this.date;
    }

    public String getTitle() {
        return this.title;
    }
}
