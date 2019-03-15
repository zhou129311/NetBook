package com.xzhou.book.read;

import android.text.TextUtils;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartoonPresenter extends BasePresenter<CartoonContract.View> implements CartoonContract.Presenter {
    private static final String TAG = "CartoonPresenter";

    private ExecutorService mSinglePool = Executors.newSingleThreadExecutor();
    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private int mCurChapter;

    public CartoonPresenter(CartoonContract.View view, BookProvider.LocalBook book) {
        super(view);
        mBook = book;
    }

    @Override
    public boolean start() {
        if (mChaptersList == null) {
            final int[] progress = AppSettings.getReadProgress(mBook._id);
            mCurChapter = progress[0];
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    if (mBook.isBookshelf()) {
                        BookProvider.updateReadTime(mBook);
                    }
                    mChaptersList = AppSettings.getChapterList(mBook._id);
                    if (mChaptersList == null) {
                        if (mBook.sourceId == null) {
                            List<Entities.BookSource> list = ZhuiShuSQApi.getBookSource(mBook._id);
                            if (list != null) {
                                for (Entities.BookSource source : list) {
                                    if (source != null && source.host != null) { //漫画需要换到sourceId才可以获取章节目录
                                        mBook.curSourceHost = source.host;
                                        mBook.sourceId = source._id;
                                        if (mBook.isBookshelf()) {
                                            BookProvider.insertOrUpdate(mBook, false);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if (TextUtils.isEmpty(mBook.sourceId)) {
                            Log.i(TAG, "mBook.sourceId = null");
                            return;
                        }
                        Entities.BookAToc aToc = ZhuiShuSQApi.getBookMixAToc(mBook._id, mBook.sourceId);
                        if (aToc != null && aToc.chapters != null) {
                            mChaptersList = aToc.chapters;
                        }
                        if (mChaptersList != null && mBook.isBookshelf()) {
                            AppSettings.saveChapterList(mBook._id, mChaptersList);
                        }
                    }
                    if (mChaptersList != null && mBook.isBookshelf()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0, size = mChaptersList.size(); i < size; i++) {
                                    Entities.Chapters chapters = mChaptersList.get(i);
                                    File file = new File(FileUtils.getCartoonDir(mBook._id, i));
                                    if (file.exists()) {
                                        File[] pics = file.listFiles();
                                        chapters.hasLocal = pics != null && pics.length > 0;
                                    } else {
                                        chapters.hasLocal = false;
                                    }
                                }
                                AppSettings.saveChapterList(mBook._id, mChaptersList);
                            }
                        }).start();
                    }
                    initChaptersList();
                    if (mChaptersList == null || mChaptersList.size() <= 0) {
                        int error = AppUtils.isNetworkAvailable() ? ReadPresenter.Error.CONNECTION_FAIL : ReadPresenter.Error.NO_NETWORK;
//                        showError(0, error, null);
                        return;
                    }
                    loadReadProgress();
                }
            });
            return true;
        }
        return super.start();
    }

    @Override
    public void loadChapter(int itemPosition, int chapter) {

    }

    @Override
    public void reloadCurPage(int itemPosition, CartoonContent pageContent) {

    }

    @Override
    public void loadPreviousPage(int itemPosition, CartoonContent pageContent) {

    }

    @Override
    public void loadNextPage(int itemPosition, CartoonContent pageContent) {

    }

    private void loadReadProgress() {

//        ZhuiShuSQApi.getPictureChapterRead();

    }

    private CartoonContent getPageContent(int chapter, boolean isNext) {
        CartoonContent pageContent = null;
        Entities.Chapters chapters = mChaptersList.get(chapter);
//        ChapterBuffer chapterBuffer = mCacheChapterBuffers.get(getKey(chapter));
//        if (chapterBuffer != null) {
//            pageContent = createNewPageContent(isNext ? chapterBuffer.getPageForPos(0) : chapterBuffer.getEndPage(),
//                    chapters.title, chapter, chapterBuffer.getPageCount());
//        } else {
//            pageContent = createNonePageContent(chapters.title, chapter, hasEndChapter(chapter));
//        }
        return pageContent;
    }

    private boolean hasEndChapter(int chapter) {
        return mChaptersList == null || mChaptersList.size() <= chapter + 1;
    }

    private void initChaptersList() {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.initChapterList(mChaptersList);
                }
            }
        });
    }
}
