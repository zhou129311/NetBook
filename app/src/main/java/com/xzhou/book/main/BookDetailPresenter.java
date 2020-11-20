package com.xzhou.book.main;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.DownloadManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class BookDetailPresenter extends BasePresenter<BookDetailContract.View> implements BookDetailContract.Presenter
        , DownloadManager.DownloadCallback {
//    private static final String TAG = "BookDetailPresenter";

    private final String mBookId;
    private Entities.BookDetail mBookDetail;

    BookDetailPresenter(BookDetailContract.View view, String bookId) {
        super(view);
        mBookId = bookId;
        DownloadManager.get().addCallback(mBookId, this);
    }

    @Override
    public boolean start() {
        if (mBookDetail == null) {
            ZhuiShuSQApi.getPool().execute(() -> {
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
            });
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        DownloadManager.get().removeCallback(mBookId, this);
    }

    @Override
    public boolean download() {
        if (DownloadManager.get().hasDownloading(mBookId)) {
            return false;
        }
        ZhuiShuSQApi.getPool().execute(() -> {
            BookProvider.insertOrUpdate(new BookProvider.LocalBook(mBookDetail), false);
            BookProvider.LocalBook book = BookManager.get().findById(mBookId);
            String id = null;
            if (book != null) {
                id = book.sourceId;
            }
            List<Entities.Chapters> chaptersList = AppSettings.getChapterList(mBookId);
            if (chaptersList == null) {
                Entities.BookAToc aToc = ZhuiShuSQApi.getBookMixAToc(mBookId, id);
                if (aToc != null && aToc.chapters != null && aToc.chapters.size() > 0) {
                    chaptersList = aToc.chapters;
                    AppSettings.saveChapterList(mBookId, chaptersList);
                }
            }
            if (chaptersList != null && chaptersList.size() > 0) {
                DownloadManager.Download download = DownloadManager.createAllDownload(chaptersList);
                DownloadManager.get().startDownload(mBookId, download);
            } else {
                int error = AppUtils.isNetworkAvailable() ? DownloadManager.ERROR_NO_TOPIC : DownloadManager.ERROR_NO_NETWORK;
                onEndDownload(0, error);
            }
        });
        return true;
    }

    private void setBookDetail() {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onInitBookDetail(mBookDetail);
            }
        });
    }

    private void setBookHotReviews(final List<MultiItemEntity> list) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onInitReviews(list);
            }
        });
    }

    private void setBookCommend(final List<MultiItemEntity> list) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onInitRecommend(list);
            }
        });
    }

    @Override
    public void onStartDownload() {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onStartDownload();
            }
        });
    }

    @Override
    public void onProgress(final int progress, final int max) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onProgress(progress, max);
            }
        });
    }

    @Override
    public void onEndDownload(final int failedCount, final int error) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onEndDownload(failedCount, error);
            }
        });
    }
}
