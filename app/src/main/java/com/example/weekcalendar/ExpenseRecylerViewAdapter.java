package com.example.weekcalendar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class ExpenseRecylerViewAdapter extends RecyclerView.Adapter<ExpenseRecylerViewAdapter.MyExpenseViewHolder> {
    private Map<Day, Map<Integer, SortedMap<String, Double>>> dayCategoryExpense;
    private List<Day> spendingDays;
    private Activity a;

    public ExpenseRecylerViewAdapter(Map<Day, Map<Integer, SortedMap<String, Double>>>  dayCategoryExpense, List<Day> spendingDays, Activity a) {
        this.dayCategoryExpense = dayCategoryExpense;
        this.spendingDays = spendingDays;
        this.a = a;
    }

    public class MyExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private RecyclerView categoryAndExpenses;

        public MyExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.date);
            this.categoryAndExpenses = itemView.findViewById(R.id.category_and_expenses);
        }
    }

    @NonNull
    @Override
    public ExpenseRecylerViewAdapter.MyExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_day_expense, parent, false);
        ExpenseRecylerViewAdapter.MyExpenseViewHolder holder = new ExpenseRecylerViewAdapter.MyExpenseViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseRecylerViewAdapter.MyExpenseViewHolder holder, int position) {
        Day d = this.spendingDays.get(position);
        Map<Integer, SortedMap<String, Double>> expenses = this.dayCategoryExpense.get(d);
        holder.date.setText(d.getDate());
        EachExpenseRecyclerViewAdapter e = new EachExpenseRecyclerViewAdapter(expenses);
    }

    @Override
    public int getItemCount() {
        return this.spendingDays == null ? 0 : this.spendingDays.size();
    }
}
