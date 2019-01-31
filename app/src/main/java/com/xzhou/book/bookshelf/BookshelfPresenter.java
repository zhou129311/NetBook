package com.xzhou.book.bookshelf;

import android.text.TextUtils;

import com.xzhou.book.DownloadManager;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class BookshelfPresenter extends BasePresenter<BookshelfContract.View> implements BookshelfContract.Presenter {

    private boolean mIsStart;

    public BookshelfPresenter(BookshelfContract.View view) {
        super(view);
        BookManager.get().setDataChangeListener(new BookManager.DataChangeListener() {
            @Override
            public void onInsert(int position, BookProvider.LocalBook book) {
                add(position, book);
            }

            @Override
            public void onDelete(BookProvider.LocalBook book) {
                delete(book);
            }

            @Override
            public void onUpdate(List<BookProvider.LocalBook> list) {
                updateList(list);
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        BookManager.get().setDataChangeListener(null);
    }

    @Override
    public boolean start() {
        if (!mIsStart) {
            mIsStart = true;
            List<BookProvider.LocalBook> books = BookManager.get().getLocalBooks();
            updateList(books);
            return books.size() > 0;
        }
        return false;
    }

    @Override
    public void refresh() {
        mView.onLoadingState(true);
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<BookProvider.LocalBook> books = BookManager.get().getLocalBooks();
                StringBuilder sb = new StringBuilder();
                for (int i = 0, size = books.size(); i < size; i++) {
                    BookProvider.LocalBook book = books.get(i);
                    if (book.isBaiduBook) {
                        List<Entities.Chapters> oldList = AppSettings.getChapterList(book._id);
                        continue;
                    }
                    if (i != 0) {
                        sb.append(",");
                    }
                    sb.append(book._id);
                }
                String ids = sb.toString();
                List<Entities.Updated> list = null;
                if (!TextUtils.isEmpty(ids)) {
                    list = ZhuiShuSQApi.getBookshelfUpdated(ids);
                }
                boolean hasUpdated = false;
                String error = null;
                List<BookProvider.LocalBook> updateList = new ArrayList<>();
                if (list != null) {
                    for (Entities.Updated updated : list) {
                        long updatedTime = AppUtils.getTimeFormDateString(updated.updated);
                        final BookProvider.LocalBook localBook = BookManager.get().findById(updated._id);
                        if (localBook != null && localBook.updated < updatedTime) {
                            Entities.BookAToc aToc = ZhuiShuSQApi.getBookMixAToc(localBook._id, localBook.sourceId);
                            if (aToc != null && aToc.chapters != null) {
                                List<Entities.Chapters> oldChapters = AppSettings.getChapterList(localBook._id);
                                List<Entities.Chapters> newChapters = null;
                                int oldSize = oldChapters != null ? oldChapters.size() : 0;
                                int newSize = aToc.chapters.size();
                                if (oldSize < newSize) {
                                    if (oldChapters != null) {
                                        newChapters = new ArrayList<>(oldChapters);
                                    } else {
                                        newChapters = new ArrayList<>();
                                    }
                                    newChapters.addAll(aToc.chapters.subList(oldSize, newSize));
                                }
                                if (newChapters != null) {
                                    AppSettings.saveChapterList(localBook._id, newChapters);
                                }

                            }

                            hasUpdated = true;
                            localBook.isShowRed = true;
                            localBook.updated = updatedTime;
                            localBook.lastChapter = updated.lastChapter;
                            updateList.add(localBook);
                        }
                    }
                } else {
                    error = AppUtils.getString(AppUtils.isNetworkAvailable() ? R.string.network_failed : R.string.network_unconnected);
                }
                if (hasUpdated) {
                    BookProvider.updateLocalBooks(updateList);
                }
                updated(hasUpdated, error);
            }
        });
    }

    @Override
    public void download(final BookProvider.LocalBook localBook) {
        if (DownloadManager.get().hasDownloading(localBook._id)) {
            ToastUtils.showShortToast("正在缓存中...");
            return;
        }
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.Chapters> chaptersList = AppSettings.getChapterList(localBook._id);
                if (chaptersList == null) {
                    if (localBook.isBaiduBook) {
                        HtmlParse htmlParse = HtmlParseFactory.getHtmlParse(localBook.curSourceHost);
                        if (htmlParse != null) {
                            chaptersList = htmlParse.parseChapters(localBook.readUrl);
                        }
                    } else {
                        Entities.BookAToc aToc = ZhuiShuSQApi.getBookMixAToc(localBook._id, localBook.sourceId);
                        if (aToc != null && aToc.chapters != null && aToc.chapters.size() > 0) {
                            chaptersList = aToc.chapters;
                        }
                    }
                    if (chaptersList != null) {
                        AppSettings.saveChapterList(localBook._id, chaptersList);
                    }
                }
                if (chaptersList != null && chaptersList.size() > 0) {
                    DownloadManager.Download download = DownloadManager.createAllDownload(chaptersList, localBook.isBaiduBook ? localBook.curSourceHost : null);
                    DownloadManager.get().startDownload(localBook._id, download);
                    if (localBook.checkAddDownloadCallback()) {
                        updateDownloadStatus(localBook);
                        localBook.setUpdateDownloadStateListener(new BookProvider.LocalBook.UpdateDownloadStateListener() {
                            @Override
                            public void onUpdate() {
                                updateDownloadStatus(localBook);
                            }
                        });
                    }
                } else {
                    localBook.downloadStatus = AppUtils.getString(R.string.book_read_download_error_topic);
                    updateDownloadStatus(localBook);
                }
            }
        });
    }

    private void updateList(final List<BookProvider.LocalBook> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    if (list != null) {
                        for (final BookProvider.LocalBook book : list) {
                            if (book.checkAddDownloadCallback()) {
                                updateDownloadStatus(book);
                                book.setUpdateDownloadStateListener(new BookProvider.LocalBook.UpdateDownloadStateListener() {
                                    @Override
                                    public void onUpdate() {
                                        updateDownloadStatus(book);
                                    }
                                });
                            }
                        }
                    }
                    mView.onLoadingState(false);
                    mView.onDataChange(list);
                }
            }
        });
    }

    private void add(final int position, final BookProvider.LocalBook book) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onAdd(position, book);
                }
            }
        });
    }

    private void delete(final BookProvider.LocalBook book) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onRemove(book);
                }
            }
        });
    }

    private void updated(final boolean updated, final String error) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoadingState(false);
                    mView.onBookshelfUpdated(updated, error);
                }
            }
        });
    }

    private void updateDownloadStatus(final BookProvider.LocalBook localBook) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUpdateDownloadState(localBook);
                }
            }
        });
    }
}
