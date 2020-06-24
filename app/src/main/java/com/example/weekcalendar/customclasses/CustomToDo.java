package com.example.weekcalendar.customclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomToDo implements Parcelable {
    private String ID;
    private String details;
    private String date;

    public CustomToDo(String ID, String details, String date) {
        this.ID = ID;
        this.details = details;
        this.date = date;
    }

    protected CustomToDo(Parcel in) {
        this.ID = in.readString();
        this.details = in.readString();
        this.date = in.readString();
    }

    public static final Creator<CustomToDo> CREATOR = new Creator<CustomToDo>() {
        @Override
        public CustomToDo createFromParcel(Parcel in) {
            return new CustomToDo(in);
        }

        @Override
        public CustomToDo[] newArray(int size) {
            return new CustomToDo[size];
        }
    };

    public String getID() {
        return this.ID;
    }

    public String getDetails() {
        return this.details;
    }

    public String getDate() {
        return this.date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ID);
        dest.writeString(this.details);
        dest.writeString(this.date);
    }
}
