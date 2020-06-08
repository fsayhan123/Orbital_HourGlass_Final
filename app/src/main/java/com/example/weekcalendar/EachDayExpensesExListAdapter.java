package com.example.weekcalendar;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class EachDayExpensesExListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listOfCat;
    private Map<String, List<CustomExpense>> expensesInEachCat;

    public EachDayExpensesExListAdapter(Context context, List<String> listOfCat,
                                        Map<String, List<CustomExpense>> expensesInEachCat) {
        this.context = context;
        this.listOfCat = listOfCat;
        this.expensesInEachCat = expensesInEachCat;
    }

    @Override
    public int getGroupCount() {
        return this.listOfCat.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String cat = this.listOfCat.get(groupPosition);
        return this.expensesInEachCat.get(cat).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listOfCat.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String cat = this.listOfCat.get(groupPosition);
        List<CustomExpense> list = this.expensesInEachCat.get(cat);
        return list.get(childPosition);
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
        String listTitle = getGroup(groupPosition).toString();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.list_parent);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final CustomExpense e = (CustomExpense) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.individual_expense_and_cost, null);
        }
        TextView expense = convertView.findViewById(R.id.expense_name);
        TextView cost = convertView.findViewById(R.id.expense_cost);
        expense.setText(e.getExpenseName());
        cost.setText(Double.toString(e.getCost()));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
