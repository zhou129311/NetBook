package com.xzhou.book.utils;

import android.annotation.StringRes;
import android.content.Context;
import android.widget.Toast;

import com.xzhou.book.MyApp;

public class ToastUtils {
    private static Toast mToast;

    public static void release() {
        mToast = null;
    }

    public static void showShortToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApp.getContext(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showShortToast(@StringRes int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApp.getContext(), AppUtils.getString(resId), Toast.LENGTH_SHORT);
        } else {
            mToast.setText(AppUtils.getString(resId));
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showLongToast(String text) {
        Context context = MyApp.getContext();
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    public static void showLongToast(@StringRes int resId) {
        Context context = MyApp.getContext();
        if (mToast == null) {
            mToast = Toast.makeText(context, AppUtils.getString(resId), Toast.LENGTH_SHORT);
        } else {
            mToast.setText(AppUtils.getString(resId));
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
