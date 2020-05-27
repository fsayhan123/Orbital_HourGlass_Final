package com.example.weekcalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database Name
    public static final String DATABASE_NAME = "Calendar.db";

    //Table Names
    public static final String TO_DO_TABLE = "To_Do_Table";
    public static final String EVENT_TABLE = "Events_Table";
    public static final String EXPENDITURE_TABLE = "Expenditure_Table";

    //To Do Table Columns
    public static final String TO_DO_1 = "DATE";

    //Expenditure Table Columns
    public static final String EXPENSE_1 = "DATE";
    public static final String EXPENSE_2 = "CATEGORY";
    public static final String EXPENSE_3 = "AMOUNT";

    //Events Table Columns
    public static final String EVENT_1 = "START_DATE";
    public static final String EVENT_2 = "END_DATE";
    public static final String EVENT_3 = "START_TIME";
    public static final String EVENT_4 = "END_TIME";
    public static final String EVENT_5 = "DESCRIPTION";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    //Initialise SQL Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EVENT_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, START_DATE DATE, END_DATE DATE, START_TIME TEXT, END_TIME TEXT, DESCRIPTION TEXT)");
        db.execSQL("create table " + EXPENDITURE_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE DATE, CATEGORY TEXT, AMOUNT INTEGER)");
        db.execSQL("create table " + TO_DO_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT)");
    }

    //Rebuilds SQL Tables when schema is changed.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENDITURE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TO_DO_TABLE);
        onCreate(db);
    }

    public void testing() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TO_DO_1, "2012/05/10");
        db.insert(TO_DO_TABLE, null, cv);
    }

}
