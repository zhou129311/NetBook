package com.xzhou.book.utils;

import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {
    public static final String ISNIGHT = "isNight";

    public static final String ISBYUPDATESORT = "isByUpdateSort";
    public static final String FLIP_STYLE = "flipStyle";

    public static final String SUFFIX_TXT = ".txt";
    public static final String SUFFIX_PDF = ".pdf";
    public static final String SUFFIX_EPUB = ".epub";
    public static final String SUFFIX_ZIP = ".zip";
    public static final String SUFFIX_CHM = ".chm";

    public static final int ITEM_TYPE_NET_BOOK = 1;
    public static final int ITEM_TYPE_TEXT = 2;
    public static final int ITEM_TYPE_TEXT_IMAGE = 3;
    public static final int ITEM_TYPE_TEXT_IMAGE_2 = 4;
    public static final int ITEM_TYPE_TEXT_GRID = 5;
    public static final int ITEM_TYPE_REVIEWS = 6;
    public static final int ITEM_TYPE_NET_BOOK_LIST = 7;
    public static final int ITEM_TYPE_BOOK_BY_AUTHOR = 8;
    public static final int ITEM_TYPE_BOOK_BY_TAG = 9;
    public static final int ITEM_TYPE_BOOK_BY_SEARCH = 10;
    public static final int ITEM_TYPE_DISCUSSION = 11;
    public static final int ITEM_TYPE_LOCAL_BOOK = 12;
    public static final int ITEM_TYPE_COMMENT = 13;
    public static final int ITEM_TYPE_HELPFUL = 14;
    public static final int ITEM_TYPE_VOTE = 15;
    public static final int ITEM_TYPE_POSTS_REVIEW = 16;
    public static final int ITEM_TYPE_POSTS_HELP = 17;

    public static final int[] tagColors = new int[] {
            Color.parseColor("#90C5F0"),
            Color.parseColor("#91CED5"),
            Color.parseColor("#F88F55"),
            Color.parseColor("#C0AFD0"),
            Color.parseColor("#E78F8F"),
            Color.parseColor("#67CCB7"),
            Color.parseColor("#F6BC7E")
    };

    @IntDef({ ReadTheme.WHITE, ReadTheme.BROWN, ReadTheme.GREEN, })
    public @interface ReadTheme {
        int WHITE = 0;
        int BROWN = 1;
        int GREEN = 2;
    }

    @IntDef({ TabSource.SOURCE_RANK_SUB,
            TabSource.SOURCE_TOPIC_LIST,
            TabSource.SOURCE_CATEGORY_SUB,
            TabSource.SOURCE_AUTHOR,
            TabSource.SOURCE_TAG,
            TabSource.SOURCE_RECOMMEND,
            TabSource.SOURCE_COMMUNITY,
    })
    public @interface TabSource {
        int SOURCE_RANK_SUB = 0;
        int SOURCE_TOPIC_LIST = 1;
        int SOURCE_CATEGORY_SUB = 2;
        int SOURCE_AUTHOR = 3;
        int SOURCE_TAG = 4;
        int SOURCE_RECOMMEND = 5;
        int SOURCE_COMMUNITY = 6;
    }

    @StringDef({
            Gender.MALE,
            Gender.FEMALE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gender {
        String MALE = "male";
        String FEMALE = "female";
        String PICTURE = "picture";
        String PRESS = "press";
    }

    @StringDef({
            CateType.HOT,
            CateType.NEW,
            CateType.REPUTATION,
            CateType.OVER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface CateType {
        String HOT = "hot";
        String NEW = "new";
        String REPUTATION = "reputation";
        String OVER = "over";
    }

    @StringDef({
            Distillate.ALL,
            Distillate.DISTILLATE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Distillate {
        String ALL = "false";

        String DISTILLATE = "true";
    }

    @StringDef({
            SortType.DEFAULT,
            SortType.COMMENT_COUNT,
            SortType.CREATED,
            SortType.HELPFUL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortType {
        String DEFAULT = "updated";

        String CREATED = "created";

        String HELPFUL = "helpful";

        String COMMENT_COUNT = "comment-count";
    }

    @StringDef({
            BookType.ALL,
            BookType.XHQH,
            BookType.WXXX,
            BookType.DSYN,
            BookType.LSJS,
            BookType.YXJJ,
            BookType.KHLY,
            BookType.CYJK,
            BookType.HMZC,
            BookType.XDYQ,
            BookType.GDYQ,
            BookType.HXYQ,
            BookType.DMTR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BookType {
        String ALL = "all";

        String XHQH = "xhqh";

        String WXXX = "wxxx";

        String DSYN = "dsyn";

        String LSJS = "lsjs";

        String YXJJ = "yxjj";

        String KHLY = "khly";

        String CYJK = "cyjk";

        String HMZC = "hmzc";

        String XDYQ = "xdyq";

        String GDYQ = "gdyq";

        String HXYQ = "hxyq";

        String DMTR = "dmtr";
    }

    public static List<String> bookTypeParams = new ArrayList<String>() {{
        add(BookType.ALL);
        add(BookType.XHQH);
        add(BookType.WXXX);
        add(BookType.DSYN);
        add(BookType.LSJS);
        add(BookType.YXJJ);
        add(BookType.KHLY);
    }};

    public static List<String> bookTypes = new ArrayList<String>() {{
        add("全部类型");
        add("玄幻奇幻");
        add("武侠仙侠");
        add("都市异能");
        add("历史军事");
        add("游戏竞技");
        add("科幻灵异");
    }};

    public static Map<String, String> typeToText = new HashMap<String, String>() {{
        put("qt", "其他");
        put(BookType.XHQH, "玄幻奇幻");
        put(BookType.WXXX, "武侠仙侠");
        put(BookType.DSYN, "都市异能");
        put(BookType.LSJS, "历史军事");
        put(BookType.YXJJ, "游戏竞技");
        put(BookType.KHLY, "科幻灵异");
        put(BookType.CYJK, "穿越架空");
        put(BookType.HMZC, "豪门总裁");
        put(BookType.XDYQ, "现代言情");
        put(BookType.GDYQ, "古代言情");
        put(BookType.HXYQ, "幻想言情");
        put(BookType.DMTR, "耽美同人");
    }};
}
