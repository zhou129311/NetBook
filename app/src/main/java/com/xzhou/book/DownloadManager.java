package com.xzhou.book;

import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {

    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;

    public static final int CHAPTER_LATER_50 = 0;
    public static final int CHAPTER_LATER_ALL = 1;
    public static final int CHAPTER_ALL = 2;

    private static DownloadManager sInstance;
    private ExecutorService mPool = Executors.newFixedThreadPool(3);
    private Map<String, DownloadCallback> mCallbackMap = new HashMap<>();

    public static class Download {
        public List<Entities.Chapters> list;
        public int start;
        public int end;
        private boolean isPause;

        public boolean isValid() {
            return list != null && list.size() >= end && end > start && start >= 0;
        }
    }

    public static Download createDownload(int which, int curChapter, List<Entities.Chapters> list) {
        Download download = new Download();
        download.list = list;
        download.start = curChapter;
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

    public void startDownload(final String bookId, final Download download) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                if (download.isValid() && !download.isPause) {
                    notifyStart(bookId);
                    int error = ERROR_NONE;
                    int fail = 0;
                    for (int i = download.start; i < download.end; i++) {
                        if (download.isPause) {
                            break;
                        }
                        Entities.Chapters chapter = download.list.get(i);
                        if (!FileUtils.hasCacheChapter(bookId, i)) {
                            Entities.ChapterRead data = ZhuiShuSQApi.getChapterRead(chapter.link);
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
                            chapter.hasLocal = true;
                        }
                        notifyProgress(bookId, i + 1, download.list.size());
                    }
                    notifyEnd(bookId, fail, error);
                }
            }
        });
    }

    public void addCallback(String bookId, DownloadCallback callback) {
        mCallbackMap.put(bookId, callback);
    }

    public void removeCallback(String bookId) {
        mCallbackMap.remove(bookId);
    }

    public boolean hasCallback(String bookId) {
        return mCallbackMap.containsKey(bookId);
    }

    private void notifyStart(String bookId) {
        final DownloadCallback callback = mCallbackMap.get(bookId);
        if (callback != null) {
            MyApp.runUI(new Runnable() {
                @Override
                public void run() {
                    callback.onStartDownload();
                }
            });
        }
    }

    private void notifyProgress(String bookId, final int progress, final int max) {
        final DownloadCallback callback = mCallbackMap.get(bookId);
        if (callback != null) {
            MyApp.runUI(new Runnable() {
                @Override
                public void run() {
                    callback.onProgress(progress, max);
                }
            });
        }
    }

    private void notifyEnd(String bookId, final int failedCount, final int error) {
        final DownloadCallback callback = mCallbackMap.get(bookId);
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                callback.onEndDownload(failedCount, error);
            }
        });
    }

}
