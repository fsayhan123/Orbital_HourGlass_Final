package com.example.weekcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ActivityEachDayExpenses extends AppCompatActivity {
    private DatabaseHelper myDB;
    private List<String> listOfCat;
    private Map<String, List<CustomExpense>> expensesInEachCat;
    private EachDayExpensesAdapter adapter;
    private LinearLayoutManager manager;
    private RecyclerView allExpenseCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_day_categories);

        Intent intent = getIntent();
        String expenseDate = intent.getStringExtra("date clicked");

        Toolbar tb = findViewById(R.id.date_header);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(expenseDate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myDB = new DatabaseHelper(this);

        String parsedDate = parseDate(expenseDate);
        Cursor results = myDB.getExpenseData(parsedDate);
        populateCategories(results);

        allExpenseCategories = findViewById(R.id.all_expense_categories);

        adapter = new EachDayExpensesAdapter(this.listOfCat, this.expensesInEachCat, this);
        manager = new LinearLayoutManager(this);
        allExpenseCategories.setHasFixedSize(true);
        allExpenseCategories.setLayoutManager(manager);
        allExpenseCategories.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(allExpenseCategories);
    }

    String deleted = null;
    String edit = null;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            switch (direction) {
                case ItemTouchHelper.LEFT:
                    deleted = listOfCat.get(position);
                    listOfCat.remove(position);
                    adapter.notifyDataSetChanged();
                    Snackbar.make(allExpenseCategories, deleted, Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                listOfCat.add(position, deleted);
                                adapter.notifyItemInserted(position);
                            })
                    .show();
                    break;
                case ItemTouchHelper.RIGHT:
                    edit = listOfCat.get(position);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(R.color.Pink)
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeRightBackgroundColor(R.color.green)
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public String parseDate(String date) {
        String[] expenseDateArr = date.split(" ");
        expenseDateArr[1] = myDB.convertDate(expenseDateArr[1].substring(0,3));
        if (expenseDateArr[0].length() == 1) {
            expenseDateArr[0] = "0" + expenseDateArr[0];
        }
        return String.join("-", expenseDateArr[2], expenseDateArr[1], expenseDateArr[0]);
    }

    public void populateCategories(Cursor results) {
        expensesInEachCat = new HashMap<>();
        listOfCat = new ArrayList<>();
        for (int i = 0; i < results.getCount(); i++) {
            results.moveToNext();
            String category = results.getString(2);
            String name = results.getString(4);
            String amount = results.getString(3);
            if (expensesInEachCat.get(category) != null) {
                expensesInEachCat.get(category).add(new CustomExpense(name, Double.valueOf(amount)));
            } else {
                List<CustomExpense> temp = new ArrayList<>();
                temp.add(new CustomExpense(name, Double.valueOf(amount)));
                expensesInEachCat.put(category, temp);
                listOfCat.add(category);
            }
        }
    }
}