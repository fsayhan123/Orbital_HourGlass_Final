package com.example.weekcalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database Name
    public static final String DATABASE_NAME = "Calendar.db";

    //Database Version
    public static final int DATABASE_VERSION = 5;

    //Table Names
    public static final String EXPENSE_TABLE = "Expense_Table";
    public static final String EVENTS_TABLE = "Events_Table";
    public static final String TO_DO_TABLE = "To_Do_Table";

    //To Do Table Columns
    public static final String TO_DO_1 = "DETAILS";

    //Expense Table Columns
    public static final String EXPENSE_1 = "DATE";
    public static final String EXPENSE_2 = "CATEGORY";
    public static final String EXPENSE_3 = "AMOUNT";

    //Events Table Columns
    public static final String EVENTS_1 = "START_DATE";
    public static final String EVENTS_2 = "END_DATE";
    public static final String EVENTS_3 = "START_TIME";
    public static final String EVENTS_4 = "END_TIME";
    public static final String EVENTS_5 = "ACTIVITY";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EVENTS_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, START_DATE TEXT, END_DATE TEXT, START_TIME TEXT, END_TIME TEXT, ACTIVITY TEXT)");
        db.execSQL("create table " + EXPENSE_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, CATEGORY TEXT, AMOUNT INTEGER)");
        db.execSQL("create table " + TO_DO_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DETAILS TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "Expenditure_Table");
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TO_DO_TABLE);
        onCreate(db);
    }

    //Adds event in EVENTS_TABLE, date in DD-MMM-YYYY format
    public boolean addEvent(String eventTitle, String startDate, String endDate, String startTime, String endTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] startDateArr = startDate.split(" ");
        startDateArr[1] = startDateArr[1].substring(0,3);
        String editedStartDate = String.join(" ", startDateArr[0], startDateArr[1], startDateArr[2]);
        String[] endDateArr = endDate.split(" ");
        endDateArr[1] = endDateArr[1].substring(0,3);
        String editedEndDate = String.join(" ", endDateArr[0], endDateArr[1], endDateArr[2]);
        ContentValues contentValues = new ContentValues();
        contentValues.put(EVENTS_1, editedStartDate);
        contentValues.put(EVENTS_2, editedEndDate);
        contentValues.put(EVENTS_3, startTime);
        contentValues.put(EVENTS_4, endTime);
        contentValues.put(EVENTS_5, eventTitle);
        long result = db.insert(EVENTS_TABLE, null, contentValues);
        if (result == -1) {
            return false;
        } return true;
    }

    //
    public Cursor getEventData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM Events_table", null);
        return result;
    }
}