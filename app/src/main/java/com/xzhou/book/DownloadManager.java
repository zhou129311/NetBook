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

    private static DownloadManager sInstance;
    private ExecutorService mPool = Executors.newFixedThreadPool(3);
    private Map<String, DownloadCallback> mCallbackMap = new HashMap<>();

    public static class Download {
        public List<Entities.Chapters> list;
        public int start;
        public int end;
        private boolean isPause;

        public boolean isValid() {
            return list != null && list.size() > end && end > start && start >= 0;
        }
    }

    public interface DownloadCallback {
        void onStart();

        void onProgress(int progress, int max);

        void onEnd(int successCount, int failedCount);
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
                    int success = 0;
                    int failed = 0;
                    for (int i = download.start; i < download.end; i++) {
                        if (download.isPause) {
                            break;
                        }
                        Entities.Chapters chapter = download.list.get(i);
                        if (!FileUtils.hasCacheChapter(bookId, i)) {
                            Entities.ChapterRead data = ZhuiShuSQApi.getChapterRead(chapter.link);
                            if (data != null && data.chapter != null && data.chapter.body != null) {
                                success++;
                                File file = FileUtils.getChapterFile(bookId, i);
                                String body = AppUtils.formatContent(data.chapter.body);
                                FileUtils.writeFile(file.getAbsolutePath(), body, false);
                            } else {
                                failed++;
                            }
                        } else {
                            success++;
                        }
                        notifyProgress(bookId, success + failed, download.end - download.start);
                    }
                    notifyEnd(bookId, success, failed);
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

    private void notifyStart(String bookId) {
        DownloadCallback callback = mCallbackMap.get(bookId);
        if (callback != null) {
            callback.onStart();
        }
    }

    private void notifyProgress(String bookId, int progress, int max) {
        DownloadCallback callback = mCallbackMap.get(bookId);
        if (callback != null) {
            callback.onProgress(progress, max);
        }
    }

    private void notifyEnd(String bookId, int success, int fail) {
        DownloadCallback callback = mCallbackMap.get(bookId);
        if (callback != null) {
            callback.onEnd(success, fail);
        }
    }

}
