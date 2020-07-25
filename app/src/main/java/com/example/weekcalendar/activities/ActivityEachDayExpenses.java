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
import java.util.Objects;

public class ActivityEachDayExpenses extends AppCompatActivity {
    /**
     * For logging purposes. To easily identify output or logs relevant to current page.
     */
    private static final String TAG = ActivityEachDayExpenses.class.getSimpleName();

    private List<CustomExpenseCategory> listOfCat = new ArrayList<>();
    private boolean canDelete = false;
    private String day;

    private ExpandableListView allExpenseCategories;
    private EachDayExpensesExListAdapter adapter;
    private List<int[]> listOfPos = new ArrayList<>();

    /**
     * Firebase information
     */
    private FirebaseFirestore db;
    private String userID;

    /**
     * Sets up ActivityEachDayExpenses when opened.
     * Then, fetches relevant data from Firebase expense collection for this day.
     * @param savedInstanceState saved state of current page, if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_day_expense_categories);

        this.db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        this.userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        Intent intent = getIntent();
        this.day = intent.getStringExtra("date clicked");

        setupXMLItems();

        populateCategories(HelperMethods.formatDateWithDash(this.day));
    }

    /**
     * Sets up layout for ActivityEachDayExpenses.
     */
    private void setupXMLItems() {
        Toolbar tb = findViewById(R.id.select_date);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Expenses on " + this.day);
        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ActivityEachDayExpenses.this, ActivityExpensePage.class));
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.allExpenseCategories = findViewById(R.id.all_expense_categories);
    }

    /**
     * Queries data from Firebase expense collection for expenses on the current day selected.
     * Sets up handlers when respective items are clicked for the different scenarios e.g.
     * when delete function is toggled.
     * @param date current day
     */
    public void populateCategories(String date) {
        this.db.collection("expense")
                .whereEqualTo("userID", this.userID)
                .whereEqualTo("Date", date)
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

                            adapter = new EachDayExpensesExListAdapter(ActivityEachDayExpenses.this, ActivityEachDayExpenses.this.listOfCat);
                            allExpenseCategories.setAdapter(adapter);

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
                        Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    }
                });
    }

    /**
     * Takes in a Firebase expense collection document and retrieves relevant data of each day
     * from the document.
     * Adds CustomExpense to its respective CustomExpenseCategory if it exists.
     * If CustomExpense is the first object in the category, creates the required CustomExpenseCategory
     * and adds the CustomExpense to the category list.
     * @param document Firebase expense collection document
     */
    public void processDocument(QueryDocumentSnapshot document) {
        String category = Objects.requireNonNull(document.get("Category")).toString();
        String name = Objects.requireNonNull(document.get("Name")).toString();
        double amount = Double.parseDouble(Objects.requireNonNull(document.get("Amount")).toString());
        boolean flag = false;
        for (CustomExpenseCategory customCat : this.listOfCat) {
            if (customCat.getName().equals(category)) {
                customCat.addExpense(new CustomExpense(document.getId(), name, amount));
                flag = true;
            }
        }

        if (!(flag)) {
            CustomExpenseCategory newCat = new CustomExpenseCategory(category, new ArrayList<>());
            newCat.addExpense(new CustomExpense(document.getId(), name, amount));
            this.listOfCat.add(newCat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    /**
     * Toggles ability to delete.
     * @param item item in menu that is clicked by user
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {

            if (this.canDelete) {
                deleteSelectedItems();
                item.setIcon(R.drawable.ic_baseline_delete_24_transparent);
                this.canDelete = false;
                Toast.makeText(this, "Deleted.", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Toggle delete off.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Toggle delete on.", Toast.LENGTH_SHORT).show();
                this.canDelete = true;
                item.setIcon(R.drawable.ic_baseline_delete_24);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes selected items. When all selected items are deleted from Firebase expense collection,
     * clears the list of selected items and updates the adapter to refresh the data displayed on
     * the page.
     * If all items from this day are deleted, returns to ActivityExpensePage.
     */
    private void deleteSelectedItems() {
        if (this.listOfPos.size() > 0) {
            for (int i = this.listOfPos.size() - 1; i >= 0; i--) {
                int[] pair = this.listOfPos.get(i);
                CustomExpense e = (CustomExpense) (this.adapter.getChild(pair[0], pair[1]));
                this.db.collection("expense").document(e.getID()).delete();
                this.adapter.remove(pair[0], pair[1]);
            }
            this.listOfPos.clear();
            this.allExpenseCategories.clearChoices();
            if (this.adapter.getGroupCount() == 0) {
                startActivity(new Intent(ActivityEachDayExpenses.this, ActivityExpensePage.class));
            }
            for (int i = 0; i < this.adapter.getGroupCount(); i++) {
                this.adapter.getGroupView(i, true, null, null);
            }
        }
    }
}