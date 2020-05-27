package com.example.weekcalendar;

import java.util.List;

public class ExpenseCategory {
    private String name;
    private List<Expense> expensesInCategory;
    private double totalCost;

    public ExpenseCategory(String name) { // to include the list and double later
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<Expense> getExpensesInCategory() {
        return this.expensesInCategory;
    }

    public double getTotalCost() {
        return this.totalCost;
    }
}
