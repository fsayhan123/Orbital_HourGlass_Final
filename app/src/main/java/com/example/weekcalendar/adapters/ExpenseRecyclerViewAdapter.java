package com.example.weekcalendar.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.helperclasses.MyOnDateClickListener;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomExpenseCategory;

import java.util.List;
import java.util.Map;

public class ExpenseRecyclerViewAdapter extends RecyclerView.Adapter<ExpenseRecyclerViewAdapter.MyExpenseViewHolder> {
    private List<CustomDay> spendingCustomDays;
    private Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay;
    private Activity a;
    private MyOnDateClickListener mDateClickListener;

    public ExpenseRecyclerViewAdapter(List<CustomDay> spendingCustomDays,
                                      Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay,
                                      Activity a,
                                      MyOnDateClickListener mDateClickListener) {
        // each CustomDay has a List of expense categories
        this.spendingCustomDays = spendingCustomDays;
        this.whatWeSpentEachDay = whatWeSpentEachDay;
        this.a = a;
        this.mDateClickListener = mDateClickListener;
    }

    public static class MyExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView costToday;
        private RecyclerView categoryAndExpenses;

        public MyExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.select_date);
            this.costToday = itemView.findViewById(R.id.total_cost_today);
            this.categoryAndExpenses = itemView.findViewById(R.id.category_and_expenses);
        }
    }

    @NonNull
    @Override
    public ExpenseRecyclerViewAdapter.MyExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_expense_page_each_day_expense, parent, false);
        ExpenseRecyclerViewAdapter.MyExpenseViewHolder holder = new MyExpenseViewHolder(view);
        holder.date.setOnClickListener(v -> {
            String day = holder.date.getText().toString();
            mDateClickListener.onDateClickListener(day);
        });
        return holder;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ExpenseRecyclerViewAdapter.MyExpenseViewHolder holder, int position) {
        CustomDay d = this.spendingCustomDays.get(position);
        holder.date.setText(d.getFullDateForView());

        List<CustomExpenseCategory> expenditureOnDayD = this.whatWeSpentEachDay.get(d);
        EachExpenseExRVAdapter e = new EachExpenseExRVAdapter(expenditureOnDayD);

        double totalCost = 0;
        for (int i = 0; i < expenditureOnDayD.size(); i++) {
            totalCost += expenditureOnDayD.get(i).getTotalCost();
        }

        holder.costToday.setText(String.format("%.2f", totalCost));

        LinearLayoutManager manager = new LinearLayoutManager(a);
        holder.categoryAndExpenses.setLayoutManager(manager);
        holder.categoryAndExpenses.setAdapter(e);
    }

    @Override
    public int getItemCount() {
        return this.spendingCustomDays == null ? 0 : this.spendingCustomDays.size();
    }
}