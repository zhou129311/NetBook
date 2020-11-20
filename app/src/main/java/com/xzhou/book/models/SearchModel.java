package com.xzhou.book.models;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xzhou.book.utils.SPUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchModel {

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
            put("www.kbiquge.com", ParseType.PARSE_TYPE_1);
            put("www.xbiquge.la", ParseType.PARSE_TYPE_1);
            put("www.bequge.com", ParseType.PARSE_TYPE_1);
            put("www.biquguan.com", ParseType.PARSE_TYPE_1);
            put("www.biquge.cc", ParseType.PARSE_TYPE_1);
            put("www.lindiankanshu.com", ParseType.PARSE_TYPE_1);
            put("www.xs.la", ParseType.PARSE_TYPE_1);
            put("www.xinxs84.com", ParseType.PARSE_TYPE_1);
            put("www.xs84.me", ParseType.PARSE_TYPE_1);
            put("baishuzhai.com", ParseType.PARSE_TYPE_1);
            put("www.dingdian.me", ParseType.PARSE_TYPE_1);
            put("www.miaoshufang.com", ParseType.PARSE_TYPE_1);
            put("www.dushu66.com", ParseType.PARSE_TYPE_1);
            put("www.biqutxt.com", ParseType.PARSE_TYPE_1);
            put("www.fenghuaju.cc", ParseType.PARSE_TYPE_1);
            put("www.xuanhuanwu.com", ParseType.PARSE_TYPE_1);
            put("www.sanjiangge.com", ParseType.PARSE_TYPE_1);
            put("www.u33.cc", ParseType.PARSE_TYPE_1);
            put("www.78zw.com", ParseType.PARSE_TYPE_1);
            put("www.tianyabook.com", ParseType.PARSE_TYPE_1);
            put("www.a306.com", ParseType.PARSE_TYPE_1);
            put("www.xszww.com", ParseType.PARSE_TYPE_1);
            put("www.biqu6.com", ParseType.PARSE_TYPE_1);
            put("www.cdzdgw.com", ParseType.PARSE_TYPE_1);
            put("www.biqugemm.com", ParseType.PARSE_TYPE_1);
            put("www.xs222.tw", ParseType.PARSE_TYPE_1);
            put("www.zanghaihuatxt.com", ParseType.PARSE_TYPE_1);
            put("www.wuxianxs.com", ParseType.PARSE_TYPE_1);
            put("www.xs98.com", ParseType.PARSE_TYPE_1);
            put("www.quledu.net", ParseType.PARSE_TYPE_1);
            put("www.52bqg.com", ParseType.PARSE_TYPE_1);
            put("www.dingdianxs.com", ParseType.PARSE_TYPE_1);
            put("www.qizi.cc", ParseType.PARSE_TYPE_1);
            put("www.ihxs.la", ParseType.PARSE_TYPE_1);
            put("www.qqxsw.la", ParseType.PARSE_TYPE_1);
//            put("www.123du.cc", ParseType.PARSE_TYPE_1);

            put("www.23us.la", ParseType.PARSE_TYPE_2);
            put("www.lwtxt.cc", ParseType.PARSE_TYPE_2);
            put("www.oldtimes.cc", ParseType.PARSE_TYPE_2);
            put("www.milepub.com", ParseType.PARSE_TYPE_2);
            put("www.x82xs.com", ParseType.PARSE_TYPE_2);
            put("www.xhxswz.com", ParseType.PARSE_TYPE_2);
            put("www.aoyuge.com", ParseType.PARSE_TYPE_2);
            put("www.52dus.com", ParseType.PARSE_TYPE_2);
            put("www.tianyibook.la", ParseType.PARSE_TYPE_2);
            put("www.77dushu.com", ParseType.PARSE_TYPE_2);
            put("www.biquger.com", ParseType.PARSE_TYPE_2);
            put("www.qqxs.cc", ParseType.PARSE_TYPE_2);
            put("www.tuhaoxs.com", ParseType.PARSE_TYPE_2);
            put("www.hongyeshuzhai.com", ParseType.PARSE_TYPE_2);

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
            put("www.35xs.com", ParseType.PARSE_TYPE_4);
            put("www.lwxslwxs.com", ParseType.PARSE_TYPE_4);
            put("www.dashubao.la", ParseType.PARSE_TYPE_4);
            put("www.lewentxt.com", ParseType.PARSE_TYPE_4);
            put("www.lingyu.org", ParseType.PARSE_TYPE_4);
            put("www.w23us.com", ParseType.PARSE_TYPE_4);
            put("www.16sy.com", ParseType.PARSE_TYPE_4);
            put("www.23wx.cm", ParseType.PARSE_TYPE_4);
            put("www.yanqingshu.com", ParseType.PARSE_TYPE_4);
            put("www.i7wx.com", ParseType.PARSE_TYPE_4);
            put("www.qianqianxs.com", ParseType.PARSE_TYPE_4);
            put("www.xqianqian.com", ParseType.PARSE_TYPE_4);
            put("www.duokan8.com", ParseType.PARSE_TYPE_4);
            put("www.heiyan.org", ParseType.PARSE_TYPE_4);
            put("www.39shubao.com", ParseType.PARSE_TYPE_4);
            put("www.shupengwang.com", ParseType.PARSE_TYPE_4);
            put("www.biquw.com", ParseType.PARSE_TYPE_4);
//            put("www.daizhuzai.com", ParseType.PARSE_TYPE_4);
//            put("www.biqige.cc", ParseType.PARSE_TYPE_4);
//            put("www.38lu.com", ParseType.PARSE_TYPE_4);
//            put("www.zhetian.org", ParseType.PARSE_TYPE_4);

            put("www.fpzw.com", ParseType.PARSE_TYPE_5);
            put("www.biqukan.com", ParseType.PARSE_TYPE_5);
            put("www.touxiang.la", ParseType.PARSE_TYPE_5);
            put("www.dushuzhe.com", ParseType.PARSE_TYPE_5);
            put("www.biqudu.com", ParseType.PARSE_TYPE_5);
            put("www.bixiadu.com", ParseType.PARSE_TYPE_5);
            put("www.wenshulou.cc", ParseType.PARSE_TYPE_5);
            put("www.e8zw.com", ParseType.PARSE_TYPE_5);
            put("www.173kt.net", ParseType.PARSE_TYPE_5);
            put("www.abcxs.com", ParseType.PARSE_TYPE_5);
            put("www.aixiashu.com", ParseType.PARSE_TYPE_5);
            put("www.3zm.la", ParseType.PARSE_TYPE_5);
            put("www.beidouxin.com", ParseType.PARSE_TYPE_5);
            put("www.zhuaji.org", ParseType.PARSE_TYPE_5);
            put("www.miaojiang8.com", ParseType.PARSE_TYPE_5);
            put("www.bishenge.com", ParseType.PARSE_TYPE_5);
            put("www.xbiquge6.com", ParseType.PARSE_TYPE_5);
            put("www.22xs.net", ParseType.PARSE_TYPE_5);

            put("www.okdd.net", ParseType.PARSE_TYPE_6);
            put("www.mywenxue.com", ParseType.PARSE_TYPE_6);
            put("www.f96.la", ParseType.PARSE_TYPE_6);
            put("www.1dwx.com", ParseType.PARSE_TYPE_6);

//            put("www.ggdown.org", PARSE_GGD);

//            put("fm.x88dushu.com", ParseType.PARSE_TYPE_6);
        }
    };

    public static class HostType {
        public static final Type TYPE = new TypeToken<List<HostType>>() {
        }.getType();

        public String host;
        public @ParseType
        int parseType;
    }

    public static void initSupportParseMap() {
        List<HostType> list = getHostTypeList();
        if (list != null) {
            for (HostType hostType : list) {
                BOOK_HOSTS.put(hostType.host, hostType.parseType);
            }
        }
    }

    public static List<HostType> getHostTypeList() {
        List<HostType> list = null;
        String listStr = SPUtils.get().getString("support_local_read_host");
        if (listStr != null) {
            try {
                list = new Gson().fromJson(listStr, HostType.TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void saveHostType(HostType hostType) {
        if (hostType == null) {
            return;
        }
        BOOK_HOSTS.put(hostType.host, hostType.parseType);
        List<HostType> list = getHostTypeList();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(hostType);
        String listStr = null;
        try {
            listStr = new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SPUtils.get().putString("support_local_read_host", listStr);
    }

    public static boolean hasSupportLocalRead(String host) {
        return BOOK_HOSTS.containsKey(host);
    }

    public static Integer getType(String host) {
        return BOOK_HOSTS.get(host);
    }

    public static class SearchBook {
        public static final Type TYPE = new TypeToken<List<SearchBook>>() {
        }.getType();
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
        public boolean isParsing;
        public String parseText = "开始解析";
        public String desc;
        public String tag;

        public SearchBook() {
        }

        public boolean hasValid() {
            boolean valid = !TextUtils.isEmpty(readUrl) && !TextUtils.isEmpty(bookName) && !TextUtils.isEmpty(sourceHost);
            if (valid) {
                id = String.valueOf((bookName + "_" + readUrl).hashCode());
            }
            return valid;
        }

        @Override
        public String toString() {
            return "SearchBook{" +
                    "id='" + id + '\'' +
                    "image='" + image + '\'' +
                    ", sourceName='" + sourceName + '\'' +
                    ", sourceHost='" + sourceHost + '\'' +
                    ", author='" + author + '\'' +
                    ", bookName='" + bookName + '\'' +
                    ", readUrl='" + readUrl + '\'' +
                    ", latestChapterName='" + latestChapterName + '\'' +
                    ", latestChapterUrl='" + latestChapterUrl + '\'' +
                    ", tag='" + tag + '\'' +
                    '}';
        }
    }

}
