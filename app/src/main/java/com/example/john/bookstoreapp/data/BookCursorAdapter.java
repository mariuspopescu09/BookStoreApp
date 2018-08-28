package com.example.john.bookstoreapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.bookstoreapp.R;

public class BookCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link BookCursorAdapter}.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button saleView = view.findViewById(R.id.sale);
        // Find the columns of book attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
        int IDColumnIndex = cursor.getColumnIndex(BookContract.BookEntry._ID);

        final String BookName = cursor.getString(nameColumnIndex);
        int Price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int BookID = cursor.getInt(IDColumnIndex);
        if (quantity == 0) {
            saleView.setEnabled(false);
            quantityTextView.setText(R.string.SoldOut);
        } else {
            saleView.setEnabled(true);
            quantityTextView.setText(context.getString(R.string.Stock) + String.valueOf(quantity));
        }
        nameTextView.setText(BookName);
        priceTextView.setText(context.getString(R.string._dollar) + String.valueOf(Price));

        saleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int Stock = quantity - 1;
                if (Stock < 0) {
                    Toast.makeText(context, context.getString(R.string.Stock), Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(BookContract.BookEntry.COLUMN_QUANTITY, Stock);
                    Uri currentBook = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, BookID);
                    context.getContentResolver().update(currentBook, values, null, null);
                }
            }
        });
    }
}
