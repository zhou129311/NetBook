package com.xzhou.book;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatDelegate;

import com.xzhou.book.utils.AppSettings;

public class MyApp extends Application {
    private static MyApp mInstance;
    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mHandler = new Handler();
        initNightMode();
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
