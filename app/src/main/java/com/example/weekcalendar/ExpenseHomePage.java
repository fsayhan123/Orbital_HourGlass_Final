package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseHomePage extends AppCompatActivity {
    private RecyclerView expensesByDay;
    private ExpenseRecylerViewAdapter dayExpenseAdapter;
    private List<Day> daysISpent;
    private Map<Day, List<ExpenseCategory>> spendingEachDay;
    private FloatingActionButton addExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_home_page);

        daysISpent = getSpendingDays();
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

    private Map<Day, List<ExpenseCategory>> getSpendingEachDay() {
        spendingEachDay = new HashMap<>();
        for (Day d : daysISpent) {
            // query for each day the categories of expenses
            List<ExpenseCategory> temp = new ArrayList<>();
            ExpenseCategory exCat;
            for (int i = 0; i < 3; i++) {
                List<Expense> whatIAte = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    whatIAte.add(new Expense("Chicken Rice " + j));
                }
                exCat = new ExpenseCategory("Food " + i, whatIAte);
                temp.add(exCat);
                if (exCat.getItems() == null) {
                    Toast.makeText(this, "empty expenses list in this category", Toast.LENGTH_SHORT).show();
                }
            }
            spendingEachDay.put(d, temp);
        }
        return spendingEachDay;
    }

    private List<Day> getSpendingDays() {
        // query here
        daysISpent = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        daysISpent.add(new Day(c.getTime()));
        for (int i = 0; i < 30; i++) {
            c.add(Calendar.DATE, 2);
            daysISpent.add(new Day(c.getTime()));
        }
        return daysISpent;
    }
}
