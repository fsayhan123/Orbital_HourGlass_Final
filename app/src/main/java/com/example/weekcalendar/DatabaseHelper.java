package com.example.weekcalendar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database Name
    public static final String DATABASE_NAME = "Calendar.db";

    //Database Version
    public static final int DATABASE_VERSION = 4;

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
}
