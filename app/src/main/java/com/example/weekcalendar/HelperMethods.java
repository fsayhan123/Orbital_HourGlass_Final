package com.example.weekcalendar;

import java.util.HashMap;
import java.util.Map;

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

    private static String[] numToStringMonth = new String[] { "",
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December" };

    public static String convertMonth(String month) {
        return stringToNumMonth.get(month);
    }

    public static String formatDateForFirebase(String date) {
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
        assert time.substring(time.length() - 2).equalsIgnoreCase("AM") || time.substring(time.length() - 2).equalsIgnoreCase("PM");
        String[] getAMOrPm = time.split(" ");
        String amOrPM = getAMOrPm[1];
        String[] timeArr = getAMOrPm[0].split(":");
        if (amOrPM.equalsIgnoreCase("PM") && Integer.parseInt(timeArr[0]) < 12) {
            timeArr[0] = String.valueOf(Integer.parseInt(timeArr[0]) + 12);
        }
        if (amOrPM.equalsIgnoreCase("AM") && timeArr[0].length() == 1) {
            timeArr[0] = "0" + timeArr[0];
        }
        return timeArr[0] + ":" + timeArr[1];
    }
}
