package com.xzhou.book.find;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

public class BookListDetailPresenter extends BasePresenter<BookListDetailContract.View> implements BookListDetailContract.Presenter {

    private String mBookListId;
    private boolean hasStart;

    BookListDetailPresenter(BookListDetailContract.View view, String bookListId) {
        super(view);
        mBookListId = bookListId;
    }

    @Override
    public boolean start() {
        if (!hasStart) {
            hasStart = true;
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    Entities.BookListDetail detail = ZhuiShuSQApi.get().getBookListDetail(mBookListId);
                    setData(detail);
                }
            });
            return true;
        }
        return false;
    }

    private void setData(final Entities.BookListDetail detail) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitData(detail);
                }
            }
        });
    }
}
