package com.example.weekcalendar.helperclasses;

import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class HelperMethods {
    private static Map<String, String> stringToNumMonth = new HashMap<String, String>()
    {{
        put("Jan", "01");
        put("Feb", "02");
        put("Mar", "03");
        put("Apr", "04");
        put("May", "05");
        put("Jun", "06");
        put("Jul", "07");
        put("Aug", "08");
        put("Sep", "09");
        put("Oct", "10");
        put("Nov", "11");
        put("Dec", "12");
    }};

    public static String[] numToStringMonth = new String[] { "",
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December" };

    public static String convertMonth(String month) {
        return stringToNumMonth.get(month);
    }

    public static String formatDateWithDash(String date) {
        String[] dateArr = date.split(" ");
        dateArr[1] = convertMonth(dateArr[1].substring(0,3));
        if (dateArr[0].length() == 1) {
            dateArr[0] = "0" + dateArr[0];
        }
        return String.join("-", dateArr[2], dateArr[1], dateArr[0]);
    }

    public static String formatDateForView(String date) {
        String[] dateArr = date.split("-");
        return String.join(" ", dateArr[2], numToStringMonth[Integer.parseInt(dateArr[1])], dateArr[0]);
    }

    // ensure is 12h format, with AM/ PM notation, specifically of this format: " xx:yy AM "
    public static String formatTimeTo24H(String time) {
        String[] getAMOrPM = time.split(" ");
        String amOrPM = getAMOrPM[1];
        String[] timeArr = getAMOrPM[0].split(":");
        if (amOrPM.equalsIgnoreCase("PM") && Integer.parseInt(timeArr[0]) < 12) {
            timeArr[0] = String.valueOf(Integer.parseInt(timeArr[0]) + 12);
        } else if (amOrPM.equalsIgnoreCase("AM") && Integer.parseInt(timeArr[0]) == 12) {
            timeArr[0] = "00";
        }
        if (amOrPM.equalsIgnoreCase("AM") && timeArr[0].length() == 1) {
            timeArr[0] = "0" + timeArr[0];
        }
        return timeArr[0] + ":" + timeArr[1];
    }

    public static String formatTimeTo12H(String time) {
        String[] timeArr = time.split(":");
        String amOrPM;
        if (timeArr[0].equals("00")) {
            timeArr[0] = "12";
            amOrPM = "AM";
        } else if (Integer.parseInt(timeArr[0]) > 12) {
            timeArr[0] = String.format("%02d", Integer.parseInt(timeArr[0]) - 12);
            amOrPM = "PM";
        } else {
            amOrPM = "AM";
        }
        return timeArr[0] + ":" + timeArr[1] + " " + amOrPM;
    }

    public static String toGoogleDateTime(String date, String time) {
        return date + "T" + time + ":00+08:00";
    }

    public static String getCurrDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime date = LocalDateTime.now();
        return dtf.format(date);
    }
}
