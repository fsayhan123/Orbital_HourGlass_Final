package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class ExpenseHomePage extends AppCompatActivity {
    private RecyclerView expensesByDay;
    private ExpenseRecylerViewAdapter expenseAdapter;
    private List<Day> spendingDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_home_page);

        expensesByDay = findViewById(R.id.category_and_expenses);
        Map<Day, Map<Integer, SortedMap<String, Double>>> dayCategoryExpense = new HashMap<>();
        spendingDays = new ArrayList<>();

        Day d = new Day(new Date());
        SortedMap<String, Double> s = new SortedMap<String, Double>() {
            @Nullable
            @Override
            public Comparator<? super String> comparator() {
                return null;
            }

            @NonNull
            @Override
            public SortedMap<String, Double> subMap(String fromKey, String toKey) {
                return null;
            }

            @NonNull
            @Override
            public SortedMap<String, Double> headMap(String toKey) {
                return null;
            }

            @NonNull
            @Override
            public SortedMap<String, Double> tailMap(String fromKey) {
                return null;
            }

            @Override
            public String firstKey() {
                return null;
            }

            @Override
            public String lastKey() {
                return null;
            }

            @NonNull
            @Override
            public Set<String> keySet() {
                return null;
            }

            @NonNull
            @Override
            public Collection<Double> values() {
                return null;
            }

            @NonNull
            @Override
            public Set<Entry<String, Double>> entrySet() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(@Nullable Object key) {
                return false;
            }

            @Override
            public boolean containsValue(@Nullable Object value) {
                return false;
            }

            @Nullable
            @Override
            public Double get(@Nullable Object key) {
                return null;
            }

            @Nullable
            @Override
            public Double put(String key, Double value) {
                return null;
            }

            @Nullable
            @Override
            public Double remove(@Nullable Object key) {
                return null;
            }

            @Override
            public void putAll(@NonNull Map<? extends String, ? extends Double> m) {

            }

            @Override
            public void clear() {

            }
        };
        s.put("Food", 5.00);
        Map<Integer, SortedMap<String, Double>> m = new HashMap<>();
        m.put(0, s);
        spendingDays.add(d);
        dayCategoryExpense.put(d, m);

        expenseAdapter = new ExpenseRecylerViewAdapter(dayCategoryExpense, spendingDays, ExpenseHomePage.this);

        LinearLayoutManager manager = new LinearLayoutManager(ExpenseHomePage.this);
        expensesByDay.setHasFixedSize(true);
        expensesByDay.setLayoutManager(manager);
        expensesByDay.setAdapter(expenseAdapter);
    }
}
