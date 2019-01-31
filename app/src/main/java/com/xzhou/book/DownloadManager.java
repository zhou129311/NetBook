package com.xzhou.book;

import android.text.TextUtils;

import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {

    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;
    public static final int ERROR_NO_TOPIC = 2;

    public static final int CHAPTER_LATER_50 = 0;
    public static final int CHAPTER_LATER_ALL = 1;
    public static final int CHAPTER_ALL = 2;

    public static final String[] DOWNLOAD_ITEMS = new String[] {
            "后面五十章", "后面全部", "全部"
    };

    private static DownloadManager sInstance;
    private ExecutorService mPool = Executors.newSingleThreadExecutor();
    private final Map<String, List<DownloadCallback>> mCallbackMap = new HashMap<>();
    private final Map<String, Download> mDownloadMap = new HashMap<>();

    public static class Download {
        public List<Entities.Chapters> list;
        public String host;
        public int start;
        public int end;
        private boolean isPause;

        public boolean isValid() {
            return list != null && list.size() >= end && end > start && start >= 0;
        }
    }

    public static Download createAllDownload(List<Entities.Chapters> list) {
        return createDownload(CHAPTER_ALL, 0, list, null);
    }

    public static Download createAllDownload(List<Entities.Chapters> list, String host) {
        return createDownload(CHAPTER_ALL, 0, list, host);
    }

    public static Download createDownload(int which, int curChapter, List<Entities.Chapters> list, String host) {
        Download download = new Download();
        download.list = list;
        download.start = curChapter;
        download.host = host;
        switch (which) {
        case CHAPTER_LATER_50:
            download.end = download.start + 51;
            if (download.end > list.size()) {
                download.end = list.size();
            }
            break;
        case CHAPTER_LATER_ALL:
            download.end = list.size();
            break;
        default:
            download.start = 0;
            download.end = list.size();
            break;
        }
        return download;
    }

    public static Download createReadCacheDownload(int curChapter, int size, List<Entities.Chapters> list) {
        Download download = new Download();
        download.list = list;
        download.start = curChapter;
        download.end = download.start + size + 1;
        if (download.end > list.size()) {
            download.end = list.size();
        }
        return download;
    }

    public interface DownloadCallback {
        void onStartDownload();

        void onProgress(int progress, int max);

        void onEndDownload(int failedCount, int error);
    }

    public static DownloadManager get() {
        if (sInstance == null) {
            sInstance = new DownloadManager();
        }
        return sInstance;
    }

    private DownloadManager() {
    }

    public boolean startDownload(final String bookId, final Download download) {
        return startDownload(bookId, download, true);
    }

    public boolean startDownload(final String bookId, final Download download, final boolean hasNotify) {
        if (hasDownloading(bookId)) {
            return false;
        }
        if (hasNotify) {
            notifyStart(bookId);
        }
        mDownloadMap.put(bookId, download);
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                if (download.isValid() && !download.isPause) {
                    int error = ERROR_NONE;
                    int fail = 0;
                    int exist = 0;
                    HtmlParse parse = null;
                    if (!TextUtils.isEmpty(download.host)) {
                        parse = HtmlParseFactory.getHtmlParse(download.host);
                    }
                    for (int i = download.start; i < download.end; i++) {
                        if (download.isPause) {
                            break;
                        }
                        Entities.Chapters chapter = download.list.get(i);
                        if (!chapter.hasLocal && !FileUtils.hasCacheChapter(bookId, i)) {
                            Entities.ChapterRead data;
                            if (parse != null) {
                                data = parse.parseChapterRead(chapter.link);
                            } else {
                                data = ZhuiShuSQApi.getChapterRead(chapter.link);
                            }
                            if (data != null && data.chapter != null && data.chapter.body != null) {
                                File file = FileUtils.getChapterFile(bookId, i);
                                String body = AppUtils.formatContent(data.chapter.body);
                                FileUtils.writeFile(file.getAbsolutePath(), body, false);
                                chapter.hasLocal = true;
                            } else {
                                fail++;
                                if (!AppUtils.isNetworkAvailable()) {
                                    error = ERROR_NO_NETWORK;
                                    break;
                                }
                            }
                        } else {
                            exist++;
                            chapter.hasLocal = true;
                        }
                        if (hasNotify) {
                            if ((i + 1) - exist > 0) {
                                notifyProgress(bookId, i + 1, download.list.size());
                            }
                        }
                    }
                    AppSettings.saveChapterList(bookId, download.list);
                    mDownloadMap.remove(bookId);
                    if (hasNotify) {
                        notifyEnd(bookId, fail, error);
                    }
                }
            }
        });
        return true;
    }

    public void pauseDownload(String bookId) {
        Download download = mDownloadMap.get(bookId);
        if (download != null) {
            download.isPause = true;
        }
    }

    public void addCallback(String bookId, DownloadCallback callback) {
        List<DownloadCallback> list = mCallbackMap.get(bookId);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(callback);
        mCallbackMap.put(bookId, list);
    }

    public void removeCallback(String bookId, DownloadCallback callback) {
        List<DownloadCallback> list = mCallbackMap.get(bookId);
        if (list != null) {
            list.remove(callback);
            if (list.size() < 1) {
                mCallbackMap.remove(bookId);
            }
        }
    }

    public boolean hasDownloading(String bookId) {
        return mDownloadMap.containsKey(bookId);
    }

    private void notifyStart(String bookId) {
        List<DownloadCallback> list = mCallbackMap.get(bookId);
        if (list != null && list.size() > 0) {
            for (final DownloadCallback callback : list) {
                if (callback != null) {
                    MyApp.runUI(new Runnable() {
                        @Override
                        public void run() {
                            callback.onStartDownload();
                        }
                    });
                }
            }
        }
    }

    private void notifyProgress(String bookId, final int progress, final int max) {
        List<DownloadCallback> list = mCallbackMap.get(bookId);
        if (list != null && list.size() > 0) {
            for (final DownloadCallback callback : list) {
                if (callback != null) {
                    MyApp.runUI(new Runnable() {
                        @Override
                        public void run() {
                            callback.onProgress(progress, max);
                        }
                    });
                }
            }
        }
    }

    private void notifyEnd(String bookId, final int failedCount, final int error) {
        List<DownloadCallback> list = mCallbackMap.get(bookId);
        if (list != null && list.size() > 0) {
            for (final DownloadCallback callback : list) {
                if (callback != null) {
                    MyApp.runUI(new Runnable() {
                        @Override
                        public void run() {
                            callback.onEndDownload(failedCount, error);
                        }
                    });
                }
            }
        }
    }

}
