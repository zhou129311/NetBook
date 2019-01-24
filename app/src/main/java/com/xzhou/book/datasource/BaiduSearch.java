package com.xzhou.book.datasource;

import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BaiduSearch {
    private static final String TAG = "BaiduSearch";

    public static void parse(String key) {
        try {
//            try {
//                key = URLEncoder.encode(key, "gb2312");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
            //解析Url获取Document对象
            String url = "http://www.baidu.com/link?url=QFDcvZ5H1HuhHjrCGhVr9VEkxEjE8-h1rHyAfi-AFt9NptKsRDfWL-I5IejevLXP";
            Document document = Jsoup.connect(url).get();
            //获取网页源码文本内容
            //System.out.println(document.toString());
            Log.i(TAG, document.toString());
            //获取指定class的内容指定tag的元素
            //Elements elements = document.getAllElements();
//            Elements divs = document.getElementsByTag("div");
            Elements result = document.getElementsByClass("result c-container ");
            Log.i(TAG, "title=" + document.title() + ",result size=" + result.size());
//            for (Element e : divs) {
//                Log.i(TAG, "divs=" + e.toString());
//            }
            for (int i = 0; i < result.size(); i++) {
                Element e = result.get(i);
                Log.i(TAG, "result id=" + e.id());
//                Log.i(TAG, "result e=" + e.html());
                Elements f13 = e.getElementsByClass("f13");
//                Log.i(TAG, "result f13=" + f13);
                for (int j = 0; j < f13.size(); j++) {
                    Element child = f13.get(j);
                    //Elements tag = child.getElementsByTag("a");
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Log.i(TAG, "title=" + title);
                    //Log.i(TAG, "cover=" + tag.attr("href"));
                }
//                Log.i(TAG, "result html=" + e.html());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
