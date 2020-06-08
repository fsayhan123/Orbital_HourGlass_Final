package com.example.weekcalendar;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class CustomExpenseCategory extends ExpandableGroup<CustomExpense> {
    private String name;
    private List<CustomExpense> expensesInCategory;
    private double totalCost;

    public CustomExpenseCategory(String name, List<CustomExpense> expensesInCategory) { // to include the list and double later
        super(name, expensesInCategory);
        double cost = 0;
        for (CustomExpense customExpense : expensesInCategory) {
            cost += customExpense.getCost();
        }
        this.totalCost = cost;
    }

    public double getTotalCost() {
        return this.totalCost;
    }
}
