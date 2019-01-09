package com.xzhou.book.read;

import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.util.LruCache;
import android.util.SparseArray;

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
    private LruCache<Integer, ChapterBuffer> mCacheChapterBuffers;
    private int mMaxLineCount;
    private Paint mPaint;
    private int mTextViewWidth;
    private int mCurPage;
    private int mCurChapter;
    private PageContent[] mOldPageContents;

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
        mCacheChapterBuffers = new LruCache<>();
    }

    @Override
    public boolean start() {
        if (mChaptersList == null) {
            showLoading(0);
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    int[] progress = AppSettings.getReadProgress(mBook._id);
                    mCurChapter = progress[0];
                    mCurPage = progress[2];
                    int readPos = progress[1];
                    mChaptersList = AppSettings.getChapterList(mBook._id);
                    if (mChaptersList == null) {
                        Entities.BookMixAToc mixAToc = ZhuiShuSQApi.getBookMixAToc(mBook._id);
                        if (mixAToc != null && mixAToc.mixToc != null && mixAToc.mixToc.chapters != null) {
                            mChaptersList = mixAToc.mixToc.chapters;
                            AppSettings.saveChapterList(mBook._id, mChaptersList);
                        }
                    }
                    initChaptersList();
                    if (mChaptersList == null || mChaptersList.size() <= 0) {
                        int error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                        showError(0, error);
                        return;
                    }

                    if (mCurChapter < 0 || mCurChapter >= mChaptersList.size()) {
                        mCurChapter = 0;
                        readPos = 0;
                    }
                    Entities.Chapters chapters = mChaptersList.get(mCurChapter);
                    int error = Error.NO_CONTENT;
                    boolean success = false;
                    ChapterBuffer curBuffer = new ChapterBuffer(mBook._id, mCurChapter);
                    mCurBuffer = curBuffer;
                    mChapterBuffers.put(mCurChapter, curBuffer);
                    if (FileUtils.hasCacheChapter(mBook._id, mCurChapter)) {
                        success = mCurBuffer.openCacheBookChapter();
                    } else {
                        Entities.ChapterRead chapterRead = ZhuiShuSQApi.getChapterRead(chapters.link);
                        if (chapterRead != null && chapterRead.chapter != null && chapterRead.chapter.body != null) {
                            success = mCurBuffer.openNetBookChapter(chapterRead.chapter);
                        } else {
                            error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                        }
                    }
                    Log.i(TAG, "chapter load success = " + success);
                    if (success) {
                        error = Error.NONE;
                        mCurBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                        int curChapterPageCount = mCurBuffer.getPageCount();
                        if (curChapterPageCount > 0) {
                            PageLines curPageLine = mCurBuffer.getPageForReadPos(readPos);
                            preparePageContents(mCurChapter, curPageLine, curChapterPageCount);
                            return;
                        }
                    }
                    showError(0, error);
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
    public void loadChapter(int chapter) {

    }

    @Override
    public void reloadCurPage() {
        Log.i(TAG, "reloadCurPage");

    }

    @Override
    public void loadPreviousPage() {
        Log.i(TAG, "loadPreviousPage");

    }

    @Override
    public void loadNextPage() {
        Log.i(TAG, "loadNextPage");

    }

    @Override
    public void destroy() {
        super.destroy();
        mPaint = null;
    }

    private void preparePageContents(int chapter, PageLines curPageLine, int curChapterPageCount) {
        PageContent[] pageContents = new PageContent[3];
        Entities.Chapters curChapters = mChaptersList.get(chapter);
        if (chapter == 0 && curPageLine.page == 0) { //第一章且第一页
            pageContents[0] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            if (curChapterPageCount > 1) {
                PageLines page1 = mCurBuffer.getPageForPos(1);
                pageContents[1] = createNewPageContent(page1, curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount > 2) {
                    pageContents[2] = createNewPageContent(mCurBuffer.getPageForPos(2), curChapters.title, chapter, curChapterPageCount);
                } else {
                    if (hasEndChapter(chapter)) { //当前章节是最后一章，下一页标记为end
                        pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                    }
                }
            } else {
                if (hasEndChapter(chapter)) { //当前章节是最后一章，下一页标记为end
                    pageContents[1] = createNonePageContent(curChapters.title, chapter, true);
                }
            }
        } else {
            if (chapter == 0) { //第一章、非第一页
                pageContents[0] = createNewPageContent(mCurBuffer.getPageForPos(curPageLine.page - 1), curChapters.title, chapter, curChapterPageCount);
                pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount == 2) {
                    if (hasEndChapter(chapter)) { //当前章节为最后一章
                        pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                    } else {
                        pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
                    }
                } else {

                }
            } else {
                if (curPageLine.page == 0) {  //非第一章、第一页
                    pageContents[0] = createNonePageContent(mChaptersList.get(chapter - 1).title, chapter - 1, false);
                    pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
                    if (curChapterPageCount > 1) {
                        pageContents[2] = createShowPageContent(mCurBuffer.getPageForPos(2), curChapters.title, chapter, curChapterPageCount);
                    } else {
                        if (hasEndChapter(chapter)) {

                        } else {

                        }
                    }
                } else {

                }
            }
        }
        updatePages(pageContents);
    }

    private PageContent createErrorPageContent(@Error int error) {
        PageContent pageContent = new PageContent();
        pageContent.bookId = mBook._id;
        pageContent.error = error;
        return pageContent;
    }

    private PageContent createNonePageContent(String title, int chapter, boolean isEnd) {
        PageContent pageContent = new PageContent();
        pageContent.bookId = mBook._id;
        pageContent.chapterTitle = title;
        pageContent.chapter = chapter;
        pageContent.isEnd = isEnd;
        return pageContent;
    }

    private PageContent createShowPageContent(PageLines lines, String title, int chapter, int chapterPages) {
        PageContent pageContent = createNewPageContent(lines, title, chapter, chapterPages);
        pageContent.isShow = true;
        return pageContent;
    }

    private PageContent createNewPageContent(PageLines lines, String title, int chapter, int chapterPages) {
        PageContent pageContent = new PageContent();
        pageContent.bookId = mBook._id;
        pageContent.chapterTitle = title;
        pageContent.mPageLines = lines;
        pageContent.chapter = chapter;
        pageContent.pageSize = chapterPages;
        return pageContent;
    }

    private void showLoading(int page) {
        PageContent[] pageContents = mOldPageContents != null ? mOldPageContents : new PageContent[3];
        for (int i = 0; i < pageContents.length; i++) {
            if (i == page) {
                pageContents[i] = createErrorPageContent(Error.NONE);
                pageContents[i].isLoading = true;
                pageContents[i].isShow = true;
            } else {
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(Error.NONE);
                }
            }
        }
        updatePages(pageContents);
    }

    private void showError(int page, @Error int error) {
        PageContent[] pageContents = mOldPageContents != null ? mOldPageContents : new PageContent[3];
        for (int i = 0; i < pageContents.length; i++) {
            if (i == page) {
                pageContents[i] = createErrorPageContent(error);
                pageContents[i].isLoading = true;
                pageContents[i].isShow = true;
            } else {
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(Error.NONE);
                }
            }
        }
        updatePages(pageContents);
    }

    private boolean hasEndChapter(int chapter) {
        return mChaptersList == null || mChaptersList.size() <= chapter + 1;
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

    private void updatePages(final PageContent[] pageContents) {
        mOldPageContents = pageContents;
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdatePages(pageContents);
                }
            }
        });
    }

    private String getKey(int chapter) {
        return mBook._id + "_" + chapter;
    }
}
