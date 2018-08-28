package com.example.john.bookstoreapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "BookStore.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BookDbHelper}.
     *
     * @param context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKSTORE_TABLE = "CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + " ( "
                + BookContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookContract.BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookContract.BookEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + BookContract.BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKSTORE_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int newVersion) {
    }
}


