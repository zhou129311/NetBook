package com.xzhou.book.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUtils {
    private static final int UNSTABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN;
    private static final int UNSTABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    private static final int STABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private static final int STABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private static final int EXPAND_STATUS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    private static final int EXPAND_NAV = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private static final long ONE_MINUTE = 60000L;
    private static final long ONE_HOUR = 3600000L;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;

    private static final String ONE_SECOND_AGO = "秒前";
    private static final String ONE_MINUTE_AGO = "分钟前";
    private static final String ONE_HOUR_AGO = "小时前";
    private static final String ONE_DAY_AGO = "天前";
    private static final String ONE_MONTH_AGO = "月前";
    private static final String ONE_YEAR_AGO = "年前";

    public static String getString(int resId) {
        return MyApp.getContext().getResources().getString(resId);
    }

    public static String getString(int resId, Object... args) {
        return MyApp.getContext().getResources().getString(resId, args);
    }

    public static int getThemeColor(@Constant.ReadTheme int theme) {
        int color = getColor(R.color.read_theme_white);
        switch (theme) {
        case Constant.ReadTheme.BROWN:
            color = getColor(R.color.read_theme_brown);
            break;
        case Constant.ReadTheme.GREEN:
            color = getColor(R.color.read_theme_green);
            break;
        }
        return color;
    }

    public static int getColor(int resId) {
        return MyApp.getContext().getResources().getColor(resId);
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                NetworkInfo info = cm.getActiveNetworkInfo();
                return info != null && info.isAvailable();
            }
        }
        return false;
    }

    public static int getScreenWidth() {
        return MyApp.getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return MyApp.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static int dip2px(float dip) {
        float density = MyApp.getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    public static int px2dip(int px) {
        // px/dip = density;
        float density = MyApp.getContext().getResources().getDisplayMetrics().density;
        return (int) (px / density + 0.5f);
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0 || str.equals("null");
    }

    /**
     * 格式化小说内容。
     * <p/>
     * <li>小说的开头，缩进2格。在开始位置，加入2格空格。
     * <li>所有的段落，缩进2格。所有的\n,替换为2格空格。
     */
    public static String formatContent(String str) {
        str = str.replaceAll("[ ]*", "");//替换来自服务器上的，特殊空格
        str = str.replaceAll("[ ]*", "");//
        str = str.replace("\n\n", "\n");
        str = str.replace("\n", "\n" + "\u3000\u3000");
        str = "\u3000\u3000" + str;
        return str;
    }

    public static String formatWordCount(int wordCount) {
        if (wordCount / 10000 > 0) {
            return (int) ((wordCount / 10000f) + 0.5) + "万字";
        } else if (wordCount / 1000 > 0) {
            return (int) ((wordCount / 1000f) + 0.5) + "千字";
        } else {
            return wordCount + "字";
        }
    }

    /**
     * 根据时间字符串获取描述性时间，如3分钟前，1天前
     *
     * @param dateString 时间字符串
     * @return 1天前
     */
    public static String getDescriptionTimeFromDateString(String dateString) {
        if (TextUtils.isEmpty(dateString))
            return "";
        try {
            return getDescriptionTimeFromDate(SDF.parse(formatZhuiShuDateString(dateString)));
        } catch (Exception ignored) {
        }
        return "";
    }

    public static String getDescriptionTimeFromTimeMills(long timeMills) {
        return getDescriptionTimeFromDate(new Date(timeMills));
    }

    /**
     * 格式化追书神器返回的时间字符串
     */
    private static String formatZhuiShuDateString(String dateString) {
        return dateString.replaceAll("T", " ").replaceAll("Z", "");
    }

    public static long getTimeFormDateString(String dateString) {
        try {
            return SDF.parse(formatZhuiShuDateString(dateString)).getTime();
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * 根据Date获取描述性时间，如3分钟前，1天前
     *
     * @param date date
     * @return 3分钟前
     */
    private static String getDescriptionTimeFromDate(Date date) {
        long delta = new Date().getTime() - date.getTime();
        if (delta < ONE_MINUTE) {
            long seconds = toSeconds(delta);
            return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
        }
        if (delta < 45L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
        }
        if (delta < 24L * ONE_HOUR) {
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
        }
        if (delta < 48L * ONE_HOUR) {
            return "昨天";
        }
        if (delta < 30L * ONE_DAY) {
            long days = toDays(delta);
            return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
        }
        if (delta < 12L * 4L * ONE_WEEK) {
            long months = toMonths(delta);
            return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
        } else {
            long years = toYears(delta);
            return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
        }
    }

    private static long toSeconds(long date) {
        return date / 1000L;
    }

    private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }

    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }

    private static long toDays(long date) {
        return toHours(date) / 24L;
    }

    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }

    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }

    /**
     * 获得当前屏幕亮度值 0~100
     */
    public static int getScreenBrightness(Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams lp = localWindow.getAttributes();
        float screenBrightness = lp.screenBrightness;
        return (int) (screenBrightness / 255f * 100f);
    }

    public static void setScreenBrightness(int brightness, Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams lp = localWindow.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            if (brightness <= 5) {
                brightness = 5;
            }
            lp.screenBrightness = brightness / 100.0F;
        }
        localWindow.setAttributes(lp);
    }

    @TargetApi(19)
    private static void setFlag(Activity activity, int flag) {
        View decorView = activity.getWindow().getDecorView();
        int option = decorView.getSystemUiVisibility() | flag;
        decorView.setSystemUiVisibility(option);
    }

    //取消flag
    @TargetApi(19)
    private static void clearFlag(Activity activity, int flag) {
        View decorView = activity.getWindow().getDecorView();
        int option = decorView.getSystemUiVisibility() & (~flag);
        decorView.setSystemUiVisibility(option);
    }

    @TargetApi(19)
    public static void hideUnStableStatusBar(Activity activity) {
        //设置隐藏StatusBar(点击任意地方会恢复)
        setFlag(activity, UNSTABLE_STATUS);
    }

    public static void showUnStableStatusBar(Activity activity) {
        clearFlag(activity, UNSTABLE_STATUS);
    }

    public static void showUnStableNavBar(Activity activity) {
        clearFlag(activity, UNSTABLE_NAV);
    }

    public static void hideStableStatusBar(Activity activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_STATUS);
    }

    public static void showStableStatusBar(Activity activity) {
        clearFlag(activity, STABLE_STATUS);
    }

    public static void hideStableNavBar(Activity activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_NAV);
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight() {
        Resources resources = MyApp.getContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取虚拟按键的高度
     */
    public static int getNavigationBarHeight() {
        int navigationBarHeight = 0;
        Resources rs = MyApp.getContext().getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && hasNavigationBar()) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    /**
     * 是否存在虚拟按键
     */
    private static boolean hasNavigationBar() {
        boolean hasNavigationBar = false;
        Resources rs = MyApp.getContext().getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception ignored) {
        }
        return hasNavigationBar;
    }

    public static void deleteBookCache(String bookId) {
        AppSettings.deleteReadProgress(bookId);
        AppSettings.deleteChapterList(bookId);
        FileUtils.deleteBookDir(bookId);
    }
}
