package com.example.weekcalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

public class EachExpenseExRVAdapter extends
        ExpandableRecyclerViewAdapter<EachExpenseExRVAdapter.ExpenseCategoryViewHolder, EachExpenseExRVAdapter.EachExpenseViewHolder> {
    public EachExpenseExRVAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public ExpenseCategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_category_and_category_cost, parent, false);
        ExpenseCategoryViewHolder holder = new ExpenseCategoryViewHolder(view);
        return holder;
    }

    @Override
    public EachExpenseViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_expense_and_cost, parent, false);
        EachExpenseViewHolder holder = new EachExpenseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindChildViewHolder(EachExpenseViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        CustomExpense e = ((CustomExpenseCategory) group).getItems().get(childIndex);
        holder.setExpenseText(e);
    }

    @Override
    public void onBindGroupViewHolder(ExpenseCategoryViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setCategoryText(group);
    }

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

    public class EachExpenseViewHolder extends ChildViewHolder {
        private TextView expenseName;
        private TextView expenseCost;

        public EachExpenseViewHolder(View itemView) {
            super(itemView);
            expenseName = itemView.findViewById(R.id.expense_name);
            expenseCost = itemView.findViewById(R.id.expense_cost);
        }

        public void setExpenseText(CustomExpense e) {
            expenseName.setText(e.getExpenseName());
            expenseCost.setText(Double.toString(e.getCost()));
        }
    }
}
