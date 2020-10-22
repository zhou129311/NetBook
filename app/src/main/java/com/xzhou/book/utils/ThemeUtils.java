package com.xzhou.book.utils;

import com.xzhou.book.R;

import java.util.ArrayList;

public class ThemeUtils {

    public static final ArrayList<ReadTheme> THEME_LIST = new ArrayList<>();

    public static void init() {
        int dayContentColor = AppUtils.getColor(R.color.chapter_content_day);
        int dayTitleColor = AppUtils.getColor(R.color.chapter_content_day);
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.DEFAULT, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_df, -1, R.mipmap.reader_background_df_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.BROWN, dayContentColor, dayTitleColor,
                -1, AppUtils.getColor(R.color.read_theme_brown), R.drawable.sel_theme_brown));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.GREEN, dayContentColor, dayTitleColor,
                -1, AppUtils.getColor(R.color.read_theme_green), R.drawable.sel_theme_green));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.DZ, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_dz, -1, R.mipmap.reader_background_dz_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.HBB, AppUtils.getColor(R.color.chapter_content_hbb),
                AppUtils.getColor(R.color.chapter_title_hbb), R.mipmap.reader_background_hbb, -1, R.mipmap.reader_background_hbb_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.MW, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_mw, -1, R.mipmap.reader_background_mw_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.SM, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_sm, -1, R.mipmap.reader_background_sm_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.SS, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_ss, -1, R.mipmap.reader_background_ss_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.TH, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_th, -1, R.mipmap.reader_background_th_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.YM, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_ym, -1, R.mipmap.reader_background_ym_sm));
        THEME_LIST.add(new ReadTheme(Constant.ReadTheme.YPZ, dayContentColor, dayTitleColor,
                R.mipmap.reader_background_ypz, -1, R.mipmap.reader_background_ypz_sm));
    }

    public static class ReadTheme {
        public @Constant.ReadTheme
        int theme;
        public int contentTextColor;
        public int titleTextColor;
        public int bgResId;
        public int bgColor;
        public int smBgResId;

        public ReadTheme(int theme, int contentTextColor, int titleTextColor, int bgResId, int bgColor, int smBgResId) {
            this.theme = theme;
            this.contentTextColor = contentTextColor;
            this.titleTextColor = titleTextColor;
            this.bgResId = bgResId;
            this.bgColor = bgColor;
            this.smBgResId = smBgResId;
        }

        @Override
        public String toString() {
            return "ReadTheme{" +
                    "theme=" + theme +
                    ", contentTextColor=" + contentTextColor +
                    ", titleTextColor=" + titleTextColor +
                    ", bgResId=" + bgResId +
                    ", bgColor=" + bgColor +
                    ", smBgResId=" + smBgResId +
                    '}';
        }
    }

    public static int getThemeColor(@Constant.ReadTheme int theme) {
        int color;
        if (theme == Constant.ReadTheme.BROWN) {
            color = AppUtils.getColor(R.color.read_theme_brown);
        } else if (theme == Constant.ReadTheme.GREEN) {
            color = AppUtils.getColor(R.color.read_theme_green);
        } else {
            color = AppUtils.getColor(R.color.read_theme_white);
        }
        return color;
    }
}
