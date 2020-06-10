package com.example.weekcalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Name
    public static final String DATABASE_NAME = "Calendar.db";

    // Database Version
    public static final int DATABASE_VERSION = 19;

    // Table Names
    public static final String EXPENSE_TABLE = "Expense_Table";
    public static final String EVENTS_TABLE = "Events_Table";
    public static final String TO_DO_TABLE = "To_Do_Table";

    // To Do Table Columns
    public static final String TO_DO_1 = "DATE";
    public static final String TO_DO_2 = "DETAILS";

    // CustomExpense Table Columns
    public static final String EXPENSE_1 = "DATE";
    public static final String EXPENSE_2 = "CATEGORY";
    public static final String EXPENSE_3 = "AMOUNT";
    public static final String EXPENSE_4 = "NAME";

    // Events Table Columns
    public static final String EVENTS_1 = "START_DATE";
    public static final String EVENTS_2 = "END_DATE";
    public static final String EVENTS_3 = "START_TIME";
    public static final String EVENTS_4 = "END_TIME";
    public static final String EVENTS_5 = "ACTIVITY";

    // static variable to prevent reinitialising every time the method is called
    private static final HashMap<String, String> mapper = new HashMap<>();
    static { mapper.put("Jan", "01");
             mapper.put("Feb", "02");
             mapper.put("Mar", "03");
             mapper.put("Apr", "04");
             mapper.put("May", "05");
             mapper.put("Jun", "06");
             mapper.put("Jul", "07");
             mapper.put("Aug", "08");
             mapper.put("Sep", "09");
             mapper.put("Oct", "10");
             mapper.put("Nov", "11");
             mapper.put("Dec", "12"); }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EVENTS_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, START_DATE TEXT, END_DATE TEXT, START_TIME TEXT, END_TIME TEXT, ACTIVITY TEXT)");
        db.execSQL("create table " + EXPENSE_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, CATEGORY TEXT, AMOUNT INTEGER, NAME TEXT)");
        db.execSQL("create table " + TO_DO_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, DETAILS TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "Expenditure_Table");
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TO_DO_TABLE);
        onCreate(db);
    }

    public String convertDate(String month) {
        mapper.put("Jan", "01");
        mapper.put("Feb", "02");
        mapper.put("Mar", "03");
        mapper.put("Apr", "04");
        mapper.put("May", "05");
        mapper.put("Jun", "06");
        mapper.put("Jul", "07");
        mapper.put("Aug", "08");
        mapper.put("Sep", "09");
        mapper.put("Oct", "10");
        mapper.put("Nov", "11");
        mapper.put("Dec", "12");
        return mapper.get(month);
    }

    /*
    Database methods for Events page
     */

    // Adds event in EVENTS_TABLE, date in DD-MMM-YYYY format
    public boolean addEvent(String eventTitle, String startDate, String endDate, String startTime, String endTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] startDateArr = startDate.split(" ");
        startDateArr[1] = this.convertDate(startDateArr[1].substring(0,3));
        if (startDateArr[0].length() == 1) {
            startDateArr[0] = "0" + startDateArr[0];
        }
        String editedStartDate = String.join("-", startDateArr[2], startDateArr[1], startDateArr[0]);

        String[] endDateArr = endDate.split(" ");
        endDateArr[1] = this.convertDate(endDateArr[1].substring(0,3));
        if (endDateArr[0].length() == 1) {
            endDateArr[0] = "0" + endDateArr[0];
        }
        String editedEndDate = String.join("-", endDateArr[2], endDateArr[1], endDateArr[0]);

        Cursor numDaysQuery = db.rawQuery(String.format("Select Cast((julianday(%s) - julianday(%s)) As Integer)", editedStartDate, editedEndDate), null);
        numDaysQuery.moveToLast();
        int numDays = numDaysQuery.getInt(0);

        if (numDays > 0) {
            for (int i = 1; i < numDays+1; i++) {
                Cursor newDateQuery = db.rawQuery(String.format("SELECT Date(\"%s\", \"+%s days\")", editedStartDate, i), null);
                newDateQuery.moveToLast();
                String newStartDate = newDateQuery.getString(0);
                ContentValues contentValues = new ContentValues();
                contentValues.put(EVENTS_1, newStartDate);
                contentValues.put(EVENTS_2, editedEndDate);
                contentValues.put(EVENTS_3, "All Day");
                contentValues.put(EVENTS_4, "All Day");
                contentValues.put(EVENTS_5, eventTitle);
                db.insert(EVENTS_TABLE, null, contentValues);
            }
        }

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

    // Get all days with event items
    public Cursor getEventData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT DISTINCT START_DATE FROM Events_Table ORDER BY START_DATE ASC", null);
        return result;
    }

    // Get all event data with a given startDate
    public Cursor getEventData(String startDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM Events_Table WHERE START_DATE = \"" + startDate + "\"" + "ORDER BY START_TIME ASC", null);
        return result;
    }

    public Cursor getEventDataByID(String eventID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM Events_Table WHERE ID = " + eventID + "\"", null );
        return result;
    }

    /*
    Database methods for Expense page
     */

    public boolean addExpense(String name, String expense, String category, String expenseDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] expenseDateArr = expenseDate.split(" ");
        expenseDateArr[1] = this.convertDate(expenseDateArr[1].substring(0,3));
        if (expenseDateArr[0].length() == 1) {
            expenseDateArr[0] = "0" + expenseDateArr[0];
        }
        String editedExpenseDate = String.join("-", expenseDateArr[2], expenseDateArr[1], expenseDateArr[0]);

        ContentValues contentValues = new ContentValues();
        contentValues.put(EXPENSE_1, editedExpenseDate);
        contentValues.put(EXPENSE_2, category);
        contentValues.put(EXPENSE_3, expense);
        contentValues.put(EXPENSE_4, name);
        long result = db.insert(EXPENSE_TABLE, null, contentValues);
        if (result == -1) {
            return false;
        } return true;
    }

    public boolean updateExpense(int ID, String name, String expense, String category, String expenseDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] expenseDateArr = expenseDate.split(" ");
        expenseDateArr[1] = this.convertDate(expenseDateArr[1].substring(0,3));
        if (expenseDateArr[0].length() == 1) {
            expenseDateArr[0] = "0" + expenseDateArr[0];
        }
        String editedExpenseDate = String.join("-", expenseDateArr[2], expenseDateArr[1], expenseDateArr[0]);

        ContentValues contentValues = new ContentValues();
        contentValues.put(EXPENSE_1, editedExpenseDate);
        contentValues.put(EXPENSE_2, category);
        contentValues.put(EXPENSE_3, expense);
        contentValues.put(EXPENSE_4, name);
        long result = db.update(EXPENSE_TABLE, contentValues, "ID = " + ID, null);
        return result != -1;
    }

    public boolean deleteExpense(int ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(EXPENSE_TABLE, "ID = " + ID, null) != -1;
    }

    // Get all days with expense items
    public Cursor getDayExpenseData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT DISTINCT DATE FROM Expense_Table ORDER BY DATE ASC", null);
        return result;
    }

    // Get all expense data for a given date
    public Cursor getDayExpenseData(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM Expense_Table WHERE DATE = \"" + date + "\" ORDER BY CATEGORY ASC" , null);
        return result;
    }

    public Cursor getExpenseDetails(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM Expense_Table WHERE ID = \"" + id + "\"", null);
        return result;
    }

    /*
    Database methods for To Do page
     */

    public boolean addToDo(String date, String details) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] todoDateArr = date.split(" ");
        todoDateArr[1] = this.convertDate(todoDateArr[1].substring(0, 3));
        if (todoDateArr[0].length() == 1) {
            todoDateArr[0] = "0" + todoDateArr[0];
        }

        String editedToDoDate = String.join("-", todoDateArr[2], todoDateArr[1], todoDateArr[0]);

        ContentValues cV = new ContentValues();
        cV.put(TO_DO_1, editedToDoDate);
        cV.put(TO_DO_2, details);

        return db.insert(TO_DO_TABLE, null, cV) != -1;
    }

    // gets all days with to-do items
    public Cursor getToDo() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT DISTINCT DATE FROM To_Do_Table ORDER BY DATE ASC", null);
        return result;
    }

    // gets all to-do for a given date
    public Cursor getToDo(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM To_Do_Table WHERE DATE = \"" + date + "\" ORDER BY ID ASC", null);
        return result;
    }
}