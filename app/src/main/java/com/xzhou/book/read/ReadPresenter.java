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

    @IntDef({ Error.NO_NETWORK, Error.CONNECTION_FAIL, Error.NO_CONTENT, Error.NONE })
    public @interface Error {
        int NO_NETWORK = 0;
        int CONNECTION_FAIL = 1;
        int NO_CONTENT = 2;
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
    private PageContent[] mPageContents = new PageContent[3];
    private int mCurPage;
    private int mCurChapter;

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
//        for (int i = 0; i < mPageContents.length; i++) {
//            mPageContents[i] = new PageContent();
//            mPageContents[i].bookId = mBook._id;
//        }
        mCurBuffer = new ChapterBuffer(mBook._id);
        mPreBuffer = new ChapterBuffer(mBook._id);
        mNextBuffer = new ChapterBuffer(mBook._id);
    }

    @Override
    public boolean start() {
        final int[] progress = AppSettings.getReadProgress(mBook._id);
        mCurChapter = progress[0];
        mCurPage = progress[2];
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
                    initChaptersList();
                    int readPos = progress[1];
                    if (mChaptersList != null) {
                        int chapterSize = mChaptersList.size();
                        if (mCurChapter < 0 || mCurChapter > chapterSize) {
                            mCurChapter = 0;
                            readPos = 0;
                        }
                        Entities.Chapters chapters = mChaptersList.get(mCurChapter);
                        if (mCurChapter == 0) {
                        }
                        int error = Error.NO_CONTENT;
                        boolean success = false;
                        if (FileUtils.hasCacheChapter(mBook._id, mCurChapter)) {
                            success = mCurBuffer.openCacheBookChapter(mCurChapter);
                        } else {
                            Entities.ChapterRead chapterRead = ZhuiShuSQApi.getChapterRead(chapters.link);
                            if (chapterRead != null && chapterRead.chapter != null && chapterRead.chapter.body != null) {
                                success = mCurBuffer.openNetBookChapter(chapterRead.chapter, mCurChapter);
                            } else {
                                error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                            }
                        }
                        Log.i(TAG, "chapter load success = " + success);
                        if (success) {
                            mCurBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                            int curChapterPageCount = mCurBuffer.getPageCount();
                            if (curChapterPageCount > 0) {
                                PageLines pageLines = mCurBuffer.getPageForReadPos(readPos);
//                                if (pageLines.pageNumber == 0 && curChapterPageCount > 1) {
//
//                                }
//                                pageNumber = getStringPageNumber(curPage.pageNumber, curChapterPageCount);
//                                if (curPage.pageNumber == 0 && chapter == 0 && curChapterPageCount > 1) {
//                                    updateNextPage(mCurBuffer.getPageForPos(1), title
//                                            , getStringPageNumber(curPage.pageNumber + 1, curChapterPageCount), error);
//                                }
                            }
                        } else {
                            error = Error.NO_CONTENT;
                        }

                    }
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
    public void loadPreviousPage() {

    }

    @Override
    public void loadNextPage() {

    }

    @Override
    public void destroy() {
        super.destroy();
        mPaint = null;
    }

    private void preparePageContents(int curChapter, int readPos) {

    }

    private PageContent createNewPageContent(PageLines lines, String title, int chapter, int chapterPages) {
        PageContent pageContent = new PageContent();
        pageContent.bookId = mBook._id;
        pageContent.chapterTitle = title;
        pageContent.mPageLines = lines;
        pageContent.chapter = chapter;
        pageContent.curPagePos = getStringPageNumber(lines.page, chapterPages);
        return pageContent;
    }

    private void initChaptersList() {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.initChapterList(mChaptersList);
                }
            }
        });
    }

    private void updatePages() {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdatePages(mPageContents);
                }
            }
        });
    }

    private static String getStringPageNumber(int page, int size) {
        if (size > 0) {
            return (page + 1) + "/" + size;
        } else {
            return null;
        }
    }
}
