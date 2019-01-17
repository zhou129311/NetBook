package com.xzhou.book.bookshelf;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.db.BookProvider;

import java.util.List;

public class BookshelfPresenter extends BasePresenter<BookshelfContract.View> implements BookshelfContract.Presenter {

    private boolean mIsStart;

    public BookshelfPresenter(BookshelfContract.View view) {
        super(view);
        BookManager.get().setDataChangeListener(new BookManager.DataChangeListener() {
            @Override
            public void onInsert(int position, BookProvider.LocalBook book) {
                add(position, book);
            }

            @Override
            public void onDelete(BookProvider.LocalBook book) {
                delete(book);
            }

            @Override
            public void onUpdate(List<BookProvider.LocalBook> list) {
                updateList(list);
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        BookManager.get().setDataChangeListener(null);
    }

    @Override
    public boolean start() {
        if (!mIsStart) {
            mIsStart = true;
            List<BookProvider.LocalBook> books = BookManager.get().getLocalBooks();
            updateList(books);
            return true;
        }
        return false;
    }

    @Override
    public void refresh() {
        mView.onLoadingState(true);
        MyApp.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mView.onLoadingState(false);
            }
        }, 2000);
    }

    private void updateList(final List<BookProvider.LocalBook> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoadingState(false);
                    mView.onDataChange(list);
                }
            }
        });
    }

    private void add(final int position, final BookProvider.LocalBook book) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onAdd(position, book);
                }
            }
        });
    }

    private void delete(final BookProvider.LocalBook book) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onRemove(book);
                }
            }
        });
    }

    private void updated(final boolean updated) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onBookshelfUpdated(updated);
                }
            }
        });
    }
}
