package com.xzhou.book.db;

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
import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import java.util.List;

public class BookProviderImpl extends ContentProvider {
    private static final String AUTHORITY = "com.xzhou.book.provider";
    private static final int DB_VERSION = 2;
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
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_BOOK);
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
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String bookId = null;
        if (values != null) {
            bookId = values.getAsString(BookProvider.COLUMN_ID);
        }
        long rowId = db.insert(TABLE_BOOK, null, values);
        if (rowId > 0) {
            List<BookProvider.LocalBook> list = loadListInternal(db, BookProvider.COLUMN_ID + "=?", new String[] { bookId });
            if (list != null) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    BookProvider.LocalBook book = list.get(i);
                    if (TextUtils.equals(book._id, bookId)) {
                        BookManager.get().onInsert(i, book);
                        break;
                    }
                }
            }
            mContext.getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String bookId = null;
        if (selectionArgs != null) {
            bookId = selectionArgs[0];
        }
        int count = db.delete(TABLE_BOOK, selection, selectionArgs);
        if (count > 0) {
            AppUtils.deleteBookCache(bookId);
            BookManager.get().onDelete(bookId);
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count = db.update(TABLE_BOOK, values, selection, selectionArgs);
        if (count > 0) {
            BookManager.get().onUpdate(loadListInternal(db, null, null));
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    public static List<BookProvider.LocalBook> loadListInternal(SQLiteDatabase db, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TABLE_BOOK);
            String sortOrder = BookProvider.getOrder();
            cursor = qb.query(db, null, selection, selectionArgs, null, null, sortOrder);
            return BookProvider.parseCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AppUtils.close(cursor);
        }
        return null;
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
            switch (oldVersion) {
            case 1:
                String sql = "ALTER TABLE " + TABLE_BOOK + " ADD COLUMN " + BookProvider.COLUMN_CUR_SOURCE_ID + " TEXT ";
                db.execSQL(sql);
                break;
            }
        }

        private void createBookTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOK + "("
                    + BookProvider.COLUMN_ID + " TEXT, "
                    + BookProvider.COLUMN_COVER + " TEXT, "
                    + BookProvider.COLUMN_TITLE + " TEXT, "
                    + BookProvider.COLUMN_LAST_CHAPTER + " TEXT, "
                    + BookProvider.COLUMN_UPDATED + " LONG, "
                    + BookProvider.COLUMN_LAST_READ_TIME + " LONG, "
                    + BookProvider.COLUMN_ADD_TIME + " LONG, "
                    + BookProvider.COLUMN_CUR_SOURCE + " TEXT, "
                    + BookProvider.COLUMN_CUR_SOURCE_ID + " TEXT);");
        }
    }
}
