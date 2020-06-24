package com.example.weekcalendar.customclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomExpense implements Parcelable {
    private String ID;
    private String expenseName;
    private double cost;

    public CustomExpense(String ID, String expenseName, double cost) {
        this.ID = ID;
        this.expenseName = expenseName;
        this.cost = cost;
    }

    protected CustomExpense(Parcel in) {
        this.ID = in.readString();
        this.expenseName = in.readString();
        this.cost = in.readDouble();
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
        dest.writeString(this.ID);
        dest.writeString(this.expenseName);
        dest.writeDouble(this.cost);
    }

    public String getID() {
        return this.ID;
    }
}
