package com.xzhou.book.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {

    public static final String PRE_KEY_ISNIGHT = "pre_key_is_night";

    private static SPUtils mInstance;
    private SharedPreferences mPreferences;

    public static SPUtils get() {
        if (mInstance == null) {
            synchronized (SPUtils.class) {
                if (mInstance == null) {
                    mInstance = new SPUtils();
                }
            }
        }
        return mInstance;
    }

    private SPUtils() {
    }

    public void init(Context context) {
        mPreferences = context.getSharedPreferences("book_preference", Context.MODE_PRIVATE);
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return mPreferences.getBoolean(key, defaultVal);
    }

    public String getString(String key, String defaultVal) {
        return mPreferences.getString(key, defaultVal);
    }

    public String getString(String key) {
        return mPreferences.getString(key, null);
    }

    public int getInt(String key, int defaultVal) {
        return mPreferences.getInt(key, defaultVal);
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public SPUtils putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
        return this;
    }

    public SPUtils putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        return this;
    }

    public SPUtils putInt(String key, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
        return this;
    }
}
