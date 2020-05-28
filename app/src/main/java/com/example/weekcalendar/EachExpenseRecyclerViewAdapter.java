package com.example.weekcalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EachExpenseRecyclerViewAdapter extends RecyclerView.Adapter<EachExpenseRecyclerViewAdapter.MyEachExpenseViewHolder> {
    private List<ExpenseCategory> expenditureOnDayD;

    public EachExpenseRecyclerViewAdapter(List<ExpenseCategory> expenditureOnDayD) {
        this.expenditureOnDayD = expenditureOnDayD;
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
        ExpenseCategory category = this.expenditureOnDayD.get(position);
        holder.category.setText(category.getTitle());
        holder.expenseCost.setText(Double.toString(category.getTotalCost()));
    }

    @Override
    public int getItemCount() {
        return this.expenditureOnDayD == null ? 0 : this.expenditureOnDayD.size();
    }

    public class MyEachExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView category;
        private TextView expenseCost;

        public MyEachExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.category = itemView.findViewById(R.id.category);
            this.expenseCost = itemView.findViewById(R.id.expense);
        }
    }
}
