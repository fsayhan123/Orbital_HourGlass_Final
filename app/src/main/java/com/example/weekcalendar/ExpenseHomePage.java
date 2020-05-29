package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseHomePage extends AppCompatActivity {
    private RecyclerView expensesByDay;
    private ExpenseRecylerViewAdapter dayExpenseAdapter;
    private List<Day> daysISpent;
    private Map<Day, List<ExpenseCategory>> spendingEachDay;
    private FloatingActionButton addExpense;
    private DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_home_page);
        myDB = new DatabaseHelper(this);
        try {
            daysISpent = getSpendingDays();
        } catch (ParseException e) {
            daysISpent = new ArrayList<Day>();
        }
        spendingEachDay = getSpendingEachDay();

        expensesByDay = findViewById(R.id.day_by_day_expense_view);
        dayExpenseAdapter = new ExpenseRecylerViewAdapter(daysISpent, spendingEachDay, ExpenseHomePage.this);

        LinearLayoutManager manager = new LinearLayoutManager(ExpenseHomePage.this);
        expensesByDay.setHasFixedSize(true);
        expensesByDay.setLayoutManager(manager);
        expensesByDay.setAdapter(dayExpenseAdapter);

        addExpense = findViewById(R.id.to_add_expense);
        addExpense.setOnClickListener(v -> moveToAddExpensePage());
    }

    private void moveToAddExpensePage() {
        Intent i = new Intent(this, CreateExpense.class);
        startActivity(i);
    }

    //Returns a Map of each day along with expense category
    private Map<Day, List<ExpenseCategory>> getSpendingEachDay() {
        spendingEachDay = new HashMap<>();
        for (Day d : daysISpent) {
            String daySQL = d.getdd() + " " + d.getMMM() + " " + d.getyyyy();
            Cursor result = myDB.getExpenseData(daySQL);
            // query for each day the categories of expenses
            HashMap<String, List<Expense>> catHashMap = new HashMap<>();
            List<ExpenseCategory> temp = new ArrayList<>();
            ExpenseCategory exCat;

            for (int i = 0; i < result.getCount(); i++) {
                result.moveToNext();
                String category = result.getString(2);
                String name = result.getString(4);
                String amount = result.getString(3);
                if (catHashMap.containsKey(category)) {
                    List<Expense> expenseCategory = new ArrayList<>(catHashMap.get(category));
                    expenseCategory.add(new Expense(name, Double.valueOf(amount)));
                    catHashMap.put(category, expenseCategory);
                } else {
                    List<Expense> expenseCategory = new ArrayList<>();
                    expenseCategory.add(new Expense(name, Double.valueOf(amount)));
                    catHashMap.put(category, expenseCategory);
                }
            }

            for (Map.Entry<String, List<Expense>> entry : catHashMap.entrySet()) {
                String key = entry.getKey();
                List<Expense> value = entry.getValue();
                exCat = new ExpenseCategory(key, value);
                temp.add(exCat);
            }
            spendingEachDay.put(d, temp);
        }
        return spendingEachDay;
    }

    //Returns a List<Day> that user has a spending;
    private List<Day> getSpendingDays() throws ParseException {
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
            Day day = new Day(date);
            if (daysISpent.contains(day)) {
                continue;
            } else {
                daysISpent.add(day);
            }} return daysISpent;
    }
}
