package com.xzhou.book.bookshelf;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.db.BookProvider;

import java.util.List;

public interface BookshelfContract {

    interface Presenter extends BaseContract.Presenter {

        void refresh();

        void download(BookProvider.LocalBook localBook);

    }

    interface View extends BaseContract.View<Presenter> {
        void onLoadingState(boolean loading);

        void onDataChange(List<BookProvider.LocalBook> books);

        void onBookshelfUpdated(boolean update, String error);

        void onAdd(int position, BookProvider.LocalBook book);

        void onRemove(BookProvider.LocalBook book);

        void onUpdateDownloadState(BookProvider.LocalBook localBook);
    }
}
