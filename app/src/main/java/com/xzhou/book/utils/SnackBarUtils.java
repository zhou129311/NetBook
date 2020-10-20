package com.xzhou.book.utils;

import androidx.annotation.ColorInt;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;

public class SnackBarUtils {

    private Snackbar mSnackBar;

    private SnackBarUtils(Snackbar snackbar) {
        mSnackBar = snackbar;
    }

    public static SnackBarUtils makeShort(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        return new SnackBarUtils(snackbar);
    }

    public static SnackBarUtils makeLong(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        return new SnackBarUtils(snackbar);
    }

    public static SnackBarUtils makeIndefinite(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
        return new SnackBarUtils(snackbar);
    }

    public void show(@ColorInt int color) {
        setSnackBarBackColor(color).show();
    }

    public void setText(String text) {
        if (mSnackBar != null) {
            mSnackBar.setText(text);
        }
    }

    public void show() {
        if (mSnackBar != null && !mSnackBar.isShown()) {
            mSnackBar.show();
        }
    }

    public void hide() {
        if (mSnackBar != null && mSnackBar.isShown()) {
            mSnackBar.dismiss();
        }
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
        View snackBarView = getSnackBarLayout(mSnackBar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(color);
        }
        return mSnackBar;
    }
}