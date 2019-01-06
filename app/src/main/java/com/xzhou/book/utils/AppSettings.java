package com.xzhou.book.utils;

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

    public static int getBrightness() {
        return SPUtils.get().getInt(PRE_KEY_BRIGHTNESS, AppUtils.getScreenBrightness());
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

    public static void saveFontSize(int fontSizePx) {
        SPUtils.get().putInt(PRE_KEY_FONT_SIZE, fontSizePx);
    }

    public static int getFontSize() {
        return SPUtils.get().getInt(PRE_KEY_FONT_SIZE, AppUtils.dip2px(16));
    }

    public static void deleteReadProgress(String bookId) {
        SPUtils.get().delete(bookId);
    }

    public static void saveReadProgress(String bookId, int chapter, int bufBeginPos, int bufEndPos) {
        SPUtils.get().putInt(getChapterKey(bookId), chapter)
                .putInt(getStartPosKey(bookId), bufBeginPos)
                .putInt(getEndPosKey(bookId), bufEndPos);
    }

    public static int[] getReadProgress(String bookId) {
        int chapter = SPUtils.get().getInt(getChapterKey(bookId), 0);
        int startPos = SPUtils.get().getInt(getStartPosKey(bookId), 0);
        int endPos = SPUtils.get().getInt(getEndPosKey(bookId), 0);
        return new int[]{chapter, startPos, endPos};
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

    private static String getEndPosKey(String bookId) {
        return bookId + "-endPos";
    }
}
