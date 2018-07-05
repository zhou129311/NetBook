package com.xzhou.book.utils;

import com.xzhou.book.MyApp;

public class AppUtils {

    public static String getString(int resId) {
        return MyApp.getContext().getResources().getString(resId);
    }

    public static int getColor(int resId) {
        return MyApp.getContext().getResources().getColor(resId);
    }

}
