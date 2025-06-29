package com.audit.myexpense.model;

public class DayWiseExpense {
    public String date;
    public double expense;

    public DayWiseExpense(String date, double expense) {
        this.date = date;
        this.expense = expense;
    }
}