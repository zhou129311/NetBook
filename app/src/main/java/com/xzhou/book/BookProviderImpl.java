package com.xzhou.book;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BookProviderImpl extends ContentProvider {
    private static final String AUTHORITY = "com.xzhou.book.provider";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "book.db";
    private static final String TABLE_BOOK = "bookshelf";
    //    private static final int MARCH_BOOK = 1;
    public static final Uri BOOKSHELF_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BOOK);
//    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private DatabaseHelper mHelper;
    private Context mContext;

//    static {
//        URI_MATCHER.addURI(AUTHORITY, TABLE_BOOK, MARCH_BOOK);
//    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mHelper = new DatabaseHelper(mContext);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        uri = ContentProvider.getUriWithoutUserId(uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_BOOK);
//        int match = URI_MATCHER.match(uri);
//        switch (match) {
//        case MARCH_BOOK:
//            qb.setTables(TABLE_BOOK);
//            break;
//        default:
//            return null;
//        }
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(mContext.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        uri = ContentProvider.getUriWithoutUserId(uri);
//        String table;
        SQLiteDatabase db = mHelper.getWritableDatabase();

//        int match = URI_MATCHER.match(uri);
//        switch (match) {
//        case MARCH_BOOK:
//            table = TABLE_BOOK;
//            break;
//        default:
//            return null;
//        }

        long rowId = db.insert(TABLE_BOOK, null, values);
        if (rowId > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        uri = ContentProvider.getUriWithoutUserId(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();
//        String table;
//        int match = URI_MATCHER.match(uri);
//        switch (match) {
//        case MARCH_BOOK:
//            table = TABLE_BOOK;
//            break;
//        default:
//            return 0;
//        }
        int count = db.delete(TABLE_BOOK, selection, selectionArgs);
        if (count > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, String selection, String[] selectionArgs) {
        uri = ContentProvider.getUriWithoutUserId(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();
//        String table;
//        int match = URI_MATCHER.match(uri);
//        switch (match) {
//        case MARCH_BOOK:
//            table = TABLE_BOOK;
//            break;
//        default:
//            return 0;
//        }

        int count = db.update(TABLE_BOOK, values, selection, selectionArgs);
        if (count > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(@Nullable Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createBookTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private void createBookTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOK + "("
                    + BookProvider.COLUMN_ID + " TEXT, "
                    + BookProvider.COLUMN_COVER + " TEXT, "
                    + BookProvider.COLUMN_TITLE + " TEXT, "
                    + BookProvider.COLUMN_LAST_CHAPTER + " TEXT, "
                    + BookProvider.COLUMN_UPDATED + " INTGER, "
                    + " TEXT);");
        }
    }
}
