package com.example.weekcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityExpensePage extends AppCompatActivity implements MyOnDateClickListener {
    /*
    RecyclerView and associated adapter, and List<CustomDay> to populate outer RecyclerView (just the dates),
    and a Map with key-value pair of CustomDay and a List<CustomExpenseCategory>, representing
    the spending in each category each day.
     */
    private List<CustomDay> daysWithExpenditure;
    private RecyclerView expensesByDay;
    private ExpenseRecyclerViewAdapter dayExpenseAdapter;
    private LinearLayoutManager manager;
    private Map<CustomDay, List<CustomExpenseCategory>> spendingEachDay;

    private final static String LIST_STATE_KEY = "recycler_list_state";
    private Parcelable mListState;

    // FloatingActionButton to link to ActivityCreateExpense
    private FloatingActionButton floatingAddExpense;

    // Database handler
    private DatabaseHelper myDB;

    private SetupNavDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);
        myDB = new DatabaseHelper(this);

        try {
            daysWithExpenditure = getSpendingDays();
            //daysWithExpenditure.sort((d1, d2) -> d1.compareTo(d2));
        } catch (ParseException e) {
            daysWithExpenditure = new ArrayList<>();
        }

        spendingEachDay = getSpendingEachDay();

        expensesByDay = findViewById(R.id.day_by_day_expense_view);
        dayExpenseAdapter = new ExpenseRecyclerViewAdapter(daysWithExpenditure, spendingEachDay,
                ActivityExpensePage.this, this);

        manager = new LinearLayoutManager(ActivityExpensePage.this);
        expensesByDay.setHasFixedSize(true);
        expensesByDay.setLayoutManager(manager);
        expensesByDay.setAdapter(dayExpenseAdapter);

        floatingAddExpense = findViewById(R.id.to_add_expense);
        floatingAddExpense.setOnClickListener(v -> moveToAddExpensePage());

        // Navigation pane drawer setup
        navDrawer = new SetupNavDrawer(this, findViewById(R.id.expenses_toolbar));
        navDrawer.setupNavDrawerPane();
    }

    private void moveToAddExpensePage() {
        Intent i = new Intent(this, ActivityCreateExpensePage.class);
        startActivity(i);
    }

    // Returns a Map of each day along with expense category
    private Map<CustomDay, List<CustomExpenseCategory>> getSpendingEachDay() {
        spendingEachDay = new HashMap<>();
        for (CustomDay d : daysWithExpenditure) {
            String day = d.getdd();
            if (day.length() == 1) {
                day = "0" + day;
            }
            String daySQL = d.getyyyy() + "-" + myDB.convertDate(d.getMMM()) + "-" + day;
            Cursor result = myDB.getDayExpenseData(daySQL);
            HashMap<String, List<CustomExpense>> catHashMap = new HashMap<>();
            List<CustomExpenseCategory> temp = new ArrayList<>();
            CustomExpenseCategory exCat;

            for (int i = 0; i < result.getCount(); i++) {
                result.moveToNext();
                int id = Integer.valueOf(result.getString(0));
                String category = result.getString(2);
                String name = result.getString(4);
                String amount = result.getString(3);
                if (catHashMap.containsKey(category)) {
                    catHashMap.get(category).add(new CustomExpense(id, name, Double.valueOf(amount)));
                } else {
                    List<CustomExpense> customExpenseCategory = new ArrayList<>();
                    customExpenseCategory.add(new CustomExpense(id, name, Double.valueOf(amount)));
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

    // Returns a List<CustomDay> that user has a spending;
    private List<CustomDay> getSpendingDays() throws ParseException {
        daysWithExpenditure = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Cursor query = this.myDB.getDayExpenseData();
        if (query.getCount() == 0) {
            return daysWithExpenditure;
        }
        for (int i = 0; i < 30; i++) {
            if (i >= query.getCount()) {
                break;
            }
            query.moveToNext();
            String result = query.getString(0);
            Date date = dateFormatter.parse(result);
            CustomDay customDay = new CustomDay(date);
            daysWithExpenditure.add(customDay);
        }
        return daysWithExpenditure;
    }

    @Override
    public void onDateClickListener(String date) {
        Toast.makeText(this, "clicked " + date, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ActivityEachDayExpenses.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }
}