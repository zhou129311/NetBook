package com.xzhou.book;

import android.app.Application;
import android.os.Handler;
import android.support.v7.app.AppCompatDelegate;
import android.util.LruCache;

import com.xzhou.book.read.ChapterBuffer;
import com.xzhou.book.utils.AppSettings;

public class MyApp extends Application {
    private static MyApp mInstance;
    private static Handler mHandler;
    private LruCache<String, ChapterBuffer> mCacheChapterBuffers;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mHandler = new Handler();
        initNightMode();
        mCacheChapterBuffers = new LruCache<>(4 * 1024 * 1024);
    }

    public LruCache<String, ChapterBuffer> getCacheChapterBuffers() {
        return mCacheChapterBuffers;
    }

    public void clearCache() {
        mCacheChapterBuffers.evictAll();
    }

    public static Handler getHandler() {
        return mHandler;
    }

    private void initNightMode() {
        if (AppSettings.isNight()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static MyApp getContext() {
        return mInstance;
    }
}
