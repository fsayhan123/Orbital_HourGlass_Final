package com.example.weekcalendar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomExpense;
import com.example.weekcalendar.customclasses.CustomExpenseCategory;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class EachExpenseExRVAdapter extends
        ExpandableRecyclerViewAdapter<EachExpenseExRVAdapter.ExpenseCategoryViewHolder, EachExpenseExRVAdapter.EachExpenseViewHolder> {
    public EachExpenseExRVAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public ExpenseCategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_expense_page_expense_category_and_category_cost, parent, false);
        ExpenseCategoryViewHolder holder = new ExpenseCategoryViewHolder(view);
        return holder;
    }

    @Override
    public EachExpenseViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_expense_page_individual_expense_and_cost, parent, false);
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
        private ImageView arrow;

        public ExpenseCategoryViewHolder(View itemView) {
            super(itemView);
            this.category = itemView.findViewById(R.id.category);
            this.expenseCost = itemView.findViewById(R.id.expense);
            this.arrow = itemView.findViewById(R.id.arrow);
        }

        public void setCategoryText(ExpandableGroup group) {
            category.setText(group.getTitle());
            expenseCost.setText(String.format("%.2f", ((CustomExpenseCategory) group).getTotalCost()));
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
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
            expenseCost.setText(String.format("%.2f", e.getCost()));
        }
    }
}