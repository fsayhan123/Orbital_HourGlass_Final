package com.example.weekcalendar;

/*
 to store data relevant to each event
 */

import android.os.Parcel;
import android.os.Parcelable;

public class CustomEvent implements Parcelable {
    private String docID;
    private String title;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String description;

    public CustomEvent(String title, String startDate, String endDate, String startTime, String endTime, String docID) {
        this.docID = docID;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.docID);
        dest.writeString(this.title);
        dest.writeString(this.startDate);
        dest.writeString(this.endDate);
        dest.writeString(this.startTime);
        dest.writeString(this.endTime);
        if (this.description != null) {
            dest.writeString(this.description);
        }
    }

    public static final Parcelable.Creator<CustomEvent> CREATOR = new Parcelable.Creator<CustomEvent>() {
        public CustomEvent createFromParcel(Parcel in) {
            return new CustomEvent(in);
        }

        public CustomEvent[] newArray(int size) {
            return new CustomEvent[size];
        }
    };

    private CustomEvent(Parcel in) {
        this.docID = in.readString();
        this.title = in.readString();
        this.startDate = in.readString();
        this.endDate = in.readString();
        this.startTime = in.readString();
        this.endTime = in.readString();
        this.description = in.readString();
    }

    public String getId() { return this.docID; }

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

    @Override
    public int describeContents() {
        return 0;
    }


}
