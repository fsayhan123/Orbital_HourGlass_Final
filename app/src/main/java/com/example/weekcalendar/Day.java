package com.example.weekcalendar;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/*
 to encapsulate a Date and return it in the format as required.
 */
public class Day {
    private String time;
    private Date today;
    private String dateAsString;
    private String num;
    private int dd;
    private String MMMMMM;
    private String MMM;
    private int yyyy;
    private int numEvents;

    static String[] months = new String[] { "",
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December" };

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");

    public Day(Date d) {
        this.today = d;
        LocalDate temp = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.dd = temp.getDayOfMonth();
        this.MMMMMM = months[temp.getMonthValue()];
        this.MMM = this.MMMMMM.substring(0, 3);
        this.yyyy = temp.getYear();
        this.time = dateFormatter.format(d.getTime());
        this.numEvents = 5;
//        this.listOfEvents = new TreeSet<>((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
    }

    public String getdd() {
        return "" + this.dd;
    }

    public String getMMMMMM() {
        return this.MMMMMM;
    }

    public String getMMM() {
        return this.MMM;
    }

    public String getyyyy() {
        return "" + this.yyyy;
    }

    public String getDate() {
        return this.dd + " " + this.MMMMMM + " " + this.yyyy;
    }

    public String getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        return this.dd + " " + this.MMMMMM + " " + this.yyyy;
    }

    public int getNumEvents() {
        return this.numEvents;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other instanceof Day) {
            Day d2 = (Day) other;
            return this.today.equals(d2.today);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.today.hashCode();
    }
}
