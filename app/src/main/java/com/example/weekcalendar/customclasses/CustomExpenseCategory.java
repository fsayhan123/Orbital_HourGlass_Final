package com.example.weekcalendar.customclasses;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class CustomExpenseCategory extends ExpandableGroup<CustomExpense> {
    private String name;
    private List<CustomExpense> expensesInCategory;
    private double totalCost;

    public CustomExpenseCategory(String name, List<CustomExpense> expensesInCategory) { // to include the list and double later
        super(name, expensesInCategory);
        this.name = name;
        this.expensesInCategory = expensesInCategory;
        double cost = 0;
        for (CustomExpense customExpense : expensesInCategory) {
            cost += customExpense.getCost();
        }
        this.totalCost = cost;
    }

    public void addExpense(CustomExpense e) {
        this.expensesInCategory.add(e);
        this.totalCost += e.getCost();
    }

    public List<CustomExpense> getExpensesInCategory() {
        return this.expensesInCategory;
    }

    public void removeExpense(int pos) {
        this.totalCost -= this.expensesInCategory.get(pos).getCost();
        this.expensesInCategory.remove(pos);
    }

    public double getTotalCost() {
        return this.totalCost;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other instanceof CustomExpenseCategory) {
            CustomExpenseCategory d2 = (CustomExpenseCategory) other;
            return this.name.equals(d2.name);
        } else if (other instanceof String) {
            String s = (String) other;
            return this.name.equals(s);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String getName() {
        return this.name;
    }
}
