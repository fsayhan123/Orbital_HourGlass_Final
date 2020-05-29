package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityExpensePage extends AppCompatActivity {
    private RecyclerView expensesByDay;
    private ExpenseRecylerViewAdapter dayExpenseAdapter;
    private List<CustomDay> daysISpent;
    private Map<CustomDay, List<CustomExpenseCategory>> spendingEachDay;
    private FloatingActionButton addExpense;
    private DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);
        myDB = new DatabaseHelper(this);
        try {
            daysISpent = getSpendingDays();
        } catch (ParseException e) {
            daysISpent = new ArrayList<CustomDay>();
        }
        spendingEachDay = getSpendingEachDay();

        expensesByDay = findViewById(R.id.day_by_day_expense_view);
        dayExpenseAdapter = new ExpenseRecylerViewAdapter(daysISpent, spendingEachDay, ActivityExpensePage.this);

        LinearLayoutManager manager = new LinearLayoutManager(ActivityExpensePage.this);
        expensesByDay.setHasFixedSize(true);
        expensesByDay.setLayoutManager(manager);
        expensesByDay.setAdapter(dayExpenseAdapter);

        addExpense = findViewById(R.id.to_add_expense);
        addExpense.setOnClickListener(v -> moveToAddExpensePage());
    }

    private void moveToAddExpensePage() {
        Intent i = new Intent(this, ActivityCreateExpensePage.class);
        startActivity(i);
    }

    //Returns a Map of each day along with expense category
    private Map<CustomDay, List<CustomExpenseCategory>> getSpendingEachDay() {
        spendingEachDay = new HashMap<>();
        for (CustomDay d : daysISpent) {
            String daySQL = d.getdd() + " " + d.getMMM() + " " + d.getyyyy();
            Cursor result = myDB.getExpenseData(daySQL);
            // query for each day the categories of expenses
            HashMap<String, List<CustomExpense>> catHashMap = new HashMap<>();
            List<CustomExpenseCategory> temp = new ArrayList<>();
            CustomExpenseCategory exCat;

            for (int i = 0; i < result.getCount(); i++) {
                result.moveToNext();
                String category = result.getString(2);
                String name = result.getString(4);
                String amount = result.getString(3);
                if (catHashMap.containsKey(category)) {
                    List<CustomExpense> customExpenseCategory = new ArrayList<>(catHashMap.get(category));
                    customExpenseCategory.add(new CustomExpense(name, Double.valueOf(amount)));
                    catHashMap.put(category, customExpenseCategory);
                } else {
                    List<CustomExpense> customExpenseCategory = new ArrayList<>();
                    customExpenseCategory.add(new CustomExpense(name, Double.valueOf(amount)));
                    catHashMap.put(category, customExpenseCategory);
                }
            }

            for (Map.Entry<String, List<CustomExpense>> entry : catHashMap.entrySet()) {
                String key = entry.getKey();
                List<CustomExpense> value = entry.getValue();
                exCat = new CustomExpenseCategory(key, value);
                temp.add(exCat);
            }
            spendingEachDay.put(d, temp);
        }
        return spendingEachDay;
    }

    //Returns a List<CustomDay> that user has a spending;
    private List<CustomDay> getSpendingDays() throws ParseException {
        // query here
        daysISpent = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
        Cursor query = this.myDB.getExpenseData();
        if (query.getCount() == 0) {
            return daysISpent;
        }

        for (int i = 0; i < 30; i++) {
            if (i >= query.getCount()) {
                break;
            }
            query.moveToNext();
            String result = query.getString(1);
            Date date = dateFormatter.parse(result);
            CustomDay customDay = new CustomDay(date);
            if (daysISpent.contains(customDay)) {
                continue;
            } else {
                daysISpent.add(customDay);
            }} return daysISpent;
    }
}
