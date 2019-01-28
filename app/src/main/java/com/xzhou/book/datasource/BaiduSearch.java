package com.xzhou.book.datasource;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.xzhou.book.models.BaiduEntities;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class BaiduSearch {
    private static final String TAG = "BaiduSearch";

    public static class Title {
        public String title;
        public String url;

        @Override
        public String toString() {
            return "Title{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public static List<BaiduEntities.BaiduBook> parseSearchKey(String key) {
        List<BaiduEntities.BaiduBook> bookList = null;
        try {
            trustEveryone();
            try {
                key = URLEncoder.encode(key, "gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
            //解析Url获取Document对象
//            String url = "http://www.baidu.com/link?url=QFDcvZ5H1HuhHjrCGhVr9VEkxEjE8-h1rHyAfi-AFt9NptKsRDfWL-I5IejevLXP";
//            String url = "https://m.boluoxs.com/book/271.html";
            Document document = Jsoup.connect(url).timeout(10000).get();
            //获取网页源码文本内容
//            logd(TAG, "document:" + document.toString());
//            获取指定class的内容指定tag的元素
//            Elements elements = document.getAllElements();
//            logd(TAG, "elements:" + elements.toString());
//            Elements divs = document.getElementsByTag("div");
//            for (Element e : divs) {
//                logi(TAG, "divs=" + e.toString());
//            }
            Elements result = document.getElementsByClass("result c-container ");
            logd(TAG, "title=" + document.title() + ",result size=" + result.size());
            bookList = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                Element e = result.get(i);
                logi(TAG, "result id=" + e.id());
//                logd(TAG, "result e=" + e.html());
                Elements f13 = e.getElementsByClass("f13");
//                logd(TAG, "result f13=" + f13);
                for (int j = 0; j < f13.size(); j++) {
                    Element child = f13.get(j);
                    //Elements tag = child.getElementsByTag("a");
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Title t = new Gson().fromJson(title, Title.class);
                    logd(TAG, "title=" + t.toString());
//                    if (e.id().equals("1")) {
                    parseResult(t.url, bookList);
//                    }
                    //logd(TAG, "cover=" + tag.attr("href"));
                }
//                logd(TAG, "result html=" + e.html());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookList;
    }

    private static void parseResult(String link, List<BaiduEntities.BaiduBook> bookList) {
        try {
            //解析Url获取Document对象
            Document document = Jsoup.connect(link).get();
            //获取网页源码文本内容
//            logd(TAG, "document:" + document.toString());
            Element head = document.head();
//            logi(TAG, "head:" + head.toString());
            Elements metas = head.getElementsByTag("meta");
//            logi(TAG, "metas:" + metas.toString());
            BaiduEntities.BaiduBook book = new BaiduEntities.BaiduBook();
            for (Element meta : metas) {
//                String type = meta.attr("http-equiv");
//                String content = meta.attr("content");
//                logi(TAG, "type = " + type + ",content=" + content);
                Attributes attributes = meta.attributes();
                logi(TAG, "meta = " + attributes.toString() + ",size = " + attributes.size());
                String property = attributes.get("property");
                if (property == null) {
                    continue;
                }
                if (property.equals("og:novel:author")) {
                    book.author = attributes.get("content");
                } else if (property.equals("og:novel:book_name")) {
                    book.bookName = attributes.get("content");
                } else if (property.equals("og:novel:read_url")) {
                    book.sourceHost = book.readUrl = attributes.get("content");
                } else if (property.equals("og:novel:latest_chapter_name")) {
                    book.latestChapterName = attributes.get("content");
                } else if (property.equals("og:novel:latest_chapter_url")) {
                    book.latestChapterUrl = attributes.get("content");
                } else if (property.equals("og:image")) {
                    book.image = attributes.get("content");
                } else if ("mobile-agent".equals(attributes.get("name")) || "mobile-agent".equals(attributes.get("http-equiv"))) {
                    String content = attributes.get("content");
                    if (content != null && content.contains("url=")) {
                        book.mobReadUrl = content.substring(content.indexOf("url=")).replace("url=", "");
                        logi(TAG, "mobReadUrl=" + book.mobReadUrl);
                    }
                }
            }
            Element body = document.body();
            Elements header_logo = body.getElementsByClass("header_logo");
//            logi(TAG, "body=" + body);
            if (header_logo != null) {
                for (Element h : header_logo) {
                    Elements a = h.getElementsByTag("a");
                    if (a != null) {
                        book.sourceName = a.text();
                        String host = a.attr("href");
                        if (!TextUtils.isEmpty(host) && host.length() > 3) {
                            book.sourceHost = host;
                        }
                        break;
                    }
                }
            }

            if (book.hasValid()) {
                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logd(String tag, String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.d(tag, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.d(tag, str);
    }

    public static void logi(String tag, String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(tag, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(tag, str);
    }
}
