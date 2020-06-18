package com.example.weekcalendar;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomExpense implements Parcelable {
    private String expenseName;
    private double cost;
    private String ID;

    public CustomExpense(String ID, String expenseName, double cost) {
        this.ID = ID;
        this.expenseName = expenseName;
        this.cost = cost;
    }

    protected CustomExpense(Parcel in) {
        expenseName = in.readString();
        cost = in.readDouble();
    }

    public static final Creator<CustomExpense> CREATOR = new Creator<CustomExpense>() {
        @Override
        public CustomExpense createFromParcel(Parcel in) {
            return new CustomExpense(in);
        }

        @Override
        public CustomExpense[] newArray(int size) {
            return new CustomExpense[size];
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

    public String getID() {
        return this.ID;
    }
}
