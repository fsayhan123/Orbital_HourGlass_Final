package com.example.weekcalendar.customclasses;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.Objects;

public class CustomToDo implements Parcelable {
    private String ID;
    private String title;
    private String date;
    private boolean completed;

    public CustomToDo(String ID, String title, String date, boolean completed) {
        this.ID = ID;
        this.title = title;
        this.date = date;
        this.completed = completed;
    }

    // Parcelable methods START

    protected CustomToDo(Parcel in) {
        this.ID = in.readString();
        this.title = in.readString();
        this.date = in.readString();
        this.completed = in.readBoolean();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ID);
        dest.writeString(this.title);
        dest.writeString(this.date);
        dest.writeBoolean(this.completed);
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

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable methods END

    // CustomToDo methods START

    public String getID() {
        return this.ID;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDate() {
        return this.date;
    }

    public boolean getCompleted() { return this.completed; }

    public void toggleComplete() {
        this.completed = !this.completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomToDo that = (CustomToDo) o;
        return getCompleted() == that.getCompleted() &&
                getID().equals(that.getID()) &&
                getTitle().equals(that.getTitle()) &&
                getDate().equals(that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getID(), getTitle(), getDate(), getCompleted());
    }

    // CustomToDo methods END
}
