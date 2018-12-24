package com.xzhou.book.main;

import com.xzhou.book.common.BasePresenter;

public class BookListPresenter extends BasePresenter<BookListContract.View> implements BookListContract.Presenter {

    public BookListPresenter(BookListContract.View view) {
        super(view);
    }

}
