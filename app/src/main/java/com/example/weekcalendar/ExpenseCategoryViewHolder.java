package com.example.weekcalendar;

import android.view.View;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ExpenseCategoryViewHolder extends GroupViewHolder {
    private TextView category;
    private TextView expenseCost;

    public ExpenseCategoryViewHolder(View itemView) {
        super(itemView);
        this.category = itemView.findViewById(R.id.category);
        this.expenseCost = itemView.findViewById(R.id.expense);
    }

    public void setCategoryText(ExpandableGroup group) {
        category.setText(group.getTitle());
        expenseCost.setText(Double.toString(((CustomExpenseCategory) group).getTotalCost()));
    }
}