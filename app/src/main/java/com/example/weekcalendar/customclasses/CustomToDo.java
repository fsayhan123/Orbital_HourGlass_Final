package com.example.weekcalendar.customclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomToDo implements Parcelable {
    private String ID;
    private String title;
    private String date;

    public CustomToDo(String ID, String title, String date) {
        this.ID = ID;
        this.title = title;
        this.date = date;
    }

    protected CustomToDo(Parcel in) {
        this.ID = in.readString();
        this.title = in.readString();
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

    public String getTitle() {
        return this.title;
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
        dest.writeString(this.title);
        dest.writeString(this.date);
    }
}
