package com.xzhou.book.utils;

import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;

public class SnackBarUtils {

    private Snackbar mSnackbar;

    private SnackBarUtils(Snackbar snackbar) {
        mSnackbar = snackbar;
    }

    public static SnackBarUtils makeShort(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        return new SnackBarUtils(snackbar);
    }

    public static SnackBarUtils makeLong(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        return new SnackBarUtils(snackbar);
    }

    public void show(@ColorInt int color) {
        setSnackBarBackColor(color).show();
        mSnackbar.show();
    }

    public void show(@ColorInt int color, String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(color)
                .setActionTextColor(MyApp.getContext().getResources().getColor(R.color.white))
                .setAction(actionText, listener)
                .show();
    }

    private View getSnackBarLayout(Snackbar snackbar) {
        if (snackbar != null) {
            return snackbar.getView();
        }
        return null;

    }

    private Snackbar setSnackBarBackColor(@ColorInt int color) {
        View snackBarView = getSnackBarLayout(mSnackbar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(color);
        }
        return mSnackbar;
    }
}