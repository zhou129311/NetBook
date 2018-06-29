package com.xzhou.book;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = "不知道是什么"; //查询关键字
                try {
                    key = URLEncoder.encode(key, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
                parse(url);
            }
        }).start();
    }

    public static void parse(String url) {
        try {
            //解析Url获取Document对象
            Document document = Jsoup.connect(url).get();
            //获取网页源码文本内容
            //System.out.println(document.toString());
            //Log.i("zx",document.toString());
            //获取指定class的内容指定tag的元素
            //Elements elements = document.getAllElements();
//            Elements divs = document.getElementsByTag("div");
            Elements result = document.getElementsByClass("result c-container ");
            Log.i("zx", "title=" + document.title() + ",result size=" + result.size());
//            for (Element e : divs) {
//                Log.i("zx", "divs=" + e.toString());
//            }
            for (int i = 0; i < result.size(); i++) {
                Element e = result.get(i);
                Log.i("zx", "result id=" + e.id());
//                Log.i("zx", "result e=" + e.html());
                Elements f13 = e.getElementsByClass("f13");
//                Log.i("zx", "result f13=" + f13);
                for (int j = 0; j < f13.size(); j++) {
                    Element child = f13.get(j);
                    //Elements tag = child.getElementsByTag("a");
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Log.i("zx", "title=" + title);
                    //Log.i("zx", "url=" + tag.attr("href"));
                }
//                Log.i("zx", "result html=" + e.html());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
