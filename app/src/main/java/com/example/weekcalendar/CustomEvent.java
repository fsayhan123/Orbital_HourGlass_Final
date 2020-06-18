package com.example.weekcalendar;

/*
 to store data relevant to each event
 */

public class CustomEvent {
    private String id;
    private String title;
    private String date;
    private String time;

    public CustomEvent(String title, String date, String time) {
        this.title = title;
        this.date = date;
        this.time = time;
    }

    public String getId() { return this.id; }

    public String getDate() {
        return this.date;
    }

    public String getTime() {
        return this.time;
    }

    public String getTitle() {
        return this.title;
    }
}
