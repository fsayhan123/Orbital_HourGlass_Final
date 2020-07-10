package com.example.weekcalendar.customclasses.event;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomEvent that = (CustomEvent) o;
        return getTitle().equals(that.getTitle()) &&
                getStartDate().equals(that.getStartDate()) &&
                getEndDate().equals(that.getEndDate()) &&
                getStartTime().equals(that.getStartTime()) &&
                getEndTime().equals(that.getEndTime()) &&
                ID.equals(that.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getStartDate(), getEndDate(), getStartTime(), getEndTime(), ID);
    }

    //    @Override
//    public boolean equals(@Nullable Object obj) {
//        if (obj == null || !(obj instanceof CustomEvent)) {
//            return false;
//        } else if (obj instanceof CustomEvent) {
//            CustomEvent e = (CustomEvent) obj;
//            return this.getTitle().equals(e.getTitle())
//                    && this.getStartDate().equals(e.getStartDate())
//                    && this.getStartTime().equals(e.getStartTime())
//                    && this.getEndDate().equals(e.getEndDate())
//                    && this.getEndTime().equals(e.getEndTime());
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public int hashCode() {
//        return (this.getTitle().hashCode()
//                + this.getStartDate()
//                + this.getStartTime()
//                + this.getEndDate()
//                + this.getEndTime()).hashCode();
//    }

    // CustomEvent methods END
}
