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
    private List<Day> spendingDays;
    private Map<Day, List<ExpenseCategory>> whatWeSpentEachDay;
    private Activity a;

    public ExpenseRecylerViewAdapter(List<Day> spendingDays, Map<Day, List<ExpenseCategory>> whatWeSpentEachDay, Activity a) {
        // each Day has a List of expense categories
        this.spendingDays = spendingDays;
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
    public ExpenseRecylerViewAdapter.MyExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_day_expense, parent, false);
        ExpenseRecylerViewAdapter.MyExpenseViewHolder holder = new ExpenseRecylerViewAdapter.MyExpenseViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseRecylerViewAdapter.MyExpenseViewHolder holder, int position) {
        Day d = this.spendingDays.get(position);
        holder.date.setText(d.getDate());

        List<ExpenseCategory> expenditureOnDayD = this.whatWeSpentEachDay.get(d);
//        EachExpenseRecyclerViewAdapter e = new EachExpenseRecyclerViewAdapter(expenditureOnDayD);
        EachExpenseExRVAdapter e = new EachExpenseExRVAdapter(expenditureOnDayD);
        LinearLayoutManager LLM = new LinearLayoutManager(a);

        holder.categoryAndExpenses.setLayoutManager(LLM);
        holder.categoryAndExpenses.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.spendingDays == null ? 0 : this.spendingDays.size();
    }
}
