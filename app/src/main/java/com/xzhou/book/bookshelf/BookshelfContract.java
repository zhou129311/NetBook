package com.xzhou.book.bookshelf;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.db.BookProvider;

import java.util.List;

public interface BookshelfContract {

    interface Presenter extends BaseContract.Presenter {

        void refresh();

    }

    interface View extends BaseContract.View<Presenter> {
        void onLoadingState(boolean loading);

        void onDataChange(List<BookProvider.LocalBook> books);

        void onBookshelfUpdated(boolean update);

        void onAdd(int position, BookProvider.LocalBook book);

        void onRemove(BookProvider.LocalBook book);
    }
}
