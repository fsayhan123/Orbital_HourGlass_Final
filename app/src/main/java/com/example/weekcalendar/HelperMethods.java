package com.example.weekcalendar;

import java.util.HashMap;
import java.util.Map;

public class HelperMethods {
    private static Map<String, String> mapper = new HashMap<String, String>()
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

    public static String convertMonth(String month) {
        return mapper.get(month);
    }

    public static String formatDate(String date) {
        String[] startDateArr = date.split(" ");
        startDateArr[1] = convertMonth(startDateArr[1].substring(0,3));
        if (startDateArr[0].length() == 1) {
            startDateArr[0] = "0" + startDateArr[0];
        }
        return String.join("-", startDateArr[2], startDateArr[1], startDateArr[0]);
    }
}
