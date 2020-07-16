package com.example.weekcalendar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.weekcalendar.helperclasses.DatabaseHelper;
import com.example.weekcalendar.adapters.EachDayExpensesExListAdapter;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomExpense;
import com.example.weekcalendar.customclasses.CustomExpenseCategory;
import com.example.weekcalendar.helperclasses.HelperMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityEachDayExpenses extends AppCompatActivity {
    private List<CustomExpenseCategory> listOfCat = new ArrayList<>();
    private boolean canDelete = false;

    private ExpandableListView allExpenseCategories;
    private EachDayExpensesExListAdapter adapter;
    private List<int[]> listOfPos = new ArrayList<>();

    //Firebase fields
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_day_expense_categories);

        Intent intent = getIntent();
        String expenseDate = intent.getStringExtra("date clicked");

        //sets up toolbar
        Toolbar tb = findViewById(R.id.select_date);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Expenses on " + expenseDate);

        // sets up back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Setup firebase fields
        this.db = FirebaseFirestore.getInstance();
        this.fAuth = FirebaseAuth.getInstance();
        this.userID = this.fAuth.getCurrentUser().getUid();

        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ActivityEachDayExpenses.this, ActivityExpensePage.class));
        });

        //Get a particular date as given from the top
        populateCategories(HelperMethods.formatDateWithDash(expenseDate));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            Toast.makeText(this, "toggle delete", Toast.LENGTH_SHORT).show();
            if (canDelete) {
                if (listOfPos.size() > 0) {
                    for (int i = listOfPos.size() - 1; i >= 0; i--) {
                        int[] pair = listOfPos.get(i);
                        Toast.makeText(this, "deleting " + Arrays.toString(pair), Toast.LENGTH_SHORT).show();
                        CustomExpense e = (CustomExpense) (adapter.getChild(pair[0], pair[1]));
                        db.collection("expense").document(e.getID()).delete();
                        adapter.remove(pair[0], pair[1]);
                    }
                    listOfPos.clear();
                    allExpenseCategories.clearChoices();
                    if (adapter.getGroupCount() == 0) {
                        startActivity(new Intent(ActivityEachDayExpenses.this, ActivityExpensePage.class));
                    }
                    for (int i = 0; i < adapter.getGroupCount(); i++) {
                        adapter.getGroupView(i, true, null, null);
                    }
                }
                item.setIcon(R.drawable.ic_baseline_delete_24_transparent);
                canDelete = false;
            } else {
                canDelete = true;
                item.setIcon(R.drawable.ic_baseline_delete_24);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void populateCategories(String date) {
        this.db.collection("expense")
                .whereEqualTo("userID", this.userID)
                .whereEqualTo("Date", date)
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
                            allExpenseCategories = findViewById(R.id.all_expense_categories);
                            adapter = new EachDayExpensesExListAdapter(ActivityEachDayExpenses.this, ActivityEachDayExpenses.this.listOfCat);
                            allExpenseCategories.setAdapter(adapter);
//                            allExpenseCategories.setDivider(R.color.transparent);
                            for (int i = 0; i < adapter.getGroupCount(); i++) {
                                allExpenseCategories.expandGroup(i);
                            }

                            allExpenseCategories.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                @Override
                                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                    if (canDelete) {
                                        long packedPos = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
                                        int pos = parent.getFlatListPosition(packedPos);
                                        boolean isChecked = parent.isItemChecked(pos);
                                        if (isChecked) {
                                            parent.setItemChecked(pos, false);
                                            for (int i = 0; i < listOfPos.size(); i++) {
                                                if (listOfPos.get(i)[0] == groupPosition && listOfPos.get(i)[1] == childPosition) {
                                                    listOfPos.remove(i);
                                                    break;
                                                }
                                            }
                                        } else {
                                            parent.setItemChecked(pos, true);
                                            listOfPos.add(new int[] {groupPosition, childPosition});
                                        }
                                    } else {
                                        Intent i = new Intent(ActivityEachDayExpenses.this, ActivityCreateExpensePage.class);
                                        CustomExpense e = (CustomExpense) adapter.getChild(groupPosition, childPosition);
                                        i.putExtra("expense ID", e.getID());
                                        startActivity(i);
                                    }
                                    return false;
                                }
                            });
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

    public void processDocument(QueryDocumentSnapshot document) {
        //String date = document.get("Date").toString();
        String category = document.get("Category").toString();
        String name = document.get("Name").toString();
        double amount = new Double(document.get("Amount").toString());
        boolean flag = false;
        for (CustomExpenseCategory customCat : this.listOfCat) {
            if (customCat.getName().equals(category)) {
                customCat.addExpense(new CustomExpense(document.getId(), name, amount));
                flag = true;
            }
        }
        //No cat is existing inside thelist
        if (!(flag)) {
            CustomExpenseCategory newCat = new CustomExpenseCategory(category, new ArrayList<>());
            newCat.addExpense(new CustomExpense(document.getId(), name, amount));
            this.listOfCat.add(newCat);
        }
    }
}