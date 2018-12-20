package com.xzhou.book.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.xzhou.book.MyApp;

import java.io.Closeable;
import java.io.IOException;

public class AppUtils {

    public static String getString(int resId) {
        return MyApp.getContext().getResources().getString(resId);
    }

    public static String getString(int resId, Object... args) {
        return MyApp.getContext().getResources().getString(resId, args);
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

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
}
