package com.xzhou.book.read;

import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.util.LruCache;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadPresenter extends BasePresenter<ReadContract.View> implements ReadContract.Presenter {
    private static final String TAG = "ReadPresenter";

    @IntDef({ Error.NO_NETWORK, Error.CONNECTION_FAIL, Error.NO_CONTENT, Error.NONE })
    public @interface Error {
        int NO_NETWORK = 0;
        int CONNECTION_FAIL = 1;
        int NO_CONTENT = 2;
        int NONE = 4;
    }

    private ExecutorService mSinglePool = Executors.newSingleThreadExecutor();
    private BookManager.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private LruCache<String, ChapterBuffer> mCacheChapterBuffers;
    private int mMaxLineCount;
    private Paint mPaint;
    private int mTextViewWidth;
    private int mCurChapter;
    private PageContent[] mOldPageContents;

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
        mCacheChapterBuffers = MyApp.getContext().getCacheChapterBuffers();
    }

    @Override
    public boolean start() {
        if (mChaptersList == null) {
            showLoading(0);
            mSinglePool.execute(new Runnable() {
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
                    if (mChaptersList == null || mChaptersList.size() <= 0) {
                        int error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                        showError(0, error);
                        return;
                    }

                    loadReadProgress();
                }
            });
            return true;
        }
        return false;
    }

    private void loadReadProgress() {
        int[] progress = AppSettings.getReadProgress(mBook._id);
        mCurChapter = progress[0];
        int readPos = progress[1];
        if (mCurChapter < 0 || mCurChapter >= mChaptersList.size()) {
            mCurChapter = 0;
            readPos = 0;
        }
        Entities.Chapters chapters = mChaptersList.get(mCurChapter);
        int error = Error.NO_CONTENT;
        boolean success = false;
        ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
        if (curBuffer == null) {
            curBuffer = new ChapterBuffer(mBook._id, mCurChapter);
            mCacheChapterBuffers.put(getKey(mCurChapter), curBuffer);
        }
        if (FileUtils.hasCacheChapter(mBook._id, mCurChapter)) {
            success = curBuffer.openCacheBookChapter();
        } else {
            Entities.ChapterRead chapterRead = ZhuiShuSQApi.getChapterRead(chapters.link);
            if (chapterRead != null && chapterRead.chapter != null && chapterRead.chapter.body != null) {
                success = curBuffer.openNetBookChapter(chapterRead.chapter);
            } else {
                error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
            }
        }
        Log.i(TAG, "chapter load success = " + success);
        if (success) {
            curBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
            int curChapterPageCount = curBuffer.getPageCount();
            if (curChapterPageCount > 0) {
                PageLines curPageLine = curBuffer.getPageForReadPos(readPos);
                preparePageContents(curBuffer, mCurChapter, curPageLine, curChapterPageCount);
                return;
            }
        }
        showError(0, error);
    }

    @Override
    public void setTextViewParams(int maxLineCount, Paint paint, int width, final PageLines pageLines) {
        mMaxLineCount = maxLineCount;
        mPaint = paint;
        mTextViewWidth = width;
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
                if (curBuffer != null && curBuffer.getPageCount() > 0 && pageLines != null) {
                    curBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                    int curChapterPageCount = curBuffer.getPageCount();
                    if (curChapterPageCount > 0) {
                        PageLines curPageLine = curBuffer.getPageForReadPos(pageLines.startPos);
                        preparePageContents(curBuffer, mCurChapter, curPageLine, curChapterPageCount);
                    }
                }
            }
        });
    }

    @Override
    public void loadChapter(final int item,final int chapter) {
        Log.i(TAG, "loadChapter::" + chapter);
        showLoading(item);
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                AppSettings.saveReadProgress(mBook._id, chapter, 0);
                loadReadProgress();
            }
        });
    }

    @Override
    public void reloadCurPage(int item) {
        Log.i(TAG, "reloadCurPage");
        if (!start()) {
            showLoading(item);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    loadReadProgress();
                }
            });
        }
    }

    @Override
    public void loadPreviousPage(int item) {
        Log.i(TAG, "loadPreviousPage");

    }

    @Override
    public void loadNextPage(int item) {
        Log.i(TAG, "loadNextPage");

    }

    @Override
    public void destroy() {
        super.destroy();
        mPaint = null;
    }

    private void preparePageContents(ChapterBuffer curBuffer, int chapter, PageLines curPageLine, int curChapterPageCount) {
        PageContent[] pageContents = new PageContent[3];
        Entities.Chapters curChapters = mChaptersList.get(chapter);
        if (chapter == 0 && curPageLine.page == 0) { //第一章且第一页
            pageContents[0] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            pageContents[0].isStart = true;
            if (curChapterPageCount > 1) {
                PageLines page1 = curBuffer.getPageForPos(1);
                pageContents[1] = createNewPageContent(page1, curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount > 2) {
                    pageContents[2] = createNewPageContent(curBuffer.getPageForPos(2), curChapters.title, chapter, curChapterPageCount);
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
            if (curPageLine.page > 0) { //非第一页
                pageContents[0] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page - 1), curChapters.title, chapter, curChapterPageCount);
                pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount == 2) { //只有两页
                    if (hasEndChapter(chapter)) { //当前章节为最后一章
                        pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                    } else {
                        pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
                    }
                } else { //大于两页
                    pageContents[2] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page + 1), curChapters.title, chapter, curChapterPageCount);
                }
            } else {//非第一章、第一页
                pageContents[0] = createNonePageContent(mChaptersList.get(chapter - 1).title, chapter - 1, false);
                pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount > 1) { //大于一页
                    pageContents[2] = createShowPageContent(curBuffer.getPageForPos(2), curChapters.title, chapter, curChapterPageCount);
                } else { //只有一页
                    if (hasEndChapter(chapter)) { //最后一章
                        pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                    } else {
                        pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
                    }
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
