package com.xzhou.book.read;

import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.LruCache;

import com.xzhou.book.DownloadManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private LruCache<String, ChapterBuffer> mCacheChapterBuffers = new LruCache<>(3);
    private int mMaxLineCount;
    private Paint mPaint;
    private int mTextViewWidth;
    private int mCurChapter;

    ReadPresenter(ReadContract.View view, BookProvider.LocalBook book) {
        super(view);
        mBook = book;
    }

    @Override
    public boolean start() {
        if (mChaptersList == null) {
            final int[] progress = AppSettings.getReadProgress(mBook._id);
            mCurChapter = progress[0];
            final int newItem = showLoading((mCurChapter == 0 && progress[1] == 0) ? 0 : 1, null);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    if (mBook.isBookshelf()) {
                        BookProvider.updateReadTime(mBook);
                    }
                    mChaptersList = AppSettings.getChapterList(mBook._id);
                    if (mChaptersList == null) {
                        if (mBook.isBaiduBook) {
                            HtmlParse htmlParse = HtmlParseFactory.getHtmlParse(mBook.curSourceHost);
                            if (htmlParse != null) {
                                mChaptersList = htmlParse.parseChapters(mBook.readUrl);
                            }
                        } else {
                            List<Entities.BookSource> list = ZhuiShuSQApi.getBookSource(mBook._id);
                            if (list != null) {
                                for (Entities.BookSource source : list) {
                                    if (source != null && source.host != null && source.host.equals("book.my716.com")) {
                                        mBook.curSourceHost = source.host;
                                        mBook.sourceId = source._id;
                                        if (mBook.isBookshelf()) {
                                            BookProvider.insertOrUpdate(mBook, true);
                                        }
                                        break;
                                    }
                                }
                            }
                            Entities.BookAToc aToc = ZhuiShuSQApi.getBookMixAToc(mBook._id, mBook.sourceId);
                            if (aToc != null && aToc.chapters != null) {
                                mChaptersList = aToc.chapters;
                            }
                        }
                        if (mChaptersList != null && mBook.isBookshelf()) {
                            for (int i = 0, size = mChaptersList.size(); i < size; i++) {
                                mChaptersList.get(i).hasLocal = FileUtils.hasCacheChapter(mBook._id, i);
                            }
                            AppSettings.saveChapterList(mBook._id, mChaptersList);
                        }
                    }
                    initChaptersList();
                    if (mChaptersList == null || mChaptersList.size() <= 0) {
                        int error = AppUtils.isNetworkAvailable() ? Error.CONNECTION_FAIL : Error.NO_NETWORK;
                        showError(0, error, null);
                        return;
                    }

                    if (mCurChapter < 0) {
                        mCurChapter = 0;
                        progress[1] = 0;
                    } else if (mCurChapter >= mChaptersList.size()) {
                        mCurChapter = mChaptersList.size() - 1;
                        progress[1] = -1;
                    }
                    loadReadProgress(mCurChapter, progress[1], newItem, false);
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
        loadReadProgress(progress[0], progress[1], item, isEnd);
    }

    private void loadReadProgress(int chapter, int readPos, int item, boolean isEnd) {
        mCurChapter = chapter;
        if (mCurChapter < 0) {
            mCurChapter = 0;
            readPos = 0;
        } else if (mCurChapter >= mChaptersList.size()) {
            mCurChapter = mChaptersList.size() - 1;
            readPos = -1;
        }

        Log.d(TAG, "loadReadProgress = " + mCurChapter + ",readPos = " + readPos);
        Entities.Chapters chapters = mChaptersList.get(mCurChapter);
        int error = Error.NO_CONTENT;
        boolean success = false;
        ChapterBuffer curBuffer = mCacheChapterBuffers.get(getKey(mCurChapter));
        if (curBuffer == null) {
            curBuffer = new ChapterBuffer(mBook._id, mCurChapter);
        }
        //加入书架才可以边看边存
        boolean saveCurChapter = false;
        if (mBook.isBookshelf()) {
            int cacheMode = AppSettings.getReadCacheMode();
            DownloadManager.Download download = null;
            switch (cacheMode) {
            case AppSettings.PRE_VALUE_READ_CACHE_1:
                saveCurChapter = true;
                break;
            case AppSettings.PRE_VALUE_READ_CACHE_5:
                saveCurChapter = true;
                download = DownloadManager.createReadCacheDownload(mCurChapter, 5, mChaptersList);
                break;
            case AppSettings.PRE_VALUE_READ_CACHE_10:
                saveCurChapter = true;
                download = DownloadManager.createReadCacheDownload(mCurChapter, 10, mChaptersList);
                break;
            }
            if (download != null) {
                DownloadManager.get().startDownload(mBook._id, download, false);
            }
        }

        if (FileUtils.hasCacheChapter(mBook._id, mCurChapter)) {
            chapters.hasLocal = true;
            success = curBuffer.openCacheBookChapter();
        } else {
            Entities.ChapterRead chapterRead = null;
            if (mBook.isBaiduBook) {
                HtmlParse htmlParse = HtmlParseFactory.getHtmlParse(mBook.curSourceHost);
                if (htmlParse != null) {
                    chapterRead = htmlParse.parseChapterRead(chapters.link);
                }
            } else {
                chapterRead = ZhuiShuSQApi.getChapterRead(chapters.link);
            }
            if (chapterRead != null && chapterRead.chapter != null && chapterRead.chapter.body != null) {
                success = curBuffer.openNetBookChapter(chapterRead.chapter, saveCurChapter);
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
        PageContent pageContent = createNonePageContent(chapters.title, mCurChapter, hasEndChapter(mCurChapter));
        if (mCurChapter == 0) {
            pageContent.isStart = true;
        }
        showError(item, error, pageContent);
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
        if (chapter < 0 || mChaptersList == null || mChaptersList.size() < chapter + 1) {
            Log.e(TAG, "loadChapter error");
            return;
        }
        ChapterBuffer buffer = mCacheChapterBuffers.get(getKey(chapter));
        if (chapter == mCurChapter && buffer != null) {
            preparePageContents(buffer, chapter, buffer.getPageForPos(0), buffer.getPageCount());
            return;
        }
        mCurChapter = chapter;
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                showLoadChapterPage(item, chapter);
                AppSettings.saveReadProgress(mBook._id, chapter, 0);
                loadReadProgress(item);
            }
        });
    }

    private void showLoadChapterPage(int item, int chapter) {
        PageContent[] newPages = new PageContent[3];
        int preChapter = chapter - 1;
        int nextChapter = chapter + 1;
        if (chapter == 0) { //加载第一章
            newPages[0] = createNonePageContent(mChaptersList.get(chapter).title, chapter, false);
            newPages[0].isStart = true;
            newPages[0].isShow = true;
            newPages[0].isLoading = true;
            if (hasEndChapter(chapter)) {
                newPages[0].isEnd = true;
            } else {
                ChapterBuffer nextBuffer = mCacheChapterBuffers.get(getKey(nextChapter));
                if (nextBuffer != null) {
                    int nextChapterPageCount = nextBuffer.getPageCount();
                    newPages[1] = createNewPageContent(nextBuffer.getPageForPos(0), mChaptersList.get(nextChapter).title, nextChapter, nextChapterPageCount);
                    if (nextChapterPageCount == 1) {
                        newPages[1].isEnd = true;
                    } else {
                        newPages[2] = createNewPageContent(nextBuffer.getPageForPos(1), mChaptersList.get(nextChapter).title, nextChapter, nextChapterPageCount);
                    }
                } else {
                    newPages[1] = createNonePageContent(mChaptersList.get(nextChapter).title, nextChapter, hasEndChapter(nextChapter));
                }
            }
        } else if (chapter + 1 >= mChaptersList.size()) { //加载最后一章
            ChapterBuffer preBuffer = mCacheChapterBuffers.get(getKey(preChapter));
            if (preBuffer != null) {
                newPages[0] = createNewPageContent(preBuffer.getEndPage(), mChaptersList.get(preChapter).title, preChapter, preBuffer.getPageCount());
            } else {
                newPages[0] = createNonePageContent(mChaptersList.get(preChapter).title, preChapter, false);
            }
            if (preChapter == 0) {
                newPages[0].isStart = true;
            }
            newPages[1] = createNonePageContent(mChaptersList.get(chapter).title, chapter, true);
            newPages[1].isShow = true;
            newPages[1].isLoading = true;
        } else {
            ChapterBuffer preBuffer = mCacheChapterBuffers.get(getKey(preChapter));
            if (preBuffer != null) {
                newPages[0] = createNewPageContent(preBuffer.getEndPage(), mChaptersList.get(preChapter).title, preChapter, preBuffer.getPageCount());
            } else {
                newPages[0] = createNonePageContent(mChaptersList.get(preChapter).title, preChapter, false);
            }
            if (preChapter == 0) {
                newPages[0].isStart = true;
            }

            newPages[1] = createNonePageContent(mChaptersList.get(chapter).title, chapter, false);
            newPages[1].isShow = true;
            newPages[1].isLoading = true;

            ChapterBuffer nextBuffer = mCacheChapterBuffers.get(getKey(nextChapter));
            if (nextBuffer != null) {
                int nextChapterPageCount = nextBuffer.getPageCount();
                newPages[2] = createNewPageContent(nextBuffer.getPageForPos(0), mChaptersList.get(nextChapter).title, nextChapter, nextChapterPageCount);
                newPages[2].isEnd = (nextChapterPageCount == 1);
            } else {
                newPages[2] = createNonePageContent(mChaptersList.get(nextChapter).title, nextChapter, hasEndChapter(nextChapter));
            }
        }
        updatePages(newPages);
    }

    @Override
    public void reloadCurPage(final int item, final PageContent pageContent) {
        Log.i(TAG, "reloadCurPage::" + pageContent);
        if (!start()) {
            final int newItem = showLoading(item, pageContent);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    if (pageContent.chapter > 0 && !TextUtils.isEmpty(pageContent.chapterTitle)) {
                        AppSettings.saveReadProgress(mBook._id, pageContent.chapter, 0);
                    }
                    loadReadProgress(newItem);
                }
            });
        }
    }

    @Override
    public void loadPreviousPage(final int item, PageContent pageContent) {
        Log.i(TAG, "loadPreviousPage::item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadPreviousPage::pageContent = null");
            showError(item, Error.NO_CONTENT, pageContent);
        } else if (pageContent.isStart && pageContent.mPageLines != null && pageContent.mPageLines.page == 0) {
            Log.i(TAG, "loadPreviousPage::pageContent.isStart = true");
            showError(item, Error.NO_CONTENT, pageContent);
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadPreviousPage::pageContent.chapter < 0:" + pageContent);
                showError(item, Error.NO_CONTENT, pageContent);
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
            final int newItem = showLoading(item, pageContent);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    AppSettings.saveReadProgress(mBook._id, mCurChapter, 0);
                    loadReadProgress(newItem, true);
                }
            });
        }
    }

    @Override
    public void loadNextPage(final int item, PageContent pageContent) {
        Log.i(TAG, "loadNextPage:item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadNextPage::pageContent = null");
            showError(item, Error.NO_CONTENT, pageContent);
        } else if (pageContent.isEnd && pageContent.mPageLines != null && pageContent.mPageLines.page == pageContent.pageSize) {
            Log.i(TAG, "loadNextPage::pageContent.isEnd = true");
            showError(item, Error.NO_CONTENT, pageContent);
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadNextPage::pageContent.chapter < 0 ," + pageContent);
                showError(item, Error.NO_CONTENT, pageContent);
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
            final int newItem = showLoading(item, pageContent);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    AppSettings.saveReadProgress(mBook._id, mCurChapter, 0);
                    loadReadProgress(newItem);
                }
            });
        }
    }

    @Override
    public void loadAllSource() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.BookSource> list = ZhuiShuSQApi.getBookSource(mBook._id);
                if (list != null) {
                    Iterator<Entities.BookSource> iter = list.iterator();
                    while (iter.hasNext()) {
                        Entities.BookSource source = iter.next();
                        if (ZhuiShuSQApi.IGNORE_HOST.equals(source.host)) {
                            iter.remove();
                        }
                    }
                    updateBookSource(list);
                }
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        mPaint = null;
    }

    private void preparePageContents(ChapterBuffer curBuffer, int chapter, PageLines curPageLine, int curChapterPageCount) {
        PageContent[] pageContents = new PageContent[3];

        Entities.Chapters curChapters = mChaptersList.get(chapter);
        Entities.Chapters nextChapters = null;
        ChapterBuffer nextBuffer = null;
        int nextChapter = chapter + 1;
        if (!hasEndChapter(chapter)) {
            nextChapters = mChaptersList.get(nextChapter);
            nextBuffer = mCacheChapterBuffers.get(getKey(nextChapter));
        }

        if (chapter == 0 && curPageLine.page == 0) { //第一章且第一页
            pageContents[0] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            pageContents[0].isStart = true;
            if (curChapterPageCount == 1) {//只有一页
                if (nextChapters == null) { //当前章节为最后一章
                    pageContents[0].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[1] = createNewPageContent(nextBuffer.getPageForPos(0), nextChapters.title, nextChapter, nextBuffer.getPageCount());
                } else {
                    pageContents[1] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                }
            } else {
                pageContents[1] = createNewPageContent(curBuffer.getPageForPos(1), curChapters.title, chapter, curChapterPageCount);
                if (curChapterPageCount > 2) {
                    pageContents[2] = createNewPageContent(curBuffer.getPageForPos(2), curChapters.title, chapter, curChapterPageCount);
                } else {
                    if (nextChapters == null) { //当前章节为最后一章
                        pageContents[1].isEnd = true;
                    } else if (nextBuffer != null) {
                        pageContents[2] = createNewPageContent(nextBuffer.getPageForPos(0), nextChapters.title, nextChapter, nextBuffer.getPageCount());
                    } else {
                        pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                    }
                }
            }
        } else if (curPageLine.page > 0) {//非第一章、非第一页
            pageContents[0] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page - 1), curChapters.title, chapter, curChapterPageCount);
            pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            if (curChapterPageCount == 2) { //只有两页
                if (nextChapters == null) {
                    pageContents[1].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[2] = createNewPageContent(nextBuffer.getPageForPos(0), nextChapters.title, nextChapter, nextBuffer.getPageCount());
                } else {
                    pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                }
            } else { //大于两页
                if (curChapterPageCount > curPageLine.page + 1) {//当前显示的不是最后一页
                    pageContents[2] = createNewPageContent(curBuffer.getPageForPos(curPageLine.page + 1), curChapters.title, chapter, curChapterPageCount);
                } else {//最后一页
                    if (nextChapters == null) { //当前章节为最后一章
                        pageContents[1].isEnd = true;
                    } else if (nextBuffer != null) {
                        pageContents[2] = createNewPageContent(nextBuffer.getPageForPos(0), nextChapters.title, nextChapter, nextBuffer.getPageCount());
                    } else {
                        pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                    }
                }
            }
        } else {//非第一章、第一页
            int preChapter = chapter - 1;
            Entities.Chapters preChapters = mChaptersList.get(preChapter);
            ChapterBuffer preBuffer = mCacheChapterBuffers.get(getKey(preChapter));
            pageContents[1] = createShowPageContent(curPageLine, curChapters.title, chapter, curChapterPageCount);
            if (preBuffer != null) {
                pageContents[0] = createNewPageContent(preBuffer.getEndPage(), preChapters.title, preChapter, preBuffer.getPageCount());
            } else {
                pageContents[0] = createNonePageContent(preChapters.title, preChapter, false);
            }
            if (curChapterPageCount > 1) { //大于一页
                pageContents[2] = createNewPageContent(curBuffer.getPageForPos(1), curChapters.title, chapter, curChapterPageCount);
            } else { //只有一页
                if (nextChapters == null) { //当前章节为最后一章
                    pageContents[1].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[2] = createNewPageContent(nextBuffer.getPageForPos(0), nextChapters.title, nextChapter, nextBuffer.getPageCount());
                } else {
                    pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
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

    private int showLoading(int page, PageContent showPageContent) {
        if (showPageContent == null) {
            showPageContent = createErrorPageContent(Error.NONE);
        }
        showPageContent.error = Error.NONE;
        showPageContent.isLoading = true;
        showPageContent.isShow = true;
        PageContent[] pageContents = getLoadingOrErrorNewPages(page, showPageContent);
        int newShowPage = page;
        for (int i = 0; i < pageContents.length; i++) {
            if (pageContents[i] != null && pageContents[i].isShow) {
                newShowPage = i;
                break;
            }
        }
        updatePages(pageContents);
        return newShowPage;
    }

    private void showError(int page, @Error int error, PageContent showPageContent) {
        if (showPageContent == null) {
            showPageContent = createErrorPageContent(error);
        }
        showPageContent.isLoading = false;
        showPageContent.isShow = true;
        PageContent[] pageContents = getLoadingOrErrorNewPages(page, showPageContent);
        updatePages(pageContents);
    }

    private PageContent[] getLoadingOrErrorNewPages(int page, PageContent showPageContent) {
        PageContent[] newPages = new PageContent[3];
        if (mChaptersList != null && mChaptersList.size() > 0) {
            if (mCurChapter == 0) {
                newPages[0] = showPageContent;
                newPages[0].isEnd = hasEndChapter(0);
                newPages[0].isStart = true;
                if (!newPages[0].isEnd) {
                    newPages[1] = getPageContent(1, true);
                    if (!newPages[1].isEnd) {
                        newPages[2] = getPageContent(2, true);
                    }
                }
            } else if (!hasEndChapter(mCurChapter)) {
                newPages[0] = getPageContent(mCurChapter - 1, false);
                newPages[1] = showPageContent;
                newPages[2] = getPageContent(mCurChapter + 1, true);
            } else {
                if (mCurChapter > 1) {
                    newPages[0] = getPageContent(mCurChapter - 2, false);
                    newPages[1] = getPageContent(mCurChapter - 1, false);
                } else {
                    newPages[1] = getPageContent(mCurChapter - 1, false);
                }
                newPages[2] = showPageContent;
                newPages[2].isEnd = true;
            }
        } else {
            newPages[page] = showPageContent;
        }
        return newPages;
    }

    private PageContent getPageContent(int chapter, boolean isNext) {
        PageContent pageContent;
        Entities.Chapters chapters = mChaptersList.get(chapter);
        ChapterBuffer chapterBuffer = mCacheChapterBuffers.get(getKey(chapter));
        if (chapterBuffer != null) {
            pageContent = createNewPageContent(isNext ? chapterBuffer.getPageForPos(0) : chapterBuffer.getEndPage(),
                    chapters.title, chapter, chapterBuffer.getPageCount());
        } else {
            pageContent = createNonePageContent(chapters.title, chapter, hasEndChapter(chapter));
        }
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

    private void updateBookSource(final List<Entities.BookSource> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdateSource(list);
                }
            }
        });
    }

    private void updatePages(final PageContent[] pageContents) {
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
