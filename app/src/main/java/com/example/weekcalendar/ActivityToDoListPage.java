package com.example.weekcalendar;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityToDoListPage extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private ToDoListViewAdapter expandableListAdapter;
    private Map<CustomDay, List<String>> expandableListDetail;
    private List<CustomDay> listOfDays;

    private DatabaseHelper myDB;
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private SetupNavDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_page);
        myDB = new DatabaseHelper(this);

        try {
            this.listOfDays = getToDoDays();
        } catch (ParseException e) {
            Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show();
            this.listOfDays = new ArrayList<>();
        }

        expandableListDetail = getData();
        expandableListView = findViewById(R.id.expandableListView);
        expandableListAdapter = new ToDoListViewAdapter(this, this.listOfDays, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listOfDays.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listOfDays.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        listOfDays.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                listOfDays.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });

        navDrawer = new SetupNavDrawer(this, findViewById(R.id.todo_toolbar));
        navDrawer.setupNavDrawerPane();
    }

    public List<CustomDay> getToDoDays() throws ParseException {
        List<CustomDay> temp = new ArrayList<>();
        Cursor query = myDB.getToDo();
        if (query.getCount() == 0) {
            return temp;
        } else {
            for (int i = 0; i < 30; i++) {
                if (i >= query.getCount()) {
                    break;
                }
                query.moveToNext();
                String result = query.getString(0);
                Date date = dateFormatter.parse(result);
                CustomDay customDay = new CustomDay(date);
                temp.add(customDay);
            }
        }
        return temp;
    }

    public void createToDo(View view) {
        Intent intent = new Intent(this, ActivityCreateToDoPage.class);
        startActivity(intent);
    }

    public void deleteToDo(View view) {
        Set<Pair<Long, Long>> setItems = expandableListAdapter.getCheckedItems();
        for (Pair<Long, Long> pair : setItems) {
            myDB.deleteToDo(expandableListAdapter.getChild((int) (long) pair.first, (int) (long) pair.second).toString(), expandableListAdapter.getGroup((int) (long) pair.first).toString());
        }

        Intent intent = new Intent(this, ActivityToDoListPage.class);
        startActivity(intent);
    }

    public Map<CustomDay, List<String>> getData() {
        expandableListDetail = new HashMap<>();
        for (CustomDay d : this.listOfDays) {
            String day = d.getdd();
            if (day.length() == 1) {
                day = "0" + day;
            }
            String daySQL = d.getyyyy() + "-" + myDB.convertDate(d.getMMM()) + "-" + day;
            Cursor result = myDB.getToDo(daySQL);
            expandableListDetail.put(d, new ArrayList<>());
            for (int i = 0; i < result.getCount(); i++) {
                result.moveToNext();
                String description = result.getString(2);
                expandableListDetail.get(d).add(description);
            }
        }
        return expandableListDetail;
    }

}