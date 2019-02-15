package com.xzhou.book.utils;

import com.xzhou.book.R;

public class ThemeUtils {

    public static int getThemeColor(@Constant.ReadTheme int theme) {
        int color = AppUtils.getColor(R.color.read_theme_white);
        switch (theme) {
        case Constant.ReadTheme.BROWN:
            color = AppUtils.getColor(R.color.read_theme_brown);
            break;
        case Constant.ReadTheme.GREEN:
            color = AppUtils.getColor(R.color.read_theme_green);
            break;
        }
        return color;
    }

}
