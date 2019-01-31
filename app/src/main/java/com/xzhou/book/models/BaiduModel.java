package com.xzhou.book.models;

import android.annotation.IntDef;
import android.text.TextUtils;

import java.util.HashMap;

public class BaiduModel {

    @IntDef({ ParseType.PARSE_TYPE_1, ParseType.PARSE_TYPE_2, ParseType.PARSE_TYPE_3, ParseType.PARSE_TYPE_4 })
    public @interface ParseType {
        int PARSE_TYPE_1 = 1; //tianxiabachang booktxt
        int PARSE_TYPE_2 = 2; //lwtxt
        int PARSE_TYPE_3 = 3; //x4399
        int PARSE_TYPE_4 = 4; //boluoxs
    }

    private static final HashMap<String, Integer> BOOK_HOSTS = new HashMap<String, Integer>() {
        {
            put("www.tianxiabachang.cn", ParseType.PARSE_TYPE_1);
            put("www.booktxt.net", ParseType.PARSE_TYPE_1); // https://www.booktxt.net/5_5784/
            put("www.lwtxt.cc", ParseType.PARSE_TYPE_2);
            put("www.x4399.com", ParseType.PARSE_TYPE_3);
//            put("www.oldtimes.cc", PARSE_LWTX);
//            put("m.ggdown.org", PARSE_GGD);
//            put("www.ggdown.org", PARSE_GGD);
            put("www.boluoxs.com", ParseType.PARSE_TYPE_4);
        }
    };

    public static boolean hasSupportLocalRead(String host) {
        return BOOK_HOSTS.containsKey(host);
    }

    public static Integer getType(String host) {
        return BOOK_HOSTS.get(host);
    }

    public static class BaiduBook {
        public String image;
        public String sourceName;
        public String sourceHost;
        public String author;
        public String bookName;
        public String readUrl;
        public String latestChapterName;
        public String latestChapterUrl;
        public String id;
        public long updated;

        public BaiduBook() {
        }

        public boolean hasValid() {
            boolean valid = !TextUtils.isEmpty(readUrl) && !TextUtils.isEmpty(bookName) && !TextUtils.isEmpty(sourceHost);
            if (valid) {
                id = bookName + "_" + readUrl;
            }
            return valid;
        }

        @Override
        public String toString() {
            return "BaiduBook{" +
                    "id='" + id + '\'' +
                    "image='" + image + '\'' +
                    ", sourceName='" + sourceName + '\'' +
                    ", sourceHost='" + sourceHost + '\'' +
                    ", author='" + author + '\'' +
                    ", bookName='" + bookName + '\'' +
                    ", readUrl='" + readUrl + '\'' +
                    ", latestChapterName='" + latestChapterName + '\'' +
                    ", latestChapterUrl='" + latestChapterUrl + '\'' +
                    '}';
        }
    }

}
