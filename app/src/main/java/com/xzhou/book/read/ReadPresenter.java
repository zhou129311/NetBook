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

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
        for (int i = 0; i < mPageContents.length; i++) {
            mPageContents[i] = new PageContent();
            mPageContents[i].bookId = mBook._id;
        }
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
                    initChaptersList();

                    if (mChaptersList != null) {
                        int chapterSize = mChaptersList.size();
                        int[] progress = AppSettings.getReadProgress(mBook._id);
                        int chapter = progress[0], startPos = progress[1];
                        if (chapter < 0 || chapter > chapterSize) {
                            chapter = 0;
                            startPos = 0;
                        }
                        Entities.Chapters chapters = mChaptersList.get(chapter);
                        if (chapter == 0) {
                            mPageContents[0].clear();
                            mPageContents[0].isLoading = true;
                            mPageContents[0].isShow = true;
                            mPageContents[0].isStart = true;
                            mPageContents[0].chapterTitle = chapters.title;
                            if (chapterSize > 1) {
                                mPageContents[1].clear();
                                mPageContents[1].chapterTitle = mChaptersList.get(1).title;
                                mPageContents[1].isEnd = chapterSize == 2;
                            }
                        }
                        int error = Error.NO_CONTENT;
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
                        Log.i(TAG, "chapter load success = " + success);
                        if (success) {
                            mCurBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                            int curChapterPageCount = mCurBuffer.getPageCount();
                            if (curChapterPageCount > 0) {
                                PageLines pageLines = mCurBuffer.getPageForReadPos(startPos);
//                                if (pageLines.pageNumber == 0 && curChapterPageCount > 1) {
//
//                                }
//                                pageNumber = getPageNumber(curPage.pageNumber, curChapterPageCount);
//                                if (curPage.pageNumber == 0 && chapter == 0 && curChapterPageCount > 1) {
//                                    updateNextPage(mCurBuffer.getPageForPos(1), title
//                                            , getPageNumber(curPage.pageNumber + 1, curChapterPageCount), error);
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

    private void preparePageContents(int curChapter, int readPos) {

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

    private static String getPageNumber(int page, int size) {
        if (size > 0) {
            return (page + 1) + "/" + size;
        } else {
            return null;
        }
    }
}
