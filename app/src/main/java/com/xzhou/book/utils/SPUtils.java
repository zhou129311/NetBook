package com.xzhou.book.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xzhou.book.MyApp;

import java.util.Set;

public class SPUtils {
    private static SPUtils mInstance;
    private final SharedPreferences mPreferences;
    private final SharedPreferences mSearchPreferences;

    public static SPUtils get() {
        if (mInstance == null) {
            mInstance = new SPUtils();
        }
        return mInstance;
    }

    private SPUtils() {
        mPreferences = MyApp.getContext().getSharedPreferences("book_preference", Context.MODE_PRIVATE);
        mSearchPreferences = MyApp.getContext().getSharedPreferences("book_search_pref", Context.MODE_PRIVATE);
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

    public String getSearchString(String key) {
        return mSearchPreferences.getString(key, null);
    }

    public Set<String> getStringSet(String key) {
        return mPreferences.getStringSet(key, null);
    }

    public int getInt(String key, int defaultVal) {
        return mPreferences.getInt(key, defaultVal);
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public SPUtils putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value).apply();
        return this;
    }

    public SPUtils putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value).apply();
        return this;
    }

    public SPUtils putSearchString(String key, String value) {
        SharedPreferences.Editor editor = mSearchPreferences.edit();
        editor.putString(key, value).apply();
        return this;
    }

    public SPUtils putStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(key, value).apply();
        return this;
    }

    public SPUtils putInt(String key, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value).apply();
        return this;
    }

    public SPUtils delete(String... keys) {
        SharedPreferences.Editor editor = mPreferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
        return this;
    }
}
