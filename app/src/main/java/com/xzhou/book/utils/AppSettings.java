package com.xzhou.book.utils;

import android.app.Activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.Constant.ReadTheme;

import java.util.List;

public class AppSettings {
    private static final String PRE_KEY_ISNIGHT = "pre_key_is_night";
    private static final String PRE_KEY_FONT_SIZE = "pre_key_font_size";
    private static final String PRE_KEY_THEME = "pre_key_theme";
    private static final String PRE_KEY_BRIGHTNESS_SYSTEM = "pre_key_brightness_system";
    private static final String PRE_KEY_BRIGHTNESS = "pre_key_brightness";
    private static final String PRE_KEY_BOOKSHELF_ORDER = "pre_key_bookshelf_order";
    private static final String PRE_KEY_READ_CACHE = "pre_key_read_cache";
    private static final String PRE_KEY_SEARCH_HISTORY = "pre_key_search_history";
    private static final String PRE_KEY_SAVING_TRAFFIC = "pre_key_saving_traffic";
    private static final String PRE_KEY_VOLUME_TURN_PAGE = "pre_key_volume_turn_page";
    private static final String PRE_KEY_FULL_SCREEN = "pre_key_full_screen";
    private static final String PRE_KEY_CLICK_NEXT_PAGE = "pre_key_click_next_page";
    private static final String PRE_KEY_LOGIN = "pre_key_login";
    private static final String PRE_KEY_TOTAL_READ_TIME = "pre_key_total_read_time";
    private static final String PRE_KEY_LAST_STOP_READ_TIME = "pre_key_last_stop_read_time";
    private static final String PRE_KEY_START_SLEEP_TIME = "pre_key_start_sleep_time";
    private static final String PRE_KEY_READ_SLEEP_TIME = "pre_key_read_sleep_time";
    private static final String PRE_KEY_SCREEN_OFF_MODE = "pre_key_screen_off_mode";

    public static final int PRE_VALUE_BOOKSHELF_ORDER_ADD_TIME = 0;
    public static final int PRE_VALUE_BOOKSHELF_ORDER_READ_TIME = 1;
    public static final int PRE_VALUE_BOOKSHELF_ORDER_UPDATE_TIME = 2;

    public static final int PRE_VALUE_READ_CACHE_NONE = 0;
    public static final int PRE_VALUE_READ_CACHE_1 = 1;
    public static final int PRE_VALUE_READ_CACHE_5 = 2;
    public static final int PRE_VALUE_READ_CACHE_10 = 3;

    public static final int PRE_VALUE_SCREEN_OFF_NONE = 0;
    public static final int PRE_VALUE_SCREEN_OFF_5 = 1;
    public static final int PRE_VALUE_SCREEN_OFF_10 = 2;
    public static final int PRE_VALUE_SCREEN_OFF_SYSTEM = 3;

    public static boolean HAS_SAVING_TRAFFIC = false;
    public static boolean HAS_VOLUME_TURN_PAGE = true;
    public static boolean HAS_FULL_SCREEN_MODE = true;
    public static boolean HAS_CLICK_NEXT_PAGE = true;
    public static int BOOK_ORDER = PRE_VALUE_BOOKSHELF_ORDER_ADD_TIME;
    public static int READ_CACHE_MODE = PRE_VALUE_READ_CACHE_NONE;
    public static int SCREEN_OFF_MODE = PRE_VALUE_SCREEN_OFF_NONE;
    public static @ReadTheme
    int READ_THEME = ReadTheme.DEFAULT;
    public static long READ_SLEEP_TIME = 30 * 60 * 1000;

    public static void init() {
        HAS_SAVING_TRAFFIC = isSavingTraffic();
        HAS_VOLUME_TURN_PAGE = isVolumeTurnPage();
        HAS_CLICK_NEXT_PAGE = isClickNextPage();
        HAS_FULL_SCREEN_MODE = isFullScreenMode();
        READ_THEME = getReadTheme();
        BOOK_ORDER = getBookshelfOrder();
        READ_CACHE_MODE = getReadCacheMode();
        READ_SLEEP_TIME = getSleepTime();
        SCREEN_OFF_MODE = getScreenOffMode();
    }

    public static int getScreenOffMode() {
        return SPUtils.get().getInt(PRE_KEY_SCREEN_OFF_MODE, PRE_VALUE_SCREEN_OFF_NONE);
    }

    public static void setScreenOffMode(int mode) {
        SCREEN_OFF_MODE = mode;
        SPUtils.get().putInt(PRE_KEY_SCREEN_OFF_MODE, mode);
    }

    public static long getSleepTime() {
        String time = SPUtils.get().getString(PRE_KEY_READ_SLEEP_TIME, String.valueOf(30 * 60 * 1000));
        return Long.parseLong(time);
    }

    public static void setSleepTime(long sleepTime) {
        READ_SLEEP_TIME = sleepTime;
        String time = String.valueOf(sleepTime);
        SPUtils.get().putString(PRE_KEY_READ_SLEEP_TIME, time);
    }

    public static long getStartSleepTime() {
        String time = SPUtils.get().getString(PRE_KEY_START_SLEEP_TIME, "0");
        return Long.parseLong(time);
    }

    public static void setStartSleepTime(long startSleepTime) {
        String time = String.valueOf(startSleepTime);
        SPUtils.get().putString(PRE_KEY_START_SLEEP_TIME, time);
    }

    public static long getTotalReadTime() {
        String time = SPUtils.get().getString(PRE_KEY_TOTAL_READ_TIME, "0");
        return Long.parseLong(time);
    }

    public static void setTotalReadTime(long totalReadTime) {
        String time = String.valueOf(totalReadTime);
        SPUtils.get().putString(PRE_KEY_TOTAL_READ_TIME, time);
    }

    public static long getLastStopReadTime() {
        String lastTime = SPUtils.get().getString(PRE_KEY_LAST_STOP_READ_TIME, "0");
        return Long.parseLong(lastTime);
    }

    public static void setLastStopReadTime(long lastStopReadTime) {
        String time = String.valueOf(lastStopReadTime);
        String timestamp = String.valueOf(System.currentTimeMillis());
        SPUtils.get().putString(PRE_KEY_LAST_STOP_READ_TIME, time);
    }

    public static long getLastStopTimestamp() {
        String time = SPUtils.get().getString("timestamp", "0");
        return Long.parseLong(time);
    }

    public static long getTodayReadTime() {
        String time = SPUtils.get().getString("today_read_time", "0");
        return Long.parseLong(time);
    }

    public static Entities.Login getLogin() {
        Entities.Login login = null;
        String loginStr = SPUtils.get().getString(PRE_KEY_LOGIN);
        if (loginStr != null) {
            try {
                login = new Gson().fromJson(loginStr, Entities.Login.TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return login;
    }

    public static void saveLogin(Entities.Login login) {
        String loginStr = "";
        if (login != null) {
            try {
                loginStr = new Gson().toJson(login);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SPUtils.get().putString(PRE_KEY_LOGIN, loginStr);
    }

    public static boolean isFullScreenMode() {
        return SPUtils.get().getBoolean(PRE_KEY_FULL_SCREEN, true);
    }

    public static void saveFullScreenMode(boolean enable) {
        HAS_FULL_SCREEN_MODE = enable;
        SPUtils.get().putBoolean(PRE_KEY_FULL_SCREEN, enable);
    }

    /**
     * @return 是否全屏点击翻页
     */
    public static boolean isClickNextPage() {
        return SPUtils.get().getBoolean(PRE_KEY_CLICK_NEXT_PAGE, false);
    }

    public static void saveClickNextPage(boolean enable) {
        HAS_CLICK_NEXT_PAGE = enable;
        SPUtils.get().putBoolean(PRE_KEY_CLICK_NEXT_PAGE, enable);
    }

    /**
     * @return 是否音量键翻页
     */
    public static boolean isVolumeTurnPage() {
        return SPUtils.get().getBoolean(PRE_KEY_VOLUME_TURN_PAGE, true);
    }

    public static void saveVolumeTurnPage(boolean enable) {
        HAS_VOLUME_TURN_PAGE = enable;
        SPUtils.get().putBoolean(PRE_KEY_VOLUME_TURN_PAGE, enable);
    }

    /**
     * @return 省流量模式
     */
    public static boolean isSavingTraffic() {
        return SPUtils.get().getBoolean(PRE_KEY_SAVING_TRAFFIC, false);
    }

    public static void setSavingTraffic(boolean enable) {
        HAS_SAVING_TRAFFIC = enable;
        SPUtils.get().putBoolean(PRE_KEY_SAVING_TRAFFIC, enable);
    }

    public static List<String> getSearchHistory() {
        List<String> list = null;
        String listStr = SPUtils.get().getString(PRE_KEY_SEARCH_HISTORY);
        if (listStr != null) {
            try {
                list = new Gson().fromJson(listStr, new TypeToken<List<String>>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void saveHistory(List<String> list) {
        String listStr = null;
        try {
            listStr = new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list == null || list.size() <= 0) {
            listStr = "";
        }
        SPUtils.get().putString(PRE_KEY_SEARCH_HISTORY, listStr);
    }

    public static int getBookshelfOrder() {
        return SPUtils.get().getInt(PRE_KEY_BOOKSHELF_ORDER, PRE_VALUE_BOOKSHELF_ORDER_ADD_TIME);
    }

    public static void saveBookshelfOrder(int value) {
        SPUtils.get().putInt(PRE_KEY_BOOKSHELF_ORDER, value);
    }

    public static int getReadCacheMode() {
        return SPUtils.get().getInt(PRE_KEY_READ_CACHE, PRE_VALUE_READ_CACHE_NONE);
    }

    public static void saveReadCacheMode(int value) {
        SPUtils.get().putInt(PRE_KEY_READ_CACHE, value);
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
        READ_THEME = theme;
        SPUtils.get().putInt(PRE_KEY_THEME, theme);
    }

    public static @ReadTheme
    int getReadTheme() {
        return SPUtils.get().getInt(PRE_KEY_THEME, ReadTheme.DEFAULT);
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
        return new int[]{chapter, startPos};
    }

    public static void saveWebReadProgress(String bookId, String url) {
        SPUtils.get().putString(getChapterKey(bookId) + "_web", url);
    }

    public static String getWebReadProgress(String bookId) {
        return SPUtils.get().getString(getChapterKey(bookId) + "_web", null);
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
        String listStr = SPUtils.get().getString(getChapterListKey(bookId));
        if (listStr != null) {
            try {
                chapters = new Gson().fromJson(listStr, Entities.Chapters.TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return chapters;
    }

    public static void saveSearchList(String key, List<SearchModel.SearchBook> list) {
        String listStr = null;
        try {
            listStr = new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listStr != null) {
            SPUtils.get().putSearchString(key, listStr);
        }
    }

    public static void deleteSearchList(String key) {
        SPUtils.get().delete(key);
    }

    public static List<SearchModel.SearchBook> getSearchList(String key) {
        List<SearchModel.SearchBook> relsut = null;
        String listStr = SPUtils.get().getSearchString(key);
        if (listStr != null) {
            try {
                relsut = new Gson().fromJson(listStr, SearchModel.SearchBook.TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return relsut;
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
