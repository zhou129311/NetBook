package com.xzhou.book.main;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

import java.util.ArrayList;
import java.util.List;

public class BookDetailPresenter extends BasePresenter<BookDetailContract.View> implements BookDetailContract.Presenter {
    private static final String TAG = "BookDetailPresenter";

    private final String mBookId;
    private Entities.BookDetail mBookDetail;

    BookDetailPresenter(BookDetailContract.View view, String bookId) {
        super(view);
        mBookId = bookId;
    }

    @Override
    public boolean start() {
        if (mBookDetail == null) {
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    mBookDetail = ZhuiShuSQApi.get().getBookDetail(mBookId);
                    setBookDetail();

                    Entities.HotReview hotReview = ZhuiShuSQApi.get().getHotReview(mBookId);
                    if (hotReview != null && hotReview.reviews != null && hotReview.reviews.size() > 0) {
                        List<MultiItemEntity> list = new ArrayList<MultiItemEntity>(hotReview.reviews);
                        setBookHotReviews(list);
                    }

                    Entities.RecommendBookList list = ZhuiShuSQApi.get().getRecommendBookList(mBookId, "4");
                    if (list != null && list.booklists != null && list.booklists.size() > 0) {
                        List<MultiItemEntity> l = new ArrayList<MultiItemEntity>(list.booklists);
                        setBookCommend(l);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void setBookDetail() {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitBookDetail(mBookDetail);
                }
            }
        });
    }

    private void setBookHotReviews(final List<MultiItemEntity> list) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitReviews(list);
                }
            }
        });
    }

    private void setBookCommend(final List<MultiItemEntity> list) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitRecommend(list);
                }
            }
        });
    }
}
