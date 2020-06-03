package com.example.weekcalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToDoListDataPump {
    List<CustomDay> daysList;

    //Constructs the DataPump with the days with ToDoEvents
    public ToDoListDataPump(List<CustomDay> daysList) {
        this.daysList = daysList;
    }

    //Returns a Hashmap of days and their respective To Do's
    public HashMap<CustomDay, List<String>> getData() {
        HashMap<CustomDay, List<String>> expandableListDetail = new HashMap<CustomDay, List<String>>();

        for (CustomDay day: this.daysList) {
            List<String> contentList = new ArrayList<>();
            //Nonsense Data
            contentList.add("Buy Food");
            contentList.add("Buy Groceries");
            contentList.add("Get BAE");
            //Insert SQL query here to extract ToDoList for respective date
            //Can also query once from DB and iterate thru cursor each time a new day is checked, unsure which is faster tbh

            expandableListDetail.put(day, contentList);
        }
        return expandableListDetail;
    }
}