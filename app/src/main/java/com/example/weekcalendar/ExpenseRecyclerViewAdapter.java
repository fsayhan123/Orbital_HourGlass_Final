package com.example.weekcalendar;

import android.app.Activity;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseRecyclerViewAdapter extends RecyclerView.Adapter<ExpenseRecyclerViewAdapter.MyExpenseViewHolder> {
    private List<CustomDay> spendingCustomDays;
    private Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay;
    private Activity a;
    private LinearLayoutManager manager;
    private RecyclerView.RecycledViewPool viewPool;
    private Map<Integer, Parcelable> saveStates;

    public ExpenseRecyclerViewAdapter(List<CustomDay> spendingCustomDays, Map<CustomDay, List<CustomExpenseCategory>> whatWeSpentEachDay, Activity a) {
        // each CustomDay has a List of expense categories
        this.spendingCustomDays = spendingCustomDays;
        this.whatWeSpentEachDay = whatWeSpentEachDay;
        this.a = a;
        this.viewPool = new RecyclerView.RecycledViewPool();
        this.saveStates = new HashMap<>();
    }

    public class MyExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private RecyclerView categoryAndExpenses;
        private EachExpenseExRVAdapter e;

        public MyExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.date);
            this.categoryAndExpenses = itemView.findViewById(R.id.category_and_expenses);
        }

        public void setHolderAdapter(EachExpenseExRVAdapter e) {
            this.e = e;
        }

        public EachExpenseExRVAdapter getAdapter() {
            return this.e;
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
        Parcelable state = this.saveStates.get(position);

        holder.date.setText(d.getDate());

        List<CustomExpenseCategory> expenditureOnDayD = this.whatWeSpentEachDay.get(d);
        EachExpenseExRVAdapter e = new EachExpenseExRVAdapter(expenditureOnDayD);

        manager = new LinearLayoutManager(a);
        if (state != null) {
            manager.onRestoreInstanceState(state);
        }

        holder.categoryAndExpenses.setLayoutManager(manager);
        holder.categoryAndExpenses.setAdapter(e);
        holder.setHolderAdapter(e);

        holder.categoryAndExpenses.setRecycledViewPool(viewPool);
    }

    @Override
    public void onViewRecycled(@NonNull MyExpenseViewHolder holder) {
        super.onViewRecycled(holder);
//        this.saveStates.put(holder.getAdapterPosition(), holder.getAdapter().onSaveInstanceState());
    }

    @Override
    public int getItemCount() {
        return this.spendingCustomDays == null ? 0 : this.spendingCustomDays.size();
    }
}