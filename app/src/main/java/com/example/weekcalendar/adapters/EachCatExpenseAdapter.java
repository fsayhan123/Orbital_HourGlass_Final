package com.example.weekcalendar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekcalendar.helperclasses.ClickPosition;
import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomExpense;

import java.util.List;

public class EachCatExpenseAdapter extends RecyclerView.Adapter<EachCatExpenseAdapter.MyCatExpenseViewHolder> {
    private List<CustomExpense> listOfExpenses;
    private ClickPosition clickPosition;

    public EachCatExpenseAdapter(List<CustomExpense> listOfExpenses, ClickPosition clickPosition) {
        this.listOfExpenses = listOfExpenses;
        this.clickPosition = clickPosition;
    }

    @NonNull
    @Override
    public EachCatExpenseAdapter.MyCatExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_expense_page_individual_expense_and_cost, parent, false);
        EachCatExpenseAdapter.MyCatExpenseViewHolder holder = new EachCatExpenseAdapter.MyCatExpenseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EachCatExpenseAdapter.MyCatExpenseViewHolder holder, int position) {
        CustomExpense e = this.listOfExpenses.get(position);
        holder.expense.setText(e.getExpenseName());
        holder.cost.setText(Double.toString(e.getCost()));
        holder.linearLayout.setOnClickListener(v -> {
            clickPosition.getPosition(position);
        });
    }

    @Override
    public int getItemCount() {
        return this.listOfExpenses == null ? 0 : this.listOfExpenses.size();
    }

    public class MyCatExpenseViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView expense;
        TextView cost;

        public MyCatExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expense = itemView.findViewById(R.id.expense_name);
            cost = itemView.findViewById(R.id.expense_cost);
            linearLayout = itemView.findViewById(R.id.layout);
        }
    }
}
