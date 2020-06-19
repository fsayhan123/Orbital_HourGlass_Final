package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private List<CustomDay> daysWithExpenditure;
    private RecyclerView expensesByDay;
    private ExpenseRecyclerViewAdapter dayExpenseAdapter;
    private LinearLayoutManager manager;
    //private List<CustomExpenseCategory> customExpenseCategoriesList = new ArrayList<>();
    private Map<CustomDay, List<CustomExpenseCategory>> spendingEachDay = new HashMap<>();

    // FloatingActionButton to link to ActivityCreateExpense
    private FloatingActionButton floatingAddExpense;

    // Database handler
    private DatabaseHelper myDB;

    private SetupNavDrawer navDrawer;

    //Firebase fields
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);
        myDB = new DatabaseHelper(this);

        //Setup firebase fields
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        try {
            getSpendingDays();
            //daysWithExpenditure.sort((d1, d2) -> d1.compareTo(d2));
        } catch (ParseException e) {
            daysWithExpenditure = new ArrayList<>();
        }

        //spendingEachDay = getSpendingEachDay();

        expensesByDay = findViewById(R.id.day_by_day_expense_view);
        //dayExpenseAdapter = new ExpenseRecyclerViewAdapter(daysWithExpenditure, spendingEachDay,
        //        ActivityExpensePage.this, this);

        manager = new LinearLayoutManager(ActivityExpensePage.this);
        expensesByDay.setHasFixedSize(true);
        expensesByDay.setLayoutManager(manager);
        //expensesByDay.setAdapter(dayExpenseAdapter);

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
    /*private Map<CustomDay, List<CustomExpenseCategory>> getSpendingEachDay() {
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
                int id = Integer.parseInt(result.getString(0));
                String category = result.getString(2);
                String name = result.getString(4);
                String amount = result.getString(3);
                if (catHashMap.containsKey(category)) {
                    catHashMap.get(category).add(new CustomExpense(id, name, Double.parseDouble(amount)));
                } else {
                    List<CustomExpense> customExpenseCategory = new ArrayList<>();
                    customExpenseCategory.add(new CustomExpense(id, name, Double.parseDouble(amount)));
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
    }*/

    // Returns a List<CustomDay> that user has a spending;
    private void getSpendingDays() throws ParseException {
        daysWithExpenditure = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        db.collection("expense")
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
                            } for (QueryDocumentSnapshot document: queryDocumentSnapshots) {
                                System.out.println(daysWithExpenditure);
                                prepHashMap(daysWithExpenditure);
                                prepSpending(document);
                            }
                            System.out.println(spendingEachDay);
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
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = document.get("Date").toString();
        Date d = null;
        try {
            d = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CustomDay day = new CustomDay(d);
        if (!(daysWithExpenditure.contains(day))) {
            daysWithExpenditure.add(day);
        }
    }

    //Fill Hashmap with days
    private void prepHashMap(List<CustomDay> dayList) {
        for (CustomDay day:dayList) {
            if (!(spendingEachDay.containsKey(day))) {
                List<CustomExpenseCategory> customCategoryList = new ArrayList<>();
                spendingEachDay.put(day, customCategoryList);
            }
        }
    }

    //Prepare hashmap
    private void prepSpending(QueryDocumentSnapshot document) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
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
        List<CustomExpenseCategory> customExpenseCategoriesList = spendingEachDay.get(day);
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