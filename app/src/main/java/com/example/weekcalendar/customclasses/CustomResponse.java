package com.example.weekcalendar.customclasses;

public class CustomResponse {
    private String date;
    private int count;

    public CustomResponse(String date, int count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return this.date;
    }

    public String getCount() {
        return String.valueOf(this.count);
    }
}
