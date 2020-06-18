package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityEachDayExpenses extends AppCompatActivity {
    private DatabaseHelper myDB;
    private List<CustomExpenseCategory> listOfCat;
    private boolean canDelete = false;
//    private EachDayExpensesAdapter adapter;
//    private LinearLayoutManager manager;
//    private RecyclerView allExpenseCategories;

    private ExpandableListView allExpenseCategories;
    private EachDayExpensesExListAdapter adapter;
    private List<int[]> listOfPos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_day_categories);

        Intent intent = getIntent();
        String expenseDate = intent.getStringExtra("date clicked");

        Toolbar tb = findViewById(R.id.select_date);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Expenses on " + expenseDate);
        // sets up back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tb.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ActivityEachDayExpenses.this, ActivityExpensePage.class));
        });

        myDB = new DatabaseHelper(this);

        String parsedDate = parseDate(expenseDate);
        Cursor results = myDB.getDayExpenseData(parsedDate);
        populateCategories(results);

        allExpenseCategories = findViewById(R.id.all_expense_categories);

//        adapter = new EachDayExpensesAdapter(this.listOfCat, this.expensesInEachCat, this);
//        manager = new LinearLayoutManager(this);
//        allExpenseCategories.setHasFixedSize(true);
//        allExpenseCategories.setLayoutManager(manager);
//        allExpenseCategories.setAdapter(adapter);

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
//        itemTouchHelper.attachToRecyclerView(allExpenseCategories);

        adapter = new EachDayExpensesExListAdapter(this, this.listOfCat);
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
                        myDB.deleteExpense(e.getID());
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

    //    String deleted = null;
//    String edit = null;
//
//    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//        @Override
//        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//            return false;
//        }
//
//        @Override
//        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//            int position = viewHolder.getAdapterPosition();
//
//            switch (direction) {
//                case ItemTouchHelper.LEFT:
//                    deleted = listOfCat.get(position);
//                    listOfCat.remove(position);
//                    adapter.notifyDataSetChanged();
//                    Snackbar.make(allExpenseCategories, deleted, Snackbar.LENGTH_LONG)
//                            .setAction("Undo", v -> {
//                                listOfCat.add(position, deleted);
//                                adapter.notifyItemInserted(position);
//                            })
//                    .show();
//                    break;
//                case ItemTouchHelper.RIGHT:
//                    edit = listOfCat.get(position);
//                    adapter.notifyDataSetChanged();
//                    break;
//            }
//        }
//
//        @Override
//        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                    .addSwipeLeftBackgroundColor(R.color.Pink)
//                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24_transparent)
//                    .addSwipeRightBackgroundColor(R.color.green)
//                    .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
//                    .create()
//                    .decorate();
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        }
//    };

    public String parseDate(String date) {
        String[] expenseDateArr = date.split(" ");
        expenseDateArr[1] = myDB.convertDate(expenseDateArr[1].substring(0,3));
        if (expenseDateArr[0].length() == 1) {
            expenseDateArr[0] = "0" + expenseDateArr[0];
        }
        return String.join("-", expenseDateArr[2], expenseDateArr[1], expenseDateArr[0]);
    }

    public void populateCategories(Cursor results) {
        Map<String, List<CustomExpense>> expensesInEachCat = new HashMap<>();
        listOfCat = new ArrayList<>();
        for (int i = 0; i < results.getCount(); i++) {
            results.moveToNext();
            int id = Integer.parseInt(results.getString(0));
            String category = results.getString(2);
            String name = results.getString(4);
            String amount = results.getString(3);
            if (expensesInEachCat.get(category) != null) {
                expensesInEachCat.get(category).add(new CustomExpense(id, name, Double.parseDouble(amount)));
            } else {
                List<CustomExpense> temp = new ArrayList<>();
                temp.add(new CustomExpense(id, name, Double.parseDouble(amount)));
                expensesInEachCat.put(category, temp);
            }
        }
        for (String s : expensesInEachCat.keySet()) {
            assert expensesInEachCat.get(s) != null;
            CustomExpenseCategory e = new CustomExpenseCategory(s, expensesInEachCat.get(s));
            listOfCat.add(e);
        }
    }
}