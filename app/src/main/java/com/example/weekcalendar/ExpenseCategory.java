package com.example.weekcalendar;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ExpenseCategory extends ExpandableGroup<Expense> {
    private String name;
    private List<Expense> expensesInCategory;
    private double totalCost;

    public ExpenseCategory(String name, List<Expense> expensesInCategory) { // to include the list and double later
        super(name, expensesInCategory);
    }

    public double getTotalCost() {
        return this.totalCost;
    }
}
