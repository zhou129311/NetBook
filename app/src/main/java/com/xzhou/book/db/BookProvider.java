package com.xzhou.book.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.DownloadManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class BookProvider {
    private static final String TAG = "BookProvider";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_COVER = "cover";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_LAST_CHAPTER = "last_chapter";
    static final String COLUMN_UPDATED = "updated";
    static final String COLUMN_LAST_READ_TIME = "read_time";
    static final String COLUMN_ADD_TIME = "add_time";
    static final String COLUMN_CUR_SOURCE = "cur_source";

    private static final String[] PROJECTION = new String[] {
            COLUMN_ID, COLUMN_COVER, COLUMN_TITLE, COLUMN_LAST_CHAPTER,
            COLUMN_UPDATED, COLUMN_LAST_READ_TIME, COLUMN_CUR_SOURCE, COLUMN_ADD_TIME
    };

    public static class LocalBook implements MultiItemEntity, Parcelable {
        public String _id;
        public long updated;
        public long readTime;
        public long addTime;
        public String title;
        public String lastChapter;
        public String cover;
        public String curSource;
        private boolean isBookshelf;

        public LocalBook(Entities.BookDetail detail) {
            _id = detail._id;
            updated = AppUtils.getTimeFormDateString(detail.updated);
            title = detail.title;
            lastChapter = detail.lastChapter;
            cover = detail.cover();
            curSource = detail.site;
        }

        public LocalBook() {
        }

        ContentValues toContentValues() {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ID, _id);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_UPDATED, updated);
            values.put(COLUMN_LAST_READ_TIME, readTime);
            values.put(COLUMN_COVER, cover);
            values.put(COLUMN_CUR_SOURCE, curSource);
            values.put(COLUMN_LAST_CHAPTER, lastChapter);
            values.put(COLUMN_ADD_TIME, addTime);
            return values;
        }

        LocalBook(Parcel in) {
            _id = in.readString();
            updated = in.readLong();
            readTime = in.readLong();
            addTime = in.readLong();
            title = in.readString();
            lastChapter = in.readString();
            cover = in.readString();
            curSource = in.readString();
            isBookshelf = in.readBoolean();
        }

        public boolean isBookshelf() {
            return isBookshelf || BookProvider.hasCacheData(_id);
        }

        public static final Creator<LocalBook> CREATOR = new Creator<LocalBook>() {
            @Override
            public LocalBook createFromParcel(Parcel in) {
                return new LocalBook(in);
            }

            @Override
            public LocalBook[] newArray(int size) {
                return new LocalBook[size];
            }
        };

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_LOCAL_BOOK;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(_id);
            dest.writeLong(updated);
            dest.writeLong(readTime);
            dest.writeLong(addTime);
            dest.writeString(title);
            dest.writeString(lastChapter);
            dest.writeString(cover);
            dest.writeString(curSource);
            dest.writeBoolean(isBookshelf);
        }

        @Override
        public String toString() {
            return "LocalBook{" +
                    "_id='" + _id + '\'' +
                    ", updated=" + updated +
                    ", readTime=" + readTime +
                    ", addTime=" + addTime +
                    ", title='" + title + '\'' +
                    ", lastChapter='" + lastChapter + '\'' +
                    ", cover='" + cover + '\'' +
                    ", curSource='" + curSource + '\'' +
                    ", isBookshelf=" + isBookshelf +
                    '}';
        }
    }

    public static List<LocalBook> loadBookshelfList() {
        Cursor cursor = null;
        try {
            String sortOrder = getOrder();
            cursor = MyApp.getContext().getContentResolver().query(BookProviderImpl.BOOKSHELF_CONTENT_URI,
                    PROJECTION, null, null, sortOrder, null);
            return parseCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AppUtils.close(cursor);
        }
        return null;
    }

    public static List<LocalBook> parseCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        List<LocalBook> list = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            LocalBook book = new LocalBook();
            book._id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
            book.cover = cursor.getString(cursor.getColumnIndex(COLUMN_COVER));
            book.readTime = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_READ_TIME));
            book.updated = cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED));
            book.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            book.curSource = cursor.getString(cursor.getColumnIndex(COLUMN_CUR_SOURCE));
            book.lastChapter = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_CHAPTER));
            book.addTime = cursor.getLong(cursor.getColumnIndex(COLUMN_ADD_TIME));
            book.isBookshelf = true;
            list.add(book);
        }
        return list;
    }

    public static void updateReadTime(LocalBook book) {
        try {
            long time = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            String where = COLUMN_ID + "=?";
            String[] args = new String[] { book._id };
            values.put(COLUMN_LAST_READ_TIME, time);
            MyApp.getContext().getContentResolver().update(BookProviderImpl.BOOKSHELF_CONTENT_URI, values, where, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertOrUpdate(final LocalBook book, boolean setReadTime) {
        if (book == null) {
            Log.e(TAG, "insertOrUpdate error , book = null");
            return;
        }
        try {
            long time = System.currentTimeMillis();
            ContentValues values = book.toContentValues();
            book.isBookshelf = true;
            String where = COLUMN_ID + "=?";
            String[] args = new String[] { book._id };
            if (hasCacheData(book._id)) {
                if (setReadTime) {
                    values.put(COLUMN_LAST_READ_TIME, time);
                }
                MyApp.getContext().getContentResolver().update(BookProviderImpl.BOOKSHELF_CONTENT_URI, values, where, args);
            } else {
                if (setReadTime) {
                    values.put(COLUMN_LAST_READ_TIME, time);
                }
                values.put(COLUMN_ADD_TIME, time);
                MyApp.getContext().getContentResolver().insert(BookProviderImpl.BOOKSHELF_CONTENT_URI, values);
                MyApp.runUI(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShortToast(AppUtils.getString(R.string.book_detail_has_joined_the_book_shelf, book.title));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(String booId, final String title) {
        try {
            String where = COLUMN_ID + "=?";
            String[] args = new String[] { booId };
            MyApp.getContext().getContentResolver().delete(BookProviderImpl.BOOKSHELF_CONTENT_URI, where, args);
            MyApp.runUI(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShortToast(AppUtils.getString(R.string.book_detail_has_remove_the_book_shelf, title));
                }
            });
            DownloadManager.get().pauseDownload(booId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasCacheData(String bookId) {
        String where = COLUMN_ID + "=?";
        String[] args = new String[] { bookId };
        try (Cursor cursor = MyApp.getContext().getContentResolver().query(BookProviderImpl.BOOKSHELF_CONTENT_URI, null,
                where, args, null)) {
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getOrder() {
        int order = AppSettings.getBookshelfOrder();
        String sortOrder;
        switch (order) {
        case AppSettings.PRE_VALUE_BOOKSHELF_ORDER_UPDATE_TIME:
            sortOrder = COLUMN_UPDATED + " DESC";
            break;
        case AppSettings.PRE_VALUE_BOOKSHELF_ORDER_READ_TIME:
            sortOrder = COLUMN_LAST_READ_TIME + " DESC";
            break;
        default:
            sortOrder = COLUMN_ADD_TIME + " DESC";
            break;
        }
        return sortOrder;
    }

}
