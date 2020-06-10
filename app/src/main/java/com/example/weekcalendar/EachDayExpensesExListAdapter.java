package com.example.weekcalendar;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

public class EachDayExpensesExListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<CustomExpenseCategory> listOfCat;

    public EachDayExpensesExListAdapter(Context context, List<CustomExpenseCategory> listOfCat) {
        this.context = context;
        this.listOfCat = listOfCat;
    }

    @Override
    public int getGroupCount() {
        return this.listOfCat.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        CustomExpenseCategory expenseCategory = this.listOfCat.get(groupPosition);
        return expenseCategory.getExpensesInCategory().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listOfCat.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        CustomExpenseCategory expenseCategory = this.listOfCat.get(groupPosition);
        return expenseCategory.getExpensesInCategory().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CustomExpenseCategory e = (CustomExpenseCategory) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expense_category_and_category_cost_2, null);
        }
        TextView categoryName = convertView.findViewById(R.id.category);
        categoryName.setTypeface(null, Typeface.BOLD);
        categoryName.setText(e.getName());
        TextView cost = convertView.findViewById(R.id.expense);
        cost.setTypeface(null, Typeface.BOLD);
        cost.setText(String.format("%.2f", (e.getTotalCost())));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final CustomExpense e = (CustomExpense) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expense_item_name_cost, null);
        }
//        convertView.setBackgroundColor(e.isSelected() ? Color.RED : Color.WHITE);
        TextView expense = convertView.findViewById(R.id.expense_name);
        TextView cost = convertView.findViewById(R.id.expense_cost);
        expense.setText(e.getExpenseName());
        cost.setText(String.format("%.2f", e.getCost()));

//        long packedPos = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
//        int pos = ((ExpandableListView) parent).getFlatListPosition(packedPos);
//        ((ExpandableListView) parent).setItemChecked(pos, true);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void remove(int groupPos, int childPos) {
        CustomExpenseCategory group = this.listOfCat.get(groupPos);
        group.removeExpense(childPos);
        if (this.getChildrenCount(groupPos) == 0) {
            this.listOfCat.remove(groupPos);
        }
        setNewItems(this.listOfCat);
    }

    public void setNewItems(List<CustomExpenseCategory> listOfCat) {
        this.listOfCat = listOfCat;
        notifyDataSetChanged();
    }
}
