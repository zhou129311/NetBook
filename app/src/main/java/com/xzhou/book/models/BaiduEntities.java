package com.xzhou.book.models;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;

public class BaiduEntities {
    public static final int PARSE_BQG = 0; //笔趣阁
    public static final int PARSE_LWTX = 1; // 乐文
    public static final int PARSE_GGD = 2; //格格党
    public static final int PARSE_BLXS = 3; //菠萝
    public static final int PARSE_DDXS = 4; //顶点
    public static final int PARSE_TXBC = 5; //

    public static final HashMap<String, Integer> BOOK_HOSTS = new HashMap<String, Integer>() {
        {
            put("www.tianxiabachang.cn", PARSE_TXBC);
            put("www.booktxt.net", PARSE_TXBC); // https://www.booktxt.net/5_5784/
            put("wap.x4399.com", PARSE_BQG);
            put("www.x4399.com", PARSE_BQG);
            put("www.lwtxt.cc", PARSE_LWTX);
            put("www.oldtimes.cc", PARSE_LWTX);
            put("m.ggdown.org", PARSE_GGD);
            put("www.ggdown.org", PARSE_GGD);
            put("m.boluoxs.com", PARSE_BLXS);
            put("www.boluoxs.com", PARSE_BLXS);
        }
    };

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
        public List<Entities.Chapters> chaptersList;

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
