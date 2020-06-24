package com.example.weekcalendar.customclasses.event;

/*
 to store data relevant to each event
 */

import android.os.Parcel;
import android.os.Parcelable;

public class CustomEventFromFirebase extends CustomEvent implements Parcelable {
    private String docID;

    public CustomEventFromFirebase(String title, String startDate, String endDate, String startTime, String endTime, String docID) {
        super(title, startDate, endDate, startTime, endTime);
        this.docID = docID;
    }

    // Parcelable methods START

    // Parcelable constructor
    private CustomEventFromFirebase(Parcel in) {
        super(in);
        this.docID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.docID);
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

    public void setId(String docID) {
        this.docID = docID;
    }

    public String getId() { return this.docID; }

    // CustomEventFromFirebase methods END
}
