package com.example.john.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class BookProvider extends ContentProvider {
    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 100;
    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Database helper object
     */
    private BookDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the books table.
                cursor = database.query(BookContract.BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.john.bookstoreapp/books/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the books table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BookContract.BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the Cursor
        //so we know waht content URI the Cursor was created for.
        //If the data at this URI changes, then we know we need to update the Cursor.

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertions is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
        if (name == null)
            throw new IllegalArgumentException("Book requires a name");
        Integer price = values.getAsInteger(BookContract.BookEntry.COLUMN_PRICE);
        if (price == null || price < 0)
            throw new IllegalArgumentException("No Negative Prices");
        Integer quantity = values.getAsInteger(BookContract.BookEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0)
            throw new IllegalArgumentException("No negative books");

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new book with the given values
        long id = database.insert(BookContract.BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Toast.makeText(getContext(), "Failed to insert row for ", Toast.LENGTH_SHORT).show();
            return null;
        }
        //Notify all; listeners that data has changed for the book content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for" + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link BookEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookContract.BookEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
            if (name == null)
                throw new IllegalArgumentException("Book requires a name");
        }
        if (values.containsKey(BookContract.BookEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(BookContract.BookEntry.COLUMN_PRICE);
            if (price == null || price < 0)
                throw new IllegalArgumentException("No Negative Prices");
        }
        if (values.containsKey(BookContract.BookEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookContract.BookEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0)
                throw new IllegalArgumentException("No negative books");
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookContract.BookEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
// Track the number of rows that were deleted
        int rowsDeleted;
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookContract.BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookContract.BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
