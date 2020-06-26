package com.example.weekcalendar.customclasses.event;

/*
 to store data relevant to each event
 */

import android.os.Parcel;
import android.os.Parcelable;

public class CustomEventFromFirebase extends CustomEvent implements Parcelable {

    public CustomEventFromFirebase(String title, String startDate, String endDate, String startTime, String endTime, String docID) {
        super(title, startDate, endDate, startTime, endTime, docID);
    }

    // Parcelable methods START

    // Parcelable constructor
    private CustomEventFromFirebase(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<CustomEventFromFirebase> CREATOR = new Parcelable.Creator<CustomEventFromFirebase>() {
        public CustomEventFromFirebase createFromParcel(Parcel in) {
            return new CustomEventFromFirebase(in);
        }

        public CustomEventFromFirebase[] newArray(int size) {
            return new CustomEventFromFirebase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable methods END

    // CustomEventFromFirebase methods START

    // CustomEventFromFirebase methods END
}
