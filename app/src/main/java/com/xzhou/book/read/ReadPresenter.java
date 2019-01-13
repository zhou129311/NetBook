package com.xzhou.book.read;

import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.text.TextUtils;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadPresenter extends BasePresenter<ReadContract.View> implements ReadContract.Presenter {
    private static final String TAG = "ReadPresenter";

    @IntDef({Error.NO_NETWORK, Error.CONNECTION_FAIL, Error.NO_CONTENT, Error.NONE})
    public @interface Error {
        int NO_NETWORK = 0;
        int CONNECTION_FAIL = 1;
        int NO_CONTENT = 2;
        int NONE = 4;
    }

    private ExecutorService mSinglePool = Executors.newSingleThreadExecutor();
    private BookManager.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private LruCache<String, ChapterBuffer> mCacheChapterBuffers = new LruCache<>(3);
    private int mMaxLineCount;
    private Paint mPaint;
    private int mTextViewWidth;
    private int mCurChapter;
    private PageContent[] mOldPageContents;

    ReadPresenter(ReadContract.View view, BookManager.LocalBook book) {
        super(view);
        mBook = book;
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

                    loadReadProgress(0);
                }
            });
            return true;
        }
        return false;
    }

    private void loadReadProgress(int item) {
        loadReadProgress(item, false);
    }

    private void loadReadProgress(int item, boolean isEnd) {
        int[] progress = AppSettings.getReadProgress(mBook._id);
        mCurChapter = progress[0];
        int readPos = progress[1];
        if (mCurChapter < 0 || mCurChapter >= mChaptersList.size()) {
            mCurChapter = 0;
            readPos = 0;
        }

        Log.d(TAG, "loadReadProgress = " + mCurChapter + ",readPos = " + readPos);
        Entities.Chapters chapters = mChaptersList.get(mCurChapter);
        int error = Error.NO_CONTENT;
        boolean success = false;
        ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
        if (curBuffer == null) {
            curBuffer = new ChapterBuffer(mBook._id, mCurChapter);
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
        Log.d(TAG, "chapter load success = " + success);
        if (success) {
            curBuffer.calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
            mCacheChapterBuffers.put(getKey(mCurChapter), curBuffer);
            int curChapterPageCount = curBuffer.getPageCount();
            if (curChapterPageCount > 0) {
                PageLines curPageLine;
                if (isEnd) {
                    curPageLine = curBuffer.getEndPage();
                } else {
                    curPageLine = curBuffer.getPageForReadPos(readPos);
                    Log.d(TAG, "chapter load success = " + curPageLine);
                }
                preparePageContents(curBuffer, mCurChapter, curPageLine, curChapterPageCount);
                return;
            }
        }

        showError(item, error);
    }

    @Override
    public void setTextViewParams(int maxLineCount, Paint paint, int width, final PageLines pageLines) {
        if (mMaxLineCount == maxLineCount) {
            return;
        }
        mMaxLineCount = maxLineCount;
        mPaint = paint;
        mTextViewWidth = width;
        Log.i(TAG, "setTextViewParams::" + mMaxLineCount + "," + mPaint.getTextSize());
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
                for (Map.Entry<String, ChapterBuffer> entry : mCacheChapterBuffers.snapshot().entrySet()) {
                    if (entry != null && entry.getValue() != null) {
                        entry.getValue().calcPageLines(mMaxLineCount, mPaint, mTextViewWidth);
                    }
                }
                if (curBuffer != null && curBuffer.getPageCount() > 0 && pageLines != null) {
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
    public void loadChapter(final int item, final int chapter) {
        Log.i(TAG, "loadChapter::" + chapter);
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                showLoading(item);
                AppSettings.saveReadProgress(mBook._id, chapter, 0);
                loadReadProgress(item);
            }
        });
    }

    @Override
    public void reloadCurPage(final int item, final PageContent pageContent) {
        Log.i(TAG, "reloadCurPage::" + pageContent);
        if (!start()) {
            showLoading(item);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                    if (pageContent.chapter > 0 && !TextUtils.isEmpty(pageContent.chapterTitle)) {
                        AppSettings.saveReadProgress(mBook._id, pageContent.chapter, 0);
                    }
                    loadReadProgress(item);
                }
            });
        }
    }

    @Override
    public void loadPreviousPage(final int item, PageContent pageContent) {
        Log.i(TAG, "loadPreviousPage::item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadPreviousPage::pageContent = null");
        } else if (pageContent.isStart) {
            Log.i(TAG, "loadPreviousPage::pageContent.isStart = true");
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadPreviousPage::pageContent.chapter < 0");
                return;
            }
            mCurChapter = pageContent.chapter;
            ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
            if (curBuffer != null) {
                if (pageContent.mPageLines != null) {
                    preparePageContents(curBuffer, mCurChapter, pageContent.mPageLines, pageContent.pageSize);
                } else {
                    PageLines lines = curBuffer.getEndPage();
                    preparePageContents(curBuffer, mCurChapter, lines, curBuffer.getPageCount());
                }
                return;
            }
            showLoading(item);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    AppSettings.saveReadProgress(mBook._id, mCurChapter, 0);
                    loadReadProgress(item, true);
                }
            });
        }
    }

    @Override
    public void loadNextPage(final int item, PageContent pageContent) {
        Log.i(TAG, "loadNextPage:item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadNextPage::pageContent = null");
        } else if (pageContent.isEnd) {
            Log.i(TAG, "loadNextPage::pageContent.isEnd = true");
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadNextPage::pageContent.chapter < 0 ," + pageContent);
                return;
            }
            mCurChapter = pageContent.chapter;
            ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
            if (curBuffer != null) {
                if (pageContent.mPageLines != null) {
                    preparePageContents(curBuffer, mCurChapter, pageContent.mPageLines, pageContent.pageSize);
                } else {
                    PageLines lines = curBuffer.getPageForPos(0);
                    preparePageContents(curBuffer, mCurChapter, lines, curBuffer.getPageCount());
                }
                return;
            }
            showLoading(item);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    AppSettings.saveReadProgress(mBook._id, mCurChapter, 0);
                    loadReadProgress(item);
                }
            });
        }
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
                pageContents[1] = createNewPageContent(curBuffer.getPageForPos(1), curChapters.title, chapter, curChapterPageCount);
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
        } else if (curPageLine.page > 0) {//非第一章、非第一页
            pageContents[0] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page - 1), curChapters.title, chapter, curChapterPageCount);
            pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            if (curChapterPageCount == 2) { //只有两页
                if (hasEndChapter(chapter)) { //当前章节为最后一章
                    pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                } else {
                    pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
                }
            } else { //大于两页
                if (curChapterPageCount > curPageLine.page + 1) {//当前显示的不是最后一页
                    pageContents[2] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page + 1), curChapters.title, chapter, curChapterPageCount);
                } else {//最后一页
                    Log.i(TAG, "chapter = " + chapter + "," + mChaptersList.size());
                    if (hasEndChapter(chapter)) { //当前章节为最后一章
                        pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                    } else {
                        ChapterBuffer nextBuffer = mCacheChapterBuffers.get(getKey(chapter + 1));
                        if (nextBuffer != null) {
                            pageContents[2] = createNewPageContent(nextBuffer.getPageForPos(0),
                                    mChaptersList.get(chapter + 1).title, chapter + 1, nextBuffer.getPageCount());
                        } else {
                            pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
                        }
                    }
                }
            }
        } else {//非第一章、第一页
            pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            int preChapter = chapter - 1;
            ChapterBuffer preBuffer = mCacheChapterBuffers.get(getKey(preChapter));
            if (preBuffer != null) {
                pageContents[0] = createNewPageContent(preBuffer.getEndPage(), mChaptersList.get(preChapter).title,
                        preChapter, preBuffer.getPageCount());
            } else {
                pageContents[0] = createNonePageContent(mChaptersList.get(preChapter).title, preChapter, false);
            }
            if (curChapterPageCount > 1) { //大于一页
                pageContents[2] = createNewPageContent(curBuffer.getPageForPos(1), curChapters.title, chapter, curChapterPageCount);
            } else { //只有一页
                if (hasEndChapter(chapter)) { //最后一章
                    pageContents[2] = createNonePageContent(curChapters.title, chapter, true);
                } else {
                    pageContents[2] = createNonePageContent(mChaptersList.get(chapter + 1).title, chapter + 1, false);
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
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(Error.NONE);
                }
                pageContents[i].error = Error.NONE;
                pageContents[i].isLoading = true;
                pageContents[i].isShow = true;
            } else {
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(Error.NONE);
                } else {
                    pageContents[i].isLoading = false;
                    pageContents[i].isShow = false;
                }
            }
        }
        PageContent[] newPages = getLoadingOrErrorNewPages(page, pageContents);
        if (newPages != null) {
            pageContents = newPages;
        }
        updatePages(pageContents);
    }

    private void showError(int page, @Error int error) {
        PageContent[] pageContents = mOldPageContents != null ? mOldPageContents : new PageContent[3];
        for (int i = 0; i < pageContents.length; i++) {
            if (i == page) {
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(error);
                    pageContents[i].isShow = true;
                } else {
                    pageContents[i].error = error;
                    pageContents[i].isShow = true;
                }
            } else {
                if (pageContents[i] == null) {
                    pageContents[i] = createErrorPageContent(Error.NONE);
                } else {
                    pageContents[i].isShow = false;
                }
            }
            pageContents[i].isLoading = false;
        }
        PageContent[] newPages = getLoadingOrErrorNewPages(page, pageContents);
        if (newPages != null) {
            pageContents = newPages;
        }
        updatePages(pageContents);
    }

    private PageContent[] getLoadingOrErrorNewPages(int page, PageContent[] pageContents) {
        PageContent[] newPages = null;
        if (mChaptersList != null && mChaptersList.size() > 0) {
            if (page == 0 && mCurChapter > 0) {
                newPages = new PageContent[3];
                int preChapter = mCurChapter - 1;
                ChapterBuffer preBuffer = mCacheChapterBuffers.get(getKey(preChapter));
                if (preBuffer != null) {
                    newPages[0] = createNewPageContent(preBuffer.getEndPage(), mChaptersList.get(preChapter).title, preChapter, preBuffer.getPageCount());
                } else {
                    newPages[0] = createNonePageContent(mChaptersList.get(preChapter).title, preChapter, false);
                }
                newPages[1] = pageContents[0];
                newPages[2] = pageContents[1];
            } else if (page == 2 && mCurChapter + 1 < mChaptersList.size()) {
                newPages = new PageContent[3];
                int nextChapter = mCurChapter + 1;
                ChapterBuffer nextBuffer = mCacheChapterBuffers.get(getKey(nextChapter));
                if (nextBuffer != null) {
                    newPages[2] = createNewPageContent(nextBuffer.getPageForPos(0), mChaptersList.get(nextChapter).title, nextChapter, nextBuffer.getPageCount());
                } else {
                    newPages[2] = createNonePageContent(mChaptersList.get(nextChapter).title, nextChapter, false);
                }
                newPages[1] = pageContents[2];
                newPages[0] = pageContents[1];
            }
        }
        return newPages;
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

    private void updatePages(final PageContent[] pageContents) {
        mOldPageContents = pageContents;
//        int i = 0;
//        for (PageContent pageContent : pageContents) {
//            Log.i(TAG, "updatePages " + i + "::" + pageContent);
//            i++;
//        }
        MyApp.runUI(new Runnable() {
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
