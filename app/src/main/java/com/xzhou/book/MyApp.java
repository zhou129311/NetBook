package com.xzhou.book;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.SPUtils;

public class MyApp extends Application {
    private static MyApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SPUtils.get().init(this);
        initNightMode();

    }

    private void initDagger() {

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
