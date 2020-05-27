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

public class ExpenseRecylerViewAdapter extends RecyclerView.Adapter<ExpenseRecylerViewAdapter.MyExpenseViewHolder> {
    private Map<Day, Map<String, Double>> dayCategoryExpense;
    private List<Day> spendingDays;
    private Activity a;

    public ExpenseRecylerViewAdapter(Map<Day, Map<String, Double>> dayCategoryExpense, List<Day> spendingDays, Activity a) {
        this.dayCategoryExpense = dayCategoryExpense;
        this.spendingDays = spendingDays;
        this.a = a;
    }

    public class MyExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        public MyExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
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
        Map<String, Double> expenses = this.dayCategoryExpense.get(d);
        holder.date.setText(d.getDate());

        LinearLayoutManager LLM = new LinearLayoutManager(a);
    }

    @Override
    public int getItemCount() {
        return this.spendingDays == null ? 0 : this.spendingDays.size();
    }
}
