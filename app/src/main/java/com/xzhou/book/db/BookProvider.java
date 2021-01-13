package com.xzhou.book.db;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.DownloadManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
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
    static final String COLUMN_ORDER_TOP = "order_top"; //置顶
    static final String COLUMN_CUR_SOURCE = "cur_source";
    static final String COLUMN_CUR_SOURCE_ID = "cur_source_id";
    static final String COLUMN_IS_SHOW_RED = "is_show_red";
    static final String COLUMN_IS_PICTURE = "is_picture";
    // baidu
    static final String COLUMN_IS_BAIDU = "is_baidu";
    static final String COLUMN_READ_URL = "read_url";
    static final String COLUMN_DESC = "bock_desc";

    public static class LocalBook implements MultiItemEntity, Parcelable {
        public String _id;
        public String sourceId;
        public long updated;
        public long readTime;
        public long addTime;
        public boolean hasTop;
        public boolean isPicture;
        public boolean isShowRed = true;
        public String title;
        public String lastChapter;
        public String cover;
        public String curSourceHost;
        private boolean isBookshelf;
        // baidu
        public boolean isBaiduBook;
        public String readUrl;
        public String desc;

        public boolean isChecked;
        public boolean isEdit;

        public String downloadStatus;
        private UpdateDownloadStateListener mStateListener;
        private DownloadManager.DownloadCallback mCallback;

        public LocalBook(Entities.BookDetail detail) {
            _id = detail._id;
            updated = AppUtils.getTimeFormDateString(detail.updated);
            title = detail.title;
            lastChapter = detail.lastChapter;
            cover = detail.cover();
            curSourceHost = detail.site;
            isPicture = detail.isPicture();
        }

        public LocalBook(SearchModel.SearchBook book) {
            isBaiduBook = true;
            _id = book.id;
            title = book.bookName;
            curSourceHost = book.sourceHost;
            sourceId = book.sourceName;
            cover = book.image;
            lastChapter = book.latestChapterName;
            updated = book.updated;
            readUrl = book.readUrl;
            if (book.desc != null && book.desc.length() > 20) {
                desc = book.desc.substring(0, 20) + "...";
            } else {
                desc = book.desc;
            }
        }

        public LocalBook() {
        }

        public interface UpdateDownloadStateListener {
            void onUpdate();
        }

        public void setUpdateDownloadStateListener(UpdateDownloadStateListener listener) {
            mStateListener = listener;
        }

        public boolean checkAddDownloadCallback() {
            if (DownloadManager.get().hasDownloading(_id)) {
                downloadStatus = AppUtils.getString(R.string.book_read_download_start, title);
                if (mCallback == null) {
                    mCallback = new DownloadManager.DownloadCallback() {
                        @Override
                        public void onStartDownload() {
                            downloadStatus = AppUtils.getString(R.string.book_read_download_start, title);
                            if (mStateListener != null) {
                                mStateListener.onUpdate();
                            }
                        }

                        @Override
                        public void onProgress(int progress, int max) {
                            downloadStatus = AppUtils.getString(R.string.book_read_download_progress, title, progress, max);
                            if (mStateListener != null) {
                                mStateListener.onUpdate();
                            }
                        }

                        @Override
                        public void onEndDownload(int failedCount, int error) {
                            if (error != DownloadManager.ERROR_NONE) {
                                downloadStatus = AppUtils.getString(error == DownloadManager.ERROR_NO_NETWORK ? R.string.book_read_download_error_net
                                        : R.string.book_read_download_error_topic);
                            } else {
                                if (failedCount > 0) {
                                    downloadStatus = AppUtils.getString(R.string.book_read_download_complete2, title, failedCount);
                                } else {
                                    downloadStatus = AppUtils.getString(R.string.book_read_download_complete, title);
                                }
                            }
                            if (mStateListener != null) {
                                mStateListener.onUpdate();
                            }
                            DownloadManager.get().removeCallback(_id, this);
                            mStateListener = null;
                        }
                    };
                }
                DownloadManager.get().addCallback(_id, mCallback);
                return true;
            }
            return false;
        }

        public void checkRemoveDownloadCallback() {
            if (mCallback != null) {
                DownloadManager.get().removeCallback(_id, mCallback);
            }
            mStateListener = null;
            mCallback = null;
        }

        public boolean isBookshelf() {
            return isBookshelf || BookProvider.hasCacheData(_id);
        }

        public String getTitle() {
            return TextUtils.isEmpty(curSourceHost) ? title : curSourceHost + "-" + title;
        }

        ContentValues toContentValues() {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ID, _id);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_UPDATED, updated);
            values.put(COLUMN_LAST_READ_TIME, readTime);
            values.put(COLUMN_COVER, cover);
            values.put(COLUMN_CUR_SOURCE, curSourceHost);
            values.put(COLUMN_LAST_CHAPTER, lastChapter);
            values.put(COLUMN_CUR_SOURCE_ID, sourceId);
            values.put(COLUMN_ORDER_TOP, hasTop ? 1 : 0);
            values.put(COLUMN_IS_SHOW_RED, isShowRed ? 1 : 0);
            values.put(COLUMN_IS_BAIDU, isBaiduBook ? 1 : 0);
            values.put(COLUMN_READ_URL, readUrl);
            values.put(COLUMN_IS_PICTURE, isPicture ? 1 : 0);
            values.put(COLUMN_DESC, desc);
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
            curSourceHost = in.readString();
            isBookshelf = in.readInt() == 1;
            hasTop = in.readInt() == 1;
            isShowRed = in.readInt() == 1;
            sourceId = in.readString();
            isBaiduBook = in.readInt() == 1;
            readUrl = in.readString();
            isPicture = in.readInt() == 1;
            desc = in.readString();
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
            dest.writeString(curSourceHost);
            dest.writeInt(isBookshelf ? 1 : 0);
            dest.writeInt(hasTop ? 1 : 0);
            dest.writeInt(isShowRed ? 1 : 0);
            dest.writeString(sourceId);
            dest.writeInt(isBaiduBook ? 1 : 0);
            dest.writeString(readUrl);
            dest.writeInt(isPicture ? 1 : 0);
            dest.writeString(desc);
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
                    ", curSourceHost='" + curSourceHost + '\'' +
                    ", isBookshelf=" + isBookshelf +
                    ", isShowRed=" + isShowRed +
                    ", hasTop=" + hasTop +
                    ", isPicture=" + isPicture +
                    ", readUrl=" + readUrl +
                    ", desc=" + desc +
                    '}';
        }
    }

    public static List<LocalBook> loadBookshelfList() {
        Cursor cursor = null;
        try {
            String sortOrder = getOrder();
            cursor = MyApp.getContext().getContentResolver().query(BookProviderImpl.BOOKSHELF_CONTENT_URI,
                    null, null, null, sortOrder, null);
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
            book.curSourceHost = cursor.getString(cursor.getColumnIndex(COLUMN_CUR_SOURCE));
            book.lastChapter = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_CHAPTER));
            book.addTime = cursor.getLong(cursor.getColumnIndex(COLUMN_ADD_TIME));
            book.sourceId = cursor.getString(cursor.getColumnIndex(COLUMN_CUR_SOURCE_ID));
            book.hasTop = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_TOP)) == 1;
            book.isShowRed = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SHOW_RED)) == 1;
            book.isBaiduBook = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_BAIDU)) == 1;
            book.readUrl = cursor.getString(cursor.getColumnIndex(COLUMN_READ_URL));
            book.isPicture = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PICTURE)) == 1;
            book.desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESC));
            book.isBookshelf = true;
            list.add(book);
        }
        return list;
    }

    public static void updateReadTime(LocalBook book) {
        try {
            ContentValues values = new ContentValues();
            String where = COLUMN_ID + "=?";
            String[] args = new String[]{book._id};
            values.put(COLUMN_LAST_READ_TIME, System.currentTimeMillis());
            values.put(COLUMN_IS_SHOW_RED, 0);
            MyApp.getContext().getContentResolver().update(BookProviderImpl.BOOKSHELF_CONTENT_URI, values, where, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateHasTop(LocalBook book) {
        try {
            ContentValues values = new ContentValues();
            String where = COLUMN_ID + "=?";
            String[] args = new String[]{book._id};
            values.put(COLUMN_ORDER_TOP, book.hasTop ? 1 : 0);
            MyApp.getContext().getContentResolver().update(BookProviderImpl.BOOKSHELF_CONTENT_URI, values, where, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLocalBooks(List<LocalBook> books) {
        if (books == null || books.size() < 1) {
            return;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (LocalBook book : books) {
            ops.add(ContentProviderOperation.newUpdate(BookProviderImpl.BOOKSHELF_CONTENT_URI)
                    .withSelection(COLUMN_ID + "=?", new String[]{book._id})
                    .withValues(book.toContentValues())
                    .withYieldAllowed(true)
                    .build());
        }
        if (ops.size() > 0) {
            try {
                MyApp.getContext().getContentResolver().applyBatch(BookProviderImpl.BOOKSHELF_CONTENT_URI.getAuthority(), ops);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            String[] args = new String[]{book._id};
            if (hasCacheData(book._id)) {
                if (setReadTime) {
                    values.put(COLUMN_LAST_READ_TIME, time);
                    values.put(COLUMN_IS_SHOW_RED, 0);
                }
                MyApp.getContext().getContentResolver().update(BookProviderImpl.BOOKSHELF_CONTENT_URI, values, where, args);
            } else {
                if (setReadTime) {
                    values.put(COLUMN_LAST_READ_TIME, time);
                    values.put(COLUMN_IS_SHOW_RED, 0);
                }
                values.put(COLUMN_ADD_TIME, System.currentTimeMillis());
                MyApp.getContext().getContentResolver().insert(BookProviderImpl.BOOKSHELF_CONTENT_URI, values);
                MyApp.runUI(() -> ToastUtils.showShortToast(AppUtils.getString(R.string.book_detail_has_joined_the_book_shelf, book.title)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(List<String> bookIds, boolean deleteCache) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            for (String bookId : bookIds) {
                DownloadManager.get().pauseDownload(bookId);
                if (deleteCache) {
                    AppUtils.deleteBookCache(bookId);
                }
                ops.add(ContentProviderOperation.newDelete(BookProviderImpl.BOOKSHELF_CONTENT_URI)
                        .withSelection(COLUMN_ID + "=?", new String[]{bookId})
                        .withYieldAllowed(true)
                        .build());
            }
            if (ops.size() > 0) {
                try {
                    MyApp.getContext().getContentResolver().applyBatch(BookProviderImpl.BOOKSHELF_CONTENT_URI.getAuthority(), ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(LocalBook book, boolean deleteCache) {
        delete(book._id, book.title, deleteCache);
        book.isBookshelf = false;
    }

    public static void delete(String bookId, final String title, boolean deleteCache) {
        try {
            String where = COLUMN_ID + "=?";
            String[] args = new String[]{bookId};
            MyApp.getContext().getContentResolver().delete(BookProviderImpl.BOOKSHELF_CONTENT_URI, where, args);
            MyApp.runUI(() -> ToastUtils.showShortToast(AppUtils.getString(R.string.book_detail_has_remove_the_book_shelf, title)));
            DownloadManager.get().pauseDownload(bookId);
            if (deleteCache) {
                AppUtils.deleteBookCache(bookId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasCacheData(String bookId) {
        String where = COLUMN_ID + "=?";
        String[] args = new String[]{bookId};
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
        String sortOrder = COLUMN_ORDER_TOP + " DESC, ";
        switch (order) {
            case AppSettings.PRE_VALUE_BOOKSHELF_ORDER_UPDATE_TIME:
                sortOrder += COLUMN_UPDATED + " DESC";
                break;
            case AppSettings.PRE_VALUE_BOOKSHELF_ORDER_READ_TIME:
                sortOrder += COLUMN_LAST_READ_TIME + " DESC";
                break;
            default:
                sortOrder += COLUMN_ADD_TIME + " DESC";
                break;
        }
        return sortOrder;
    }
}
