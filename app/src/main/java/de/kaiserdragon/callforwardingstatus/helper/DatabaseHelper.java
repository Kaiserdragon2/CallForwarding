package de.kaiserdragon.callforwardingstatus.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "PhoneNumber.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and column names
    private static final String TABLE_NAME = "phone_numbers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_SELECTED = "selected";
    // SQL statement to create the table
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PHONE_NUMBER + " TEXT NOT NULL," + COLUMN_SELECTED + " INTEGER NOT NULL" + ")";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String getColumnPhoneNumber() {
        return COLUMN_PHONE_NUMBER;
    }

    public String getColumnId() {
        return COLUMN_ID;
    }

    public String getColumnSelected() {
        return COLUMN_SELECTED;
    }

    public void changeSelected(String id) {
        SQLiteDatabase db = getReadableDatabase();
        // Update all rows to have selected value "false"

        ContentValues values = new ContentValues();
        // Update the row with the specified ID to have selected value "true"
        values.put(DatabaseHelper.COLUMN_SELECTED, "true");
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {id};
        int ok = db.update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);
        if (ok == 1) {
            values.put(DatabaseHelper.COLUMN_SELECTED, "false");
            whereClause = DatabaseHelper.COLUMN_SELECTED + " = ? AND " + DatabaseHelper.COLUMN_ID + " != ?";
            whereArgs = new String[]{"true", id};
            db.update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

        }
    }

    public String[] getSelected() {
        SQLiteDatabase db = getReadableDatabase();
        // Define a projection that specifies which columns from the database you will actually use after this query
        String[] projection = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_PHONE_NUMBER, DatabaseHelper.COLUMN_SELECTED};

        // Filter results WHERE "selected" = "true"
        String selection = DatabaseHelper.COLUMN_SELECTED + " = ?";
        String[] selectionArgs = {"true"};

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        int selectedRowId = -1;
        String selectedPhoneNumber = "";
        if (cursor.moveToFirst()) {
            selectedRowId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            selectedPhoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER));
        }
        cursor.close();
        return new String[]{String.valueOf(selectedRowId), selectedPhoneNumber};
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table
        db.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}