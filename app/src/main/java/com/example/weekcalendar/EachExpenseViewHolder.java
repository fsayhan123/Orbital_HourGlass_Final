package com.example.weekcalendar;

import android.view.View;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class EachExpenseViewHolder extends ChildViewHolder {
    private TextView expenseName;
    private TextView expenseCost;

    public EachExpenseViewHolder(View itemView) {
        super(itemView);
        expenseName = itemView.findViewById(R.id.expense_name);
        expenseCost = itemView.findViewById(R.id.expense_cost);
    }

    public void setExpenseText(Expense e) {
        expenseName.setText(e.getExpenseName());
        expenseCost.setText(Double.toString(e.getCost()));
    }
}
