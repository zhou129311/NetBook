package com.xzhou.book.utils;

import android.app.Activity;

import com.google.gson.Gson;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant.ReadTheme;

import java.util.List;

public class AppSettings {
    private static final String PRE_KEY_ISNIGHT = "pre_key_is_night";
    private static final String PRE_KEY_FONT_SIZE = "pre_key_font_size";
    private static final String PRE_KEY_THEME = "pre_key_theme";
    private static final String PRE_KEY_BRIGHTNESS_SYSTEM = "pre_key_brightness_system";
    private static final String PRE_KEY_BRIGHTNESS = "pre_key_brightness";
    private static final String PRE_KEY_BOOKSHELF_ORDER = "pre_key_bookshelf_order";

    public static final int PRE_VALUE_BOOKSHELF_ORDER_UPDATE_TIME = 0;
    public static final int PRE_VALUE_BOOKSHELF_ORDER_READ_TIME = 1;
    public static final int PRE_VALUE_BOOKSHELF_ORDER_ADD_TIME = 2;

    public static int getBookshelfOrder() {
        return SPUtils.get().getInt(PRE_KEY_BOOKSHELF_ORDER, PRE_VALUE_BOOKSHELF_ORDER_ADD_TIME);
    }

    public static void saveBookshelfOrder(int value) {
        SPUtils.get().putInt(PRE_KEY_BOOKSHELF_ORDER, value);
    }

    public static int getBrightness(Activity activity) {
        return SPUtils.get().getInt(PRE_KEY_BRIGHTNESS, AppUtils.getScreenBrightness(activity));
    }

    public static void saveBrightness(int value) {
        SPUtils.get().putInt(PRE_KEY_BRIGHTNESS, value);
    }

    public static void saveBrightnessSystem(boolean isSystem) {
        SPUtils.get().putBoolean(PRE_KEY_BRIGHTNESS_SYSTEM, isSystem);
    }

    public static boolean isBrightnessSystem() {
        return SPUtils.get().getBoolean(PRE_KEY_BRIGHTNESS_SYSTEM, true);
    }

    public static void saveReadTheme(@ReadTheme int theme) {
        SPUtils.get().putInt(PRE_KEY_THEME, theme);
    }

    public static @ReadTheme
    int getReadTheme() {
        return SPUtils.get().getInt(PRE_KEY_THEME, ReadTheme.WHITE);
    }

    public static boolean isNight() {
        return SPUtils.get().getBoolean(PRE_KEY_ISNIGHT, false);
    }

    public static void setNight(boolean night) {
        SPUtils.get().putBoolean(PRE_KEY_ISNIGHT, night);
    }

    public static void saveFontSizeSp(int fontSizeSp) {
        SPUtils.get().putInt(PRE_KEY_FONT_SIZE, fontSizeSp);
    }

    public static int getFontSizeSp() {
        return SPUtils.get().getInt(PRE_KEY_FONT_SIZE, 16);
    }

    public static void deleteReadProgress(String bookId) {
        SPUtils.get().delete(getChapterKey(bookId), getStartPosKey(bookId), getPageKey(bookId));
    }

    public static void saveReadProgress(String bookId, int chapter, int bufBeginPos) {
        SPUtils.get().putInt(getChapterKey(bookId), chapter)
                .putInt(getStartPosKey(bookId), bufBeginPos);
    }

    public static int[] getReadProgress(String bookId) {
        int chapter = SPUtils.get().getInt(getChapterKey(bookId), 0);
        int startPos = SPUtils.get().getInt(getStartPosKey(bookId), 0);
        return new int[] { chapter, startPos };
    }

    public static void deleteChapterList(String bookId) {
        SPUtils.get().delete(getChapterListKey(bookId));
    }

    public static void saveChapterList(String bookId, List<Entities.Chapters> list) {
        String listStr = null;
        try {
            listStr = new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listStr != null) {
            SPUtils.get().putString(getChapterListKey(bookId), listStr);
        }
    }

    public static List<Entities.Chapters> getChapterList(String bookId) {
        List<Entities.Chapters> chapters = null;
        String listStr = SPUtils.get().getString(getChapterListKey(bookId), null);
        if (listStr != null) {
            try {
                chapters = new Gson().fromJson(listStr, Entities.Chapters.TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return chapters;
    }

    private static String getChapterListKey(String bookId) {
        return bookId + "-chapterList";
    }

    private static String getChapterKey(String bookId) {
        return bookId + "-chapter";
    }

    private static String getStartPosKey(String bookId) {
        return bookId + "-startPos";
    }

    private static String getPageKey(String bookId) {
        return bookId + "-Page";
    }
}
