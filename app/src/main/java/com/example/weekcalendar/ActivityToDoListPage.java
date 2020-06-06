package com.example.weekcalendar;


import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ActivityToDoListPage extends AppCompatActivity {

    ToDoListDataPump toDoListDataPump;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<CustomDay> expandableListTitle;
    HashMap<CustomDay, List<String>> expandableListDetail;

    private DatabaseHelper myDB;
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_page);
        myDB = new DatabaseHelper(this);

        try {
            this.toDoListDataPump = new ToDoListDataPump(getToDoDays());
        } catch (ParseException e) {
            Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show();
            this.toDoListDataPump = new ToDoListDataPump(new ArrayList<>());
        }

        expandableListView = findViewById(R.id.expandableListView);
        expandableListDetail = this.toDoListDataPump.getData();
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new ToDoListViewAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        expandableListTitle.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });

        Toolbar myChildToolbar = findViewById(R.id.to_do_list);
        setSupportActionBar(myChildToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public List<CustomDay> getToDoDays() throws ParseException {
        List<CustomDay> temp = new ArrayList<>();
        // Insert query here
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
                if (temp.contains(customDay)) {
                    // we should try and change this, very inefficient, should not check for contains
                    continue;
                } else {
                    temp.add(customDay);
                }
            }
        }
        return temp;
    }
}