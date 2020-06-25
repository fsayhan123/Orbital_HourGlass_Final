package com.example.weekcalendar.customclasses.event;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomEventFromGoogle extends CustomEvent implements Parcelable {
    private String eventID;

    public CustomEventFromGoogle(String title, String startDate, String endDate, String startTime, String endTime, String eventID) {
        super(title, startDate, endDate, startTime, endTime);
        this.eventID = eventID;
    }

    // Parcelable methods START

    // Parcelable constructor
    protected CustomEventFromGoogle(Parcel in) {
        super(in);
    }

    public static final Creator<CustomEventFromGoogle> CREATOR = new Creator<CustomEventFromGoogle>() {
        @Override
        public CustomEventFromGoogle createFromParcel(Parcel in) {
            return new CustomEventFromGoogle(in);
        }

        @Override
        public CustomEventFromGoogle[] newArray(int size) {
            return new CustomEventFromGoogle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable methods END

    // CustomEventFromFirebase methods START

    public void setId(String eventID) {
        this.eventID = eventID;
    }

    public String getId() { return this.eventID; }

    // CustomEventFromFirebase methods END
}
