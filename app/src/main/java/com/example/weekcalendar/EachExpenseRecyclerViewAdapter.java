package com.example.weekcalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;
import java.util.SortedMap;

public class EachExpenseRecyclerViewAdapter extends RecyclerView.Adapter<EachExpenseRecyclerViewAdapter.MyEachExpenseViewHolder> {
    private final Map<Integer, SortedMap<String, Double>> expenses;

    public EachExpenseRecyclerViewAdapter(Map<Integer, SortedMap<String, Double>> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public MyEachExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_with_expense, parent, false);
        EachExpenseRecyclerViewAdapter.MyEachExpenseViewHolder holder = new EachExpenseRecyclerViewAdapter.MyEachExpenseViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyEachExpenseViewHolder holder, int position) {
        SortedMap<String, Double> catAndEx = this.expenses.get(position);
        for (String category : catAndEx.keySet()) {
            holder.category.setText(category);
            holder.expense.setText(Double.toString(catAndEx.get(category)));
        }
    }

    @Override
    public int getItemCount() {
        return this.expenses == null ? 0 : this.expenses.keySet().size();
    }

    public class MyEachExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView category;
        private TextView expense;

        public MyEachExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.category = itemView.findViewById(R.id.category);
            this.expense = itemView.findViewById(R.id.expense);
        }
    }
}
