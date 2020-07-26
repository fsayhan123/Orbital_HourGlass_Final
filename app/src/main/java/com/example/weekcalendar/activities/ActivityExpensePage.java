package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sets up ActivityExpensePage by querying data from Firebase expenses collection relevant to the user logged in.
 */
public class ActivityExpensePage extends AppCompatActivity implements MyOnDateClickListener {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityExpensePage.class.getSimpleName();

    /*
    RecyclerView and associated adapter, and List<CustomDay> to populate outer RecyclerView (just the dates),
    and a Map with key-value pair of CustomDay and a List<CustomExpenseCategory>, representing
    the spending in each category each day.
     */
    private List<CustomDay> listOfDaysWithExpenditure;
    private Set<CustomDay> setOfDaysWithExpenditure;
    private RecyclerView expensesByDay;
    private ExpenseRecyclerViewAdapter dayExpenseAdapter;
    private Map<CustomDay, List<CustomExpenseCategory>> spendingEachDay;

    /**
     * Firebase information
     */
    private FirebaseFirestore db;
    private String userID;

    /**
     * DateFormat to format Dates and Strings
     */
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Sets up ActivityExpensePage when it is opened.
     * First, sets up Firebase information.
     * Then, sets up layout items by calling setupXMLItems();
     * Finally, fetches data from Firebase by calling getSpendingDays() method.
     * @param savedInstanceState saved state of current page, if applicable
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);

        this.db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        setupXMLItems();

        getSpendingDays();
    }

    /**
     * Sets up layout for ActivityExpensePage.
     */
    private void setupXMLItems() {
        this.expensesByDay = findViewById(R.id.day_by_day_expense_view);

        LinearLayoutManager manager = new LinearLayoutManager(ActivityExpensePage.this);
        this.expensesByDay.setHasFixedSize(true);
        this.expensesByDay.setLayoutManager(manager);
        this.expensesByDay.setAdapter(this.dayExpenseAdapter);

        FloatingActionButton floatingAddExpense = findViewById(R.id.to_add_expense);
        floatingAddExpense.setOnClickListener(v -> moveToAddExpensePage());

        SetupNavDrawer navDrawer = new SetupNavDrawer(this, findViewById(R.id.expenses_toolbar));
        navDrawer.setupNavDrawerPane();
    }

    /**
     * Creates and starts intent when the FloatingActionButton in the XML is clicked. Links to
     * ActivityCreateExpensePage.
     */
    private void moveToAddExpensePage() {
        Intent i = new Intent(this, ActivityCreateExpensePage.class);
        startActivity(i);
    }

    /**
     * Initialises data structures required to pass into layout adapters.
     * Also queries expense data from Firebase database which are relevant to the user, ordered by date.
     * Populates data structures, and initialises layout adapters.
     */
    private void getSpendingDays() {
        this.setOfDaysWithExpenditure = new HashSet<>();
        this.spendingEachDay = new HashMap<>();
        this.db.collection("expense")
                .whereEqualTo("userID", this.userID)
                .orderBy("Date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            Log.d(TAG, "onSuccess: LIST NOT EMPTY");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                processDocument(document);
                            }
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots) {
                                prepHashMap(setOfDaysWithExpenditure);
                                prepSpending(document);
                            }
                            listOfDaysWithExpenditure = new ArrayList<>(setOfDaysWithExpenditure);
                            Collections.sort(listOfDaysWithExpenditure);
                            dayExpenseAdapter = new ExpenseRecyclerViewAdapter(listOfDaysWithExpenditure, spendingEachDay,
                                    ActivityExpensePage.this, ActivityExpensePage.this);
                            expensesByDay.setAdapter(dayExpenseAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    }
                });
    }

    /**
     * Takes in a Firebase expense collection document and retrieves relevant data of each day
     * from the document.
     * Thereafter, keeps track of the CustomDays which have expenditure by storing in a HashSet.
     * @param document Firebase expense collection document
     */
    private void processDocument(QueryDocumentSnapshot document) {
        String date = Objects.requireNonNull(document.get("Date")).toString();
        try {
            Date d = dateFormatter.parse(date);
            assert d != null;
            CustomDay day = new CustomDay(d);
            this.setOfDaysWithExpenditure.add(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the HashMap which will later be passed into the layout adapter. Each key is a CustomDay,
     * and each value is the list of categories which have expenditure during that day.
     * @param daysWithExpenditure set of CustomDay with expenses
     */
    private void prepHashMap(Set<CustomDay> daysWithExpenditure) {
        for (CustomDay day : daysWithExpenditure) {
            if (!(this.spendingEachDay.containsKey(day))) {
                List<CustomExpenseCategory> customCategoryList = new ArrayList<>();
                this.spendingEachDay.put(day, customCategoryList);
            }
        }
    }

    /**
     * First, retrieves relevant data of each individual expense from the Firebase document.
     * Creates a CustomExpense object with data from the document, and add it to its category list
     * in the HashMap this.spendingEachDay, where the category list was created in method prepHashMap().
     *
     * If CustomExpense is the first object in the category, creates the required CustomExpenseCategory
     * and adds the CustomExpense to the category list.
     * @param document Firebase expense collection document
     */
    private void prepSpending(QueryDocumentSnapshot document) {
        String date = Objects.requireNonNull(document.get("Date")).toString();
        String category = Objects.requireNonNull(document.get("Category")).toString();
        String name = Objects.requireNonNull(document.get("Name")).toString();
        double amount = Double.parseDouble(Objects.requireNonNull(document.get("Amount")).toString());

        try {
            Date d = dateFormatter.parse(date);
            assert d != null;
            CustomDay day = new CustomDay(d);
            List<CustomExpenseCategory> listOfCustomExpenseCategories = this.spendingEachDay.get(day);
            boolean flag = false;
            assert listOfCustomExpenseCategories != null;
            for (CustomExpenseCategory cat : listOfCustomExpenseCategories) {
                if (cat.getName().equals(category)) {
                    cat.addExpense(new CustomExpense(document.getId(), name, amount));
                    flag = true;
                }
            }

            if (!(flag)) {
                CustomExpenseCategory newCat = new CustomExpenseCategory(category, new ArrayList<>());
                newCat.addExpense(new CustomExpense(document.getId(), name, amount));
                listOfCustomExpenseCategories.add(newCat);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Links to the ActivityEachDayExpenses, the page for expenses on the particular day clicked.
     * @param date day clicked
     */
    @Override
    public void onDateClickListener(String date) {
        Intent i = new Intent(this, ActivityEachDayExpenses.class);
        i.putExtra("date clicked", date);
        startActivity(i);
    }
}