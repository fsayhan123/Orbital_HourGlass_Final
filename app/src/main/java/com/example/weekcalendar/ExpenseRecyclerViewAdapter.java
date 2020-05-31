package com.example.weekcalendar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ExpenseRecyclerViewAdapter extends RecyclerView.Adapter<ExpenseRecyclerViewAdapter.MyExpenseViewHolder> {
    private List<CustomDay> spendingCustomDays;
    private Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay;
    private Activity a;
    private LinearLayoutManager manager;

    public ExpenseRecyclerViewAdapter(List<CustomDay> spendingCustomDays, Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay, Activity a) {
        // each CustomDay has a List of expense categories
        this.spendingCustomDays = spendingCustomDays;
        this.whatWeSpentEachDay = whatWeSpentEachDay;
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
    public ExpenseRecyclerViewAdapter.MyExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_day_expense, parent, false);
        ExpenseRecyclerViewAdapter.MyExpenseViewHolder holder = new ExpenseRecyclerViewAdapter.MyExpenseViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseRecyclerViewAdapter.MyExpenseViewHolder holder, int position) {
        CustomDay d = this.spendingCustomDays.get(position);
        holder.date.setText(d.getDate());

        List<CustomExpenseCategory> expenditureOnDayD = this.whatWeSpentEachDay.get(d);
        EachExpenseExRVAdapter e = new EachExpenseExRVAdapter(expenditureOnDayD);

        manager = new LinearLayoutManager(a);
        holder.categoryAndExpenses.setLayoutManager(manager);
        holder.categoryAndExpenses.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.spendingCustomDays == null ? 0 : this.spendingCustomDays.size();
    }
}
