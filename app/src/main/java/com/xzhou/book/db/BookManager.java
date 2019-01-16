package com.xzhou.book.db;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class BookManager {

    private static BookManager sInstance;
    private final List<BookProvider.LocalBook> mList = new ArrayList<>();
    private DataChangeListener mDataChangeListener;

    public interface DataChangeListener {
        void onInsert(int position, BookProvider.LocalBook book);

        void onDelete(BookProvider.LocalBook book);

        void onUpdate(List<BookProvider.LocalBook> list);
    }

    public static BookManager get() {
        if (sInstance == null) {
            sInstance = new BookManager();
        }
        return sInstance;
    }

    private BookManager() {
    }

    public void init() {
        List<BookProvider.LocalBook> list = BookProvider.loadBookshelfList();
        if (list != null && list.size() > 0) {
            synchronized (mList) {
                mList.addAll(list);
            }
        }
    }

    public List<BookProvider.LocalBook> getLocalBooks() {
        List<BookProvider.LocalBook> list;
        synchronized (mList) {
            list = new ArrayList<>(mList);
        }
        return list;
    }

    public BookProvider.LocalBook findById(String bookId) {
        if (bookId == null) {
            return null;
        }
        synchronized (mList) {
            for (BookProvider.LocalBook book : mList) {
                if (TextUtils.equals(bookId, book._id)) {
                    return book;
                }
            }
        }
        return null;
    }

    public void setDataChangeListener(DataChangeListener listener) {
        mDataChangeListener = listener;
    }

    public void onUpdate(List<BookProvider.LocalBook> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
        }
        if (mDataChangeListener != null) {
            mDataChangeListener.onUpdate(list);
        }
    }

    public void onInsert(int position, BookProvider.LocalBook book) {
        if (book == null) {
            return;
        }
        synchronized (mList) {
            mList.add(position, book);
        }
        if (mDataChangeListener != null) {
            mDataChangeListener.onInsert(position, book);
        }
    }

    public void onDelete(String bookId) {
        if (bookId == null) {
            return;
        }
        BookProvider.LocalBook book = findById(bookId);
        if (book == null) {
            return;
        }

        synchronized (mList) {
            mList.remove(book);
        }
        if (mDataChangeListener != null) {
            mDataChangeListener.onDelete(book);
        }
    }
}
