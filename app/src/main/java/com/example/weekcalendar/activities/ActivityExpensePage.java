package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.weekcalendar.helperclasses.DatabaseHelper;
import com.example.weekcalendar.adapters.ExpenseRecyclerViewAdapter;
import com.example.weekcalendar.helperclasses.MyOnDateClickListener;
import com.example.weekcalendar.R;
import com.example.weekcalendar.helperclasses.SetupNavDrawer;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomExpense;
import com.example.weekcalendar.customclasses.CustomExpenseCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private static final String TAG = ActivityExpensePage.class.getSimpleName();

    private List<CustomDay> daysWithExpenditure;
    private RecyclerView expensesByDay;
    private ExpenseRecyclerViewAdapter dayExpenseAdapter;
    private LinearLayoutManager manager;
    //private List<CustomExpenseCategory> customExpenseCategoriesList = new ArrayList<>();
    private Map<CustomDay, List<CustomExpenseCategory>> spendingEachDay = new HashMap<>();

    // FloatingActionButton to link to ActivityCreateExpense
    private FloatingActionButton floatingAddExpense;

    private SetupNavDrawer navDrawer;

    //Firebase fields
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private String userID;

    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);

        //Setup firebase fields
        this.db = FirebaseFirestore.getInstance();
        this.fAuth = FirebaseAuth.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        getSpendingDays();

        this.expensesByDay = findViewById(R.id.day_by_day_expense_view);
        dayExpenseAdapter = new ExpenseRecyclerViewAdapter(daysWithExpenditure, spendingEachDay,
                ActivityExpensePage.this, this);

        this.manager = new LinearLayoutManager(ActivityExpensePage.this);
        this.expensesByDay.setHasFixedSize(true);
        this.expensesByDay.setLayoutManager(this.manager);
        expensesByDay.setAdapter(dayExpenseAdapter);

        this.floatingAddExpense = findViewById(R.id.to_add_expense);
        this.floatingAddExpense.setOnClickListener(v -> moveToAddExpensePage());

        // Navigation pane drawer setup
        this.navDrawer = new SetupNavDrawer(this, findViewById(R.id.expenses_toolbar));
        this.navDrawer.setupNavDrawerPane();

        for (CustomDay day : spendingEachDay.keySet()) {
            List<CustomExpenseCategory> list = spendingEachDay.get(day);
            Log.d(TAG, list.toString());
        }
    }

    private void moveToAddExpensePage() {
        Intent i = new Intent(this, ActivityCreateExpensePage.class);
        startActivity(i);
    }

    // Returns a List<CustomDay> that user has a spending;
    private void getSpendingDays() {
        this.daysWithExpenditure = new ArrayList<>();
        this.db.collection("expense")
                .whereEqualTo("userID", this.userID)
                .orderBy("Date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("TAG", "onSuccess: LIST EMPTY");
                        } else {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
                            }
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots) {
                                prepHashMap(daysWithExpenditure);
                                prepSpending(document);
                            }
                            dayExpenseAdapter = new ExpenseRecyclerViewAdapter(daysWithExpenditure, spendingEachDay,
                                    ActivityExpensePage.this, ActivityExpensePage.this);
                            expensesByDay.setAdapter(dayExpenseAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "hello!!!!!!!!!!!!!!!!!!! " + e.getLocalizedMessage());
                    }
                });
    }

    //Get the days with expenditure and add them into the list
    private void processDocument(QueryDocumentSnapshot document) {
        String date = document.get("Date").toString();
        Date d = null;
        try {
            d = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(d);
        if (!(this.daysWithExpenditure.contains(day))) {
            this.daysWithExpenditure.add(day);
        }
    }

    //Fill Hashmap with days
    private void prepHashMap(List<CustomDay> dayList) {
        for (CustomDay day : dayList) {
            if (!(this.spendingEachDay.containsKey(day))) {
                List<CustomExpenseCategory> customCategoryList = new ArrayList<>();
                this.spendingEachDay.put(day, customCategoryList);
            }
        }
    }

    //Prepare hashmap
    private void prepSpending(QueryDocumentSnapshot document) {
        String date = document.get("Date").toString();
        String category = document.get("Category").toString();
        String name = document.get("Name").toString();
        double amount = new Double(document.get("Amount").toString());
        Date d = null;
        try {
            d = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(d);
        List<CustomExpenseCategory> customExpenseCategoriesList = this.spendingEachDay.get(day);
        boolean flag = false;
        for (CustomExpenseCategory cat : customExpenseCategoriesList) {
            if (cat.getName().equals(category)) {
                cat.addExpense(new CustomExpense(document.getId(), name, amount));
                flag = true;
            }
        }

        if (!(flag)) {
            CustomExpenseCategory newCat = new CustomExpenseCategory(category, new ArrayList<>());
            newCat.addExpense(new CustomExpense(document.getId(), name, amount));
            customExpenseCategoriesList.add(newCat);
        }
    }

    @Override
    public void onDateClickListener(String date) {
        Toast.makeText(this, "clicked " + date, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ActivityEachDayExpenses.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }
}