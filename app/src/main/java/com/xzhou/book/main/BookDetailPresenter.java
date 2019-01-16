package com.xzhou.book.main;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.db.BookProvider;
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
                    mBookDetail = ZhuiShuSQApi.getBookDetail(mBookId);
                    setBookDetail();

                    Entities.HotReview hotReview = ZhuiShuSQApi.getHotReview(mBookId);
                    if (hotReview != null && hotReview.reviews != null && hotReview.reviews.size() > 0) {
                        List<MultiItemEntity> reviews = new ArrayList<>();
                        if (hotReview.reviews.size() > 2) {
                            reviews.addAll(hotReview.reviews.subList(0, 2));
                        } else {
                            reviews.addAll(hotReview.reviews);
                        }
                        setBookHotReviews(reviews);
                    }

                    Entities.Recommend list = ZhuiShuSQApi.getRecommendBook(mBookId);
                    if (list != null && list.books != null && list.books.size() > 0) {
                        List<MultiItemEntity> recommeds = new ArrayList<>();
                        if (list.books.size() > 4) {
                            recommeds.addAll(list.books.subList(0, 4));
                        } else {
                            recommeds.addAll(list.books);
                        }
                        setBookCommend(recommeds);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void setBookDetail() {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitBookDetail(mBookDetail);
                }
            }
        });
    }

    private void setBookHotReviews(final List<MultiItemEntity> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitReviews(list);
                }
            }
        });
    }

    private void setBookCommend(final List<MultiItemEntity> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onInitRecommend(list);
                }
            }
        });
    }
}
