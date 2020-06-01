package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestExpandableList extends AppCompatActivity {
    private List<String> forExList;
    private Map<String, List<String>> map;
    private List<String> sectionHeader;

    private RecyclerView test100;
    private TestExListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_expandable_list);

        forExList = new ArrayList<>();
        map = new HashMap<>();
        sectionHeader = new ArrayList<>();

        initListData();
        test100 = findViewById(R.id.test100);
        adapter = new TestExListAdapter(sectionHeader, forExList, map, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        test100.setHasFixedSize(true);
        test100.setLayoutManager(manager);
        test100.setAdapter(adapter);
    }

    private void initListData() {
        for (int i = 0; i < 6; i++) {
            sectionHeader.add("Mega Group " + i);
        }


        forExList.add(getString(R.string.group1));
        forExList.add(getString(R.string.group2));
        forExList.add(getString(R.string.group3));
        forExList.add(getString(R.string.group4));
        forExList.add(getString(R.string.group5));
        forExList.add(getString(R.string.group6));

        String[] array;
        List<String> list1 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group1);
        for (String item : array) {
            list1.add(item);
        }
        List<String> list2 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group2);
        for (String item : array) {
            list2.add(item);
        }
        List<String> list3 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group3);
        for (String item : array) {
            list3.add(item);
        }
        List<String> list4 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group4);
        for (String item : array) {
            list4.add(item);
        }
        List<String> list5 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group5);
        for (String item : array) {
            list5.add(item);
        }
        List<String> list6 = new ArrayList<>();
        array = getResources().getStringArray(R.array.group6);
        for (String item : array) {
            list6.add(item);
        }

        map.put(forExList.get(0), list1);
        map.put(forExList.get(1), list2);
        map.put(forExList.get(2), list3);
        map.put(forExList.get(3), list4);
        map.put(forExList.get(4), list5);
        map.put(forExList.get(5), list6);

        Toast.makeText(this, "Lists created", Toast.LENGTH_SHORT).show();
    }
}