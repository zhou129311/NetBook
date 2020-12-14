package com.xzhou.book;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.ThemeUtils;

public class MyApp extends Application {
    private static MyApp mInstance;
    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mHandler = new Handler();
        SearchModel.initSupportParseMap();
        initNightMode();
        BookManager.get().init();
        AppSettings.init();
        ThemeUtils.init();
        CrashHandler.getInstance().init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).onLowMemory();
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static void runUI(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
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
