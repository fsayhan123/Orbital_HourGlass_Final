package com.example.weekcalendar.customclasses;

import com.example.weekcalendar.helperclasses.HelperMethods;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/*
 to encapsulate a Date and return it in the format as required.
 */
public class CustomDay implements Comparable<CustomDay> {
    private String time;
    private Date today;
    private int dd;
    private String MMMMMM;
    private String MMM;
    private String MM;
    private int yyyy;

    private static String[] months = new String[] { "",
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December" };

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");

    public CustomDay(Date d) {
        this.today = d;
        LocalDate temp = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.dd = temp.getDayOfMonth();
        this.MMMMMM = months[temp.getMonthValue()];
        this.MMM = this.MMMMMM.substring(0, 3);
        this.MM = HelperMethods.convertMonth(this.MMM);
        this.yyyy = temp.getYear();
        this.time = dateFormatter.format(d.getTime());
    }

    public String getdd() {
        return "" + this.dd;
    }

    public String getMMM() {
        return this.MMM;
    }

    public String getyyyy() {
        return "" + this.yyyy;
    }

    public String getFullDateForView() {
        return this.dd + " " + this.MMMMMM + " " + this.yyyy;
    }

    public String getDateForFirebase() {
        return this.yyyy + "-" + this.MM + "-" + this.dd;
    }

    public String getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        return this.dd + " " + this.MMMMMM + " " + this.yyyy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomDay customDay = (CustomDay) o;
        return dd == customDay.dd &&
                yyyy == customDay.yyyy &&
                Objects.equals(getTime(), customDay.getTime()) &&
                today.equals(customDay.today) &&
                Objects.equals(MMMMMM, customDay.MMMMMM) &&
                Objects.equals(getMMM(), customDay.getMMM());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), today, dd, MMMMMM, getMMM(), yyyy);
    }

    @Override
    public int compareTo(CustomDay o) {
        return this.today.compareTo(o.today);
    }
}
