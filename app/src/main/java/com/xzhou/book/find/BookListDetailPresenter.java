package com.xzhou.book.find;

import com.xzhou.book.common.BasePresenter;

public class BookListDetailPresenter extends BasePresenter<BookListDetailContract.View> implements BookListDetailContract.Presenter {

    private String mBookListId;

    public BookListDetailPresenter(BookListDetailContract.View view, String bookListId) {
        super(view);
        mBookListId = bookListId;
    }

    @Override
    public boolean start() {
        return super.start();
    }
}
