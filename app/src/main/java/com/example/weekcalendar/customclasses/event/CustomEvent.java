package com.example.weekcalendar.customclasses.event;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class CustomEvent implements Parcelable {
    protected String title;
    protected String startDate;
    protected String endDate;
    protected String startTime;
    protected String endTime;
    protected String description;
    protected String ID;

    public CustomEvent(String title, String startDate, String endDate, String startTime, String endTime, String ID) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ID = ID;
    }

    // Parcelable methods START

    protected CustomEvent(Parcel in) {
        this.title = in.readString();
        this.startDate = in.readString();
        this.endDate = in.readString();
        this.startTime = in.readString();
        this.endTime = in.readString();
        this.ID = in.readString();
        this.description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(ID);
        if (this.description != null) {
            dest.writeString(this.description);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable methods END

    // CustomEvent methods START

    public String getStartDate() {
        return this.startDate;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public String getTitle() {
        return this.title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.ID;
    }

    // CustomEvent methods END
}
