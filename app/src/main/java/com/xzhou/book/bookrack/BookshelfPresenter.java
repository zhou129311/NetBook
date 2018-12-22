package com.xzhou.book.bookrack;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;

public class BookshelfPresenter extends BasePresenter<BookshelfContract.View> implements BookshelfContract.Presenter {

    public BookshelfPresenter(BookshelfContract.View view) {
        super(view);
    }

    @Override
    public boolean start() {
//        List<Entities.Book> list = new ArrayList<>();
//        list.add(new Entities.Book());
//        list.add(new Entities.Book());
//        list.add(new Entities.Book());
//        list.add(new Entities.Book());
//        list.add(new Entities.Book());
//        mView.onInitData(list);
        return true;
    }

    @Override
    public void refresh() {
        MyApp.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mView.hideLoading();
            }
        },2000);
    }
}
