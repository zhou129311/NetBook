package com.xzhou.book.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.xzhou.book.MyApp;

import java.io.Closeable;
import java.io.IOException;

public class AppUtils {

    public static String getString(int resId) {
        return MyApp.getContext().getResources().getString(resId);
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

    public static DisplayMetrics getDisplayMetrics(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            return dm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getScreenWidth() {
        DisplayMetrics dm = getDisplayMetrics(MyApp.getContext());
        return dm == null ? 0 : dm.widthPixels;
    }
}
