package com.xzhou.book.models;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.xzhou.book.utils.Log;

import java.util.HashMap;

public class BaiduModel {

    @IntDef({ParseType.PARSE_TYPE_1, ParseType.PARSE_TYPE_2, ParseType.PARSE_TYPE_3, ParseType.PARSE_TYPE_4
            , ParseType.PARSE_TYPE_5, ParseType.PARSE_TYPE_6})
    public @interface ParseType {
        int PARSE_TYPE_1 = 1;
        int PARSE_TYPE_2 = 2;
        int PARSE_TYPE_3 = 3;
        int PARSE_TYPE_4 = 4;
        int PARSE_TYPE_5 = 5;
        int PARSE_TYPE_6 = 6;
    }

    private static final HashMap<String, Integer> BOOK_HOSTS = new HashMap<String, Integer>() {
        {
            put("www.tianxiabachang.cn", ParseType.PARSE_TYPE_1);
            put("www.yangguiweihuo.com", ParseType.PARSE_TYPE_1);
            put("www.biqugexsw.com", ParseType.PARSE_TYPE_1);
            put("www.biqugex.com", ParseType.PARSE_TYPE_1);
            put("www.biquge.lu", ParseType.PARSE_TYPE_1);
            put("www.biquge.info", ParseType.PARSE_TYPE_1);
            put("www.booktxt.net", ParseType.PARSE_TYPE_1);
            put("www.booktxt.com", ParseType.PARSE_TYPE_1);
            put("www.x4399.com", ParseType.PARSE_TYPE_1);
            put("www.qu.la", ParseType.PARSE_TYPE_1);
            put("www.uxiaoshuo.com", ParseType.PARSE_TYPE_1);
            put("www.siluke.tw", ParseType.PARSE_TYPE_1);
            put("www.xinremenxs.com", ParseType.PARSE_TYPE_1);
            put("www.dingdiann.com", ParseType.PARSE_TYPE_1);
            put("www.sbiquge.com", ParseType.PARSE_TYPE_1);
            put("www.fhxiaoshuo.com", ParseType.PARSE_TYPE_1);
            put("www.shu008.com", ParseType.PARSE_TYPE_1);
            put("www.518east.com", ParseType.PARSE_TYPE_1);
            put("www.cilook.net", ParseType.PARSE_TYPE_1);
            put("www.biqukan.com", ParseType.PARSE_TYPE_1);
            put("www.biqudu.com", ParseType.PARSE_TYPE_1);
            put("www.kbiquge.com", ParseType.PARSE_TYPE_1);
            put("www.xbiquge.la", ParseType.PARSE_TYPE_1);
            put("www.bequge.com", ParseType.PARSE_TYPE_1);
            put("www.biquguan.com", ParseType.PARSE_TYPE_1);
            put("www.biquge.cc", ParseType.PARSE_TYPE_1);
            put("www.lindiankanshu.com", ParseType.PARSE_TYPE_1);
            put("www.xs.la", ParseType.PARSE_TYPE_1);
//            put("www.123du.cc", ParseType.PARSE_TYPE_1);

            put("www.23us.la", ParseType.PARSE_TYPE_2);
            put("www.lwtxt.cc", ParseType.PARSE_TYPE_2);
            put("www.oldtimes.cc", ParseType.PARSE_TYPE_2);
            put("www.milepub.com", ParseType.PARSE_TYPE_2);
            put("www.x82xs.com", ParseType.PARSE_TYPE_2);

            put("www.fengyunok.com", ParseType.PARSE_TYPE_3);

            put("www.kenshu.cc", ParseType.PARSE_TYPE_4);
            put("www.kuaiyankanshu.net", ParseType.PARSE_TYPE_4);
            put("www.boluoxs.com", ParseType.PARSE_TYPE_4);
            put("www.qiushuzw.com", ParseType.PARSE_TYPE_4);
            put("www.boquge.com", ParseType.PARSE_TYPE_4);
            put("www.x88dushu.com", ParseType.PARSE_TYPE_4);
            put("www.qisuu.la", ParseType.PARSE_TYPE_4);
            put("www.xshuoshuo.com", ParseType.PARSE_TYPE_4);
            put("www.t7yyw.com", ParseType.PARSE_TYPE_4);
            put("www.31wxw8.com", ParseType.PARSE_TYPE_4);
            put("www.tsxsw.com", ParseType.PARSE_TYPE_4);
            put("www.81xzw.com", ParseType.PARSE_TYPE_4);
            put("www.dushu.kr", ParseType.PARSE_TYPE_4);
//            put("www.zhetian.org", ParseType.PARSE_TYPE_4);

            put("www.fpzw.com", ParseType.PARSE_TYPE_5);

//            put("www.ggdown.org", PARSE_GGD);


//            put("fm.x88dushu.com", ParseType.PARSE_TYPE_6);
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
            } else {
                Log.i("Book", "invalid book = " + toString());
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
