package com.example.weekcalendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Expense implements Parcelable {
    private String expenseName;
    private double cost;

    public Expense(String expenseName) {
        this.expenseName = expenseName;
    }

    protected Expense(Parcel in) {
        expenseName = in.readString();
        cost = in.readDouble();
    }

    public static final Creator<Expense> CREATOR = new Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel in) {
            return new Expense(in);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };

    public String getExpenseName() {
        return this.expenseName;
    }

    public double getCost() {
        return this.cost;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(expenseName);
        dest.writeDouble(cost);
    }
}
