package com.xzhou.book;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatDelegate;

import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.SPUtils;
import com.xzhou.book.utils.ToastUtils;

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

    private void initNightMode() {
        boolean isNight = SPUtils.get().getBoolean(AppSettings.PRE_KEY_ISNIGHT, false);
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static MyApp getContext() {
        return mInstance;
    }
}
