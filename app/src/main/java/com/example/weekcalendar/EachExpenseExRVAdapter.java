package com.example.weekcalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class EachExpenseExRVAdapter extends ExpandableRecyclerViewAdapter<ExpenseCategoryViewHolder, EachExpenseViewHolder> {
    public EachExpenseExRVAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public ExpenseCategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_with_expense, parent, false);
        ExpenseCategoryViewHolder holder = new ExpenseCategoryViewHolder(view);
        return holder;
    }

    @Override
    public EachExpenseViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_expense, parent, false);
        EachExpenseViewHolder holder = new EachExpenseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindChildViewHolder(EachExpenseViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        Expense e = ((ExpenseCategory) group).getItems().get(childIndex);
        holder.setExpenseText(e);
    }

    @Override
    public void onBindGroupViewHolder(ExpenseCategoryViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setCategoryText(group);
    }
}
