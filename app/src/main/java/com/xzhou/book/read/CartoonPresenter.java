package com.xzhou.book.read;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartoonPresenter extends BasePresenter<CartoonContract.View> implements CartoonContract.Presenter {
    private static final String TAG = "CartoonPresenter";

    private ExecutorService mSinglePool = Executors.newSingleThreadExecutor();
    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;

    private LruCache<String, ChapterBufferImage> mCacheChapterBuffers = new LruCache<>(3);
    private LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(50 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private int mCurChapter;

    public CartoonPresenter(CartoonContract.View view, BookProvider.LocalBook book) {
        super(view);
        mBook = book;
    }

    @Override
    public boolean start() {
        if (mChaptersList == null || mChaptersList.size() <= 0) {
            mChaptersList = null;
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
                                    chapters.hasLocal = FileUtils.hasPicCacheChapter(mBook._id, i);
                                }
                                AppSettings.saveChapterList(mBook._id, mChaptersList);
                            }
                        }).start();
                    }
                    initChaptersList();
                    if (mChaptersList == null || mChaptersList.size() <= 0) {
                        int error = AppUtils.isNetworkAvailable() ? ReadPresenter.Error.CONNECTION_FAIL : ReadPresenter.Error.NO_NETWORK;
                        showError(0, error, null);
                        return;
                    }
                    loadReadProgress(mCurChapter, progress[1], newItem, false);
                }
            });
            return true;
        }
        return super.start();
    }

    @Override
    public void loadChapter(int itemPosition, final int chapter) {
        Log.i(TAG, "loadChapter::" + chapter);
        if (chapter < 0 || mChaptersList == null || mChaptersList.size() < chapter + 1) {
            Log.e(TAG, "loadChapter error");
            start();
            return;
        }
        ChapterBufferImage buffer = mCacheChapterBuffers.get(String.valueOf(chapter));
        if (chapter == mCurChapter && buffer != null) {
            preparePageContents(buffer, chapter, 0, buffer.getPageCount());
            return;
        }
        mCurChapter = chapter;
        final int newItem = showLoading(itemPosition, null);
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                AppSettings.saveReadProgress(mBook._id, chapter, 0);
                loadReadProgress(newItem);
            }
        });
    }

    @Override
    public void reloadCurPage(int itemPosition, final CartoonContent pageContent) {
        Log.i(TAG, "reloadCurPage::" + pageContent);
        if (!start()) {
            final int newItem = showLoading(itemPosition, pageContent);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    if (pageContent.chapter > 0 && !TextUtils.isEmpty(pageContent.title)) {
                        AppSettings.saveReadProgress(mBook._id, pageContent.chapter, 0);
                    }
                    loadReadProgress(newItem);
                }
            });
        }
    }

    @Override
    public void loadPreviousPage(int item, CartoonContent pageContent) {
        Log.i(TAG, "loadPreviousPage::item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadPreviousPage::pageContent = null");
            showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
        } else if (pageContent.isStart && pageContent.curPage == 0) {
            Log.i(TAG, "loadPreviousPage::pageContent.isStart = true");
//            showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadPreviousPage::pageContent.chapter < 0:" + pageContent);
                showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
                return;
            }
            mCurChapter = pageContent.chapter;
            ChapterBufferImage curBuffer = mCacheChapterBuffers.get(String.valueOf(mCurChapter));
            if (curBuffer != null) {
                if (pageContent.bitmap != null) {
                    preparePageContents(curBuffer, mCurChapter, pageContent.curPage, pageContent.totalPage);
                } else {
                    preparePageContents(curBuffer, mCurChapter, curBuffer.getEndPos(), curBuffer.getPageCount());
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
    public void loadNextPage(int item, CartoonContent pageContent) {
        Log.i(TAG, "loadNextPage:item = " + item);
        if (pageContent == null) {
            Log.e(TAG, "loadNextPage::pageContent = null");
            showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
        } else if (pageContent.isEnd && pageContent.bitmap != null && pageContent.curPage >= pageContent.totalPage - 1) {
            Log.i(TAG, "loadNextPage::pageContent.isEnd = true");
//            showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
        } else {
            if (pageContent.chapter < 0) {
                Log.e(TAG, "loadNextPage::pageContent.chapter < 0 ," + pageContent);
                showError(item, ReadPresenter.Error.NO_CONTENT, pageContent);
                return;
            }
            mCurChapter = pageContent.chapter;
            ChapterBufferImage curBuffer = mCacheChapterBuffers.get(String.valueOf(mCurChapter));
            if (curBuffer != null) {
                if (pageContent.url != null) {
                    preparePageContents(curBuffer, mCurChapter, pageContent.curPage, pageContent.totalPage);
                } else {
                    preparePageContents(curBuffer, mCurChapter, 0, curBuffer.getPageCount());
                }
                return;
            }
            final int newItem = showLoading(item, pageContent);
            Log.i(TAG, "loadNextPage:newItem = " + newItem);
            mSinglePool.execute(new Runnable() {
                @Override
                public void run() {
                    AppSettings.saveReadProgress(mBook._id, mCurChapter, 0);
                    loadReadProgress(newItem);
                }
            });
        }
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
        int error = ReadPresenter.Error.NO_CONTENT;
        boolean success = false;
        ChapterBufferImage curBuffer = mCacheChapterBuffers.get(String.valueOf(mCurChapter));
        if (curBuffer == null) {
            curBuffer = new ChapterBufferImage(mBook._id, mCurChapter, mImageCache);
        }
        //加入书架才可以边看边存
        boolean saveCurChapter = false;
//        if (mBook.isBookshelf()) {
//            int cacheMode = AppSettings.getReadCacheMode();
//            DownloadManager.Download download = null;
//            switch (cacheMode) {
//            case AppSettings.PRE_VALUE_READ_CACHE_1:
//                saveCurChapter = true;
//                break;
//            case AppSettings.PRE_VALUE_READ_CACHE_5:
//                saveCurChapter = true;
//                download = DownloadManager.createReadCacheDownload(mCurChapter, 5, mChaptersList);
//                break;
//            case AppSettings.PRE_VALUE_READ_CACHE_10:
//                saveCurChapter = true;
//                download = DownloadManager.createReadCacheDownload(mCurChapter, 10, mChaptersList);
//                break;
//            }
//            if (download != null) {
//                download.isNotify = false;
//                DownloadManager.get().startDownload(mBook._id, download);
//            }
//        }
        boolean hasCache = FileUtils.hasPicCacheChapter(mBook._id, mCurChapter);
        Log.i(TAG, "mCurChapter =" + mCurChapter + ",hasCache = " + hasCache);
        if (hasCache) {
            chapters.hasLocal = true;
            success = curBuffer.openCacheBookChapter();
        } else {
            Entities.ChapterRead read = ZhuiShuSQApi.getPictureChapterRead(chapters.link);
            if (read != null && read.chapter != null && read.chapter.getImages().size() > 0) {
                success = curBuffer.openNetBookChapter(read.chapter, saveCurChapter);
            }
            if (!success) {
                error = AppUtils.isNetworkAvailable() ? ReadPresenter.Error.CONNECTION_FAIL : ReadPresenter.Error.NO_NETWORK;
            }
        }
        Log.d(TAG, "chapter load success = " + success);
        if (success) {
            mCacheChapterBuffers.put(String.valueOf(mCurChapter), curBuffer);
            if (isEnd) {
                readPos = curBuffer.getEndPos();
            }
            Log.d(TAG, "chapter load success, cur readPos=" + readPos);
            preparePageContents(curBuffer, mCurChapter, readPos, curBuffer.getPageCount());
            return;
        }
        Log.d(TAG, "chapter load error = " + error);
        CartoonContent pageContent = createNonePageContent(chapters.title, mCurChapter, hasEndChapter(mCurChapter));
        showError(item, error, pageContent);
    }

    private void preparePageContents(ChapterBufferImage curBuffer, int chapter, int curPage, int totalPage) {
        CartoonContent[] pageContents = new CartoonContent[3];

        Entities.Chapters curChapters = mChaptersList.get(chapter);
        Entities.Chapters nextChapters = null;
        ChapterBufferImage nextBuffer = null;
        int nextChapter = chapter + 1;
        if (!hasEndChapter(chapter)) {
            nextChapters = mChaptersList.get(nextChapter);
            nextBuffer = mCacheChapterBuffers.get(String.valueOf(nextChapter));
        }

        if (chapter == 0 && curPage == 0) { //第一章且第一页
            pageContents[0] = createShowPageContent(curBuffer, curPage, curChapters.title);
            pageContents[0].isStart = true;
            if (totalPage == 1) {//只有一页
                if (nextChapters == null) { //当前章节为最后一章
                    pageContents[0].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[1] = createNewPageContent(nextBuffer, 0, nextChapters.title);
                } else {
                    pageContents[1] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                }
            } else {
                pageContents[1] = createNewPageContent(curBuffer, 1, curChapters.title);
                if (totalPage > 2) {
                    pageContents[2] = createNewPageContent(curBuffer, 2, curChapters.title);
                } else {
                    if (nextChapters == null) { //当前章节为最后一章
                        pageContents[1].isEnd = true;
                    } else if (nextBuffer != null) {
                        pageContents[2] = createNewPageContent(nextBuffer, 0, nextChapters.title);
                    } else {
                        pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                    }
                }
            }
        } else if (curPage > 0) {//非第一页
            pageContents[0] = createNewPageContent(curBuffer, curPage - 1, curChapters.title);
            pageContents[1] = createShowPageContent(curBuffer, curPage, curChapters.title);
            if (totalPage == 2) { //只有两页
                if (nextChapters == null) {
                    pageContents[1].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[2] = createNewPageContent(nextBuffer, 0, nextChapters.title);
                } else {
                    pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                }
            } else { //大于两页
                if (totalPage > curPage + 1) {//当前显示的不是最后一页
                    pageContents[2] = createNewPageContent(curBuffer, curPage + 1, curChapters.title);
                } else {//最后一页
                    if (nextChapters == null) { //当前章节为最后一章
                        pageContents[1].isEnd = true;
                    } else if (nextBuffer != null) {
                        pageContents[2] = createNewPageContent(nextBuffer, 0, nextChapters.title);
                    } else {
                        pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                    }
                }
            }
        } else {//非第一章、第一页
            int preChapter = chapter - 1;
            Entities.Chapters preChapters = mChaptersList.get(preChapter);
            ChapterBufferImage preBuffer = mCacheChapterBuffers.get(String.valueOf(preChapter));
            pageContents[1] = createShowPageContent(curBuffer, curPage, curChapters.title);
            if (preBuffer != null) {
                pageContents[0] = createNewPageContent(preBuffer, preBuffer.getPageCount() - 1, preChapters.title);
            } else {
                pageContents[0] = createNonePageContent(preChapters.title, preChapter, false);
            }
            if (totalPage > 1) { //大于一页
                pageContents[2] = createNewPageContent(curBuffer, 1, curChapters.title);
            } else { //只有一页
                if (nextChapters == null) { //当前章节为最后一章
                    pageContents[1].isEnd = true;
                } else if (nextBuffer != null) {
                    pageContents[2] = createNewPageContent(nextBuffer, 0, nextChapters.title);
                } else {
                    pageContents[2] = createNonePageContent(nextChapters.title, nextChapter, hasEndChapter(nextChapter));
                }
            }
        }
        updatePages(pageContents);
    }

    private CartoonContent getPageContent(int chapter, boolean isNext) {
        CartoonContent pageContent;
        Entities.Chapters chapters = mChaptersList.get(chapter);
        ChapterBufferImage chapterBuffer = mCacheChapterBuffers.get(String.valueOf(chapter));
        if (chapterBuffer != null) {
            int pos = isNext ? 0 : chapterBuffer.getEndPos();
            pageContent = createNewPageContent(chapterBuffer, pos, chapters.title);
        } else {
            pageContent = createNonePageContent(chapters.title, chapter, hasEndChapter(chapter));
        }
        return pageContent;
    }

    private boolean hasEndChapter(int chapter) {
        return mChaptersList == null || mChaptersList.size() <= chapter + 1;
    }

    private int showLoading(int page, CartoonContent showPageContent) {
        if (showPageContent == null) {
            showPageContent = createErrorPageContent(ReadPresenter.Error.NONE);
        }
        showPageContent.error = ReadPresenter.Error.NONE;
        showPageContent.isLoading = true;
        showPageContent.isShow = true;
        CartoonContent[] pageContents = getLoadingOrErrorNewPages(page, showPageContent);
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

    private void showError(int page, @ReadPresenter.Error int error, CartoonContent showPageContent) {
        if (showPageContent == null) {
            showPageContent = createErrorPageContent(error);
        } else {
            showPageContent.error = error;
        }
        showPageContent.isLoading = false;
        showPageContent.isShow = true;
        CartoonContent[] pageContents = getLoadingOrErrorNewPages(page, showPageContent);
        updatePages(pageContents);
    }

    private CartoonContent[] getLoadingOrErrorNewPages(int page, CartoonContent showPageContent) {
        CartoonContent[] newPages = new CartoonContent[3];
        if (mChaptersList != null && mChaptersList.size() > 0) {
            if (mCurChapter == 0) {
                newPages[0] = showPageContent;
                newPages[0].isEnd = hasEndChapter(0);
                newPages[0].isStart = true;
                if (!newPages[0].isEnd) {
                    newPages[1] = getPageContent(1, true);
                    if (!hasEndChapter(1)) {
                        newPages[2] = getPageContent(2, true);
                    }
                }
            } else if (!hasEndChapter(mCurChapter)) {
                newPages[0] = getPageContent(mCurChapter - 1, false);
                newPages[1] = showPageContent;
                newPages[2] = getPageContent(mCurChapter + 1, true);
            } else {
                newPages[0] = getPageContent(mCurChapter - 1, false);
                newPages[1] = showPageContent;
                newPages[1].isEnd = true;
            }
        } else {
            newPages[page] = showPageContent;
        }
        return newPages;
    }

    private CartoonContent createErrorPageContent(@ReadPresenter.Error int error) {
        CartoonContent pageContent = new CartoonContent();
        pageContent.bookId = mBook._id;
        pageContent.error = error;
        return pageContent;
    }

    private CartoonContent createNonePageContent(String title, int chapter, boolean isEnd) {
        CartoonContent pageContent = new CartoonContent();
        pageContent.bookId = mBook._id;
        pageContent.title = title;
        pageContent.chapter = chapter;
        pageContent.isEnd = isEnd;
        return pageContent;
    }

    private CartoonContent createNewPageContent(ChapterBufferImage buffer, int curPage, String title) {
        CartoonContent pageContent = new CartoonContent();
        pageContent.bookId = mBook._id;
        pageContent.title = title;
        pageContent.url = buffer.getUrl(curPage);
        pageContent.chapter = buffer.getChapter();
        pageContent.totalPage = buffer.getPageCount();
        pageContent.maxScale = buffer.getScale(curPage);
        pageContent.curPage = curPage;
        pageContent.bitmap = mImageCache.get(pageContent.url);
        return pageContent;
    }

    private CartoonContent createShowPageContent(ChapterBufferImage buffer, int curPage, String title) {
        CartoonContent pageContent = createNewPageContent(buffer, curPage, title);
        pageContent.isShow = true;
        return pageContent;
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

    private void updatePages(final CartoonContent[] pageContents) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdatePages(pageContents);
                }
            }
        });
    }
}
