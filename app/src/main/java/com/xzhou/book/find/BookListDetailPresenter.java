package com.xzhou.book.find;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

public class BookListDetailPresenter extends BasePresenter<BookListDetailContract.View> implements BookListDetailContract.Presenter {

    private String mBookListId;
    private Entities.BookListDetail mBookListDetail;

    BookListDetailPresenter(BookListDetailContract.View view, String bookListId) {
        super(view);
        mBookListId = bookListId;
    }

    @Override
    public boolean start() {
        if (mBookListDetail == null) {
            mView.onLoading(true);
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    mBookListDetail = ZhuiShuSQApi.getBookListDetail(mBookListId);
                    setData();
                }
            });
            return true;
        }
        return false;
    }

    private void setData() {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoading(false);
                    mView.onInitData(mBookListDetail);
                }
            }
        });
    }
}
