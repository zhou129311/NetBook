package com.xzhou.book.read;

import android.graphics.Paint;
import android.support.annotation.IntDef;

import com.xzhou.book.BookManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.util.List;

public class ReadPresenter extends BasePresenter<ReadContract.View> implements ReadContract.Presenter {
    private static final String TAG = "ReadPresenter";

    @IntDef({Error.NO_NETWORK, Error.CONNECTION_FAIL, Error.NO_CONTENT, Error.END, Error.NONE})
    public @interface Error {
        int NO_NETWORK = 0;
        int CONNECTION_FAIL = 1;
        int NO_CONTENT = 2;
        int END = 3;
        int NONE = 4;
    }

    private BookManager.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private ChapterBuffer mCurBuffer;
    private ChapterBuffer mPreBuffer;
    private ChapterBuffer mNextBuffer;
    private int mMaxLineCount;
    private Paint mPaint;
    private int mTextViewWidth;

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
        mCurBuffer = new ChapterBuffer(mBook._id);
        mPreBuffer = new ChapterBuffer(mBook._id);
        mNextBuffer = new ChapterBuffer(mBook._id);
    }

    @Override
    public boolean start() {
        if (mChaptersList == null) {
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    mChaptersList = AppSettings.getChapterList(mBook._id);
                    if (mChaptersList == null) {
                        Entities.BookMixAToc mixAToc = ZhuiShuSQApi.getBookMixAToc(mBook._id);
                        if (mixAToc != null && mixAToc.mixToc != null && mixAToc.mixToc.chapters != null) {
                            mChaptersList = mixAToc.mixToc.chapters;
                            AppSettings.saveChapterList(mBook._id, mChaptersList);
                        }
                    }
                    @Error int error = Error.NONE;
                    PageContent curPage = null;
                    String pageNumber = null;
                    String title = null;
                    if (mChaptersList != null) {
                        int[] readProgress = AppSettings.getReadProgress(mBook._id);
                        int chapter = readProgress[0];
                        int startPos = readProgress[1];
                        int endPos = readProgress[2];
                        if (chapter < 0 || chapter > mChaptersList.size()) {
                            chapter = 0;
                            startPos = 0;
                            endPos = 0;
                        }
                        Entities.Chapters chapters = mChaptersList.get(chapter);
                        title = chapters.title;
                        boolean success = false;
                        if (FileUtils.hasCacheChapter(mBook._id, chapter)) {
                            success = mCurBuffer.openCacheBookChapter(chapter);
                        } else {
                            Entities.ChapterRead chapterRead = ZhuiShuSQApi.getChapterRead(chapters.link);
                            if (chapterRead != null && chapterRead.chapter != null && chapterRead.chapter.body != null) {
                                success = mCurBuffer.openNetBookChapter(chapterRead.chapter, chapter);
                            } else {
                                error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                            }
                        }
                        Log.i(TAG, "success = " + success);
                        if (success) {
                            mCurBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                            int curChapterPageCount = mCurBuffer.getPageCount();
                            if (curChapterPageCount > 0) {
                                curPage = mCurBuffer.getPageForReadPos(startPos);
                                pageNumber = getPageNumber(curPage.pageNumber, curChapterPageCount);
                                if (curPage.pageNumber == 0 && chapter == 0 && curChapterPageCount > 1) {
                                    updateNextPage(mCurBuffer.getPageForPos(1), title
                                            , getPageNumber(curPage.pageNumber + 1, curChapterPageCount), error);
                                }
                            }
                        } else {
                            error = Error.NO_CONTENT;
                        }

                        int preChapter = chapter - 1;
                        int nextChapter = chapter + 1;

                    }


                    setChaptersList(curPage, title, pageNumber, error);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void setTextViewParams(int maxLineCount, Paint paint, int width) {
        mMaxLineCount = maxLineCount;
        mPaint = paint;
        mTextViewWidth = width;
    }

    @Override
    public void previous() {

    }

    @Override
    public void next() {

    }

    @Override
    public void destroy() {
        super.destroy();
        mPaint = null;
    }

    private void setChaptersList(final PageContent pageContent, final String chapterTitle, final String pageNumber,
                                 final @ReadPresenter.Error int error) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.initChapterList(mChaptersList, pageContent, chapterTitle, pageNumber, error);
                }
            }
        });
    }

    private void updatePrePage(final PageContent pageContent, final String chapterTitle, final String pageNumber,
                               final @ReadPresenter.Error int error) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdatePrePage(pageContent, chapterTitle, pageNumber, error);
                }
            }
        });
    }

    private void updateNextPage(final PageContent pageContent, final String chapterTitle, final String pageNumber,
                                final @ReadPresenter.Error int error) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdateNextPage(pageContent, chapterTitle, pageNumber, error);
                }
            }
        });
    }

    public static String getPageNumber(int page, int size) {
        if (size > 0) {
            return (page + 1) + "/" + size;
        } else {
            return null;
        }
    }
}
