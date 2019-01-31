package com.xzhou.book.datasource;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xzhou.book.models.BaiduModel;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
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

    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
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

    public static List<BaiduModel.BaiduBook> parseSearchKey(String key) {
        List<BaiduModel.BaiduBook> bookList = null;
        try {
            trustEveryone();
            try {
                key = URLEncoder.encode(key, "gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
            Document document = Jsoup.connect(url).timeout(10000).get();
            Elements result = document.getElementsByClass("result c-container ");
            logd("title=" + document.title() + ",result size=" + result.size());
            bookList = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                Element e = result.get(i);
                logi("result id=" + e.id());
                Elements f13 = e.getElementsByClass("f13");
                for (int j = 0; j < f13.size(); j++) {
                    Element child = f13.get(j);
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Title t = new Gson().fromJson(title, Title.class);
                    logd("title=" + t.toString());
                    if (t.title.contains("网盘")) {
                        continue;
                    }
                    BaiduModel.BaiduBook book = parseResult(t);
                    if (book != null && book.hasValid()) {
                        if (urlInvalid(book.readUrl)) {
                            continue;
                        }
                        Log.i(TAG, "book = " + book);
                        bookList.add(book);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookList;
    }

    private static BaiduModel.BaiduBook parseResult(Title title) {
        BaiduModel.BaiduBook book = new BaiduModel.BaiduBook();
        try {
            trustEveryone();
            Document document = Jsoup.connect(title.url).timeout(10000).get();
//            logd(TAG, "document:" + document.toString());
            Element head = document.head();
//            logi(TAG, "head:" + head.toString());
            Elements metas = head.getElementsByTag("meta");
//            logi(TAG, "metas:" + metas.toString());
            for (Element meta : metas) {
//                String type = meta.attr("http-equiv");
//                String content = meta.attr("content");
//                logi(TAG, "type = " + type + ",content=" + content);
                Attributes attributes = meta.attributes();
//                logi(TAG, "meta = " + attributes.toString() + ",size = " + attributes.size());
                String property = attributes.get("property");
                if (property == null) {
                    continue;
                }
                logi("property = " + attributes.toString());
                if (property.equals("og:novel:author")) {
                    book.author = attributes.get("content");
                } else if (property.equals("author")) {
                    book.author = attributes.get("content");
                } else if (property.equals("og:novel:book_name")) {
                    book.bookName = attributes.get("content");
                } else if (property.equals("og:novel:read_url")) {
                    String read_url = attributes.get("content");
                    if (read_url != null && read_url.toLowerCase().startsWith("http")) {
                        book.readUrl = read_url;
                        book.sourceHost = AppUtils.getHostFromUrl(read_url);
                    }
                } else if (property.equals("og:novel:latest_chapter_name")) {
                    book.latestChapterName = attributes.get("content");
                } else if (property.equals("og:novel:latest_chapter_url")) {
                    book.latestChapterUrl = attributes.get("content");
                } else if (property.equals("og:image")) {
                    String imageUrl = attributes.get("content");
                    if (imageUrl != null && imageUrl.startsWith("http")) {
                        book.image = imageUrl;
                    }
                }
            }
            Element body = document.body();
            Elements header_logo = body.getElementsByClass("header_logo");
            Elements header_left = body.getElementsByClass("header-left");
//            logi(TAG, "body=" + body);
            if (header_logo != null) {
                for (Element h : header_logo) {
                    Elements a = h.getElementsByTag("a");
                    if (a != null) {
                        book.sourceName = a.text();
                    }
                }
            } else if (header_left != null) {
                for (Element h : header_left) {
                    Elements a = h.getElementsByTag("a");
                    if (a != null) {
                        book.sourceName = a.attr("title");
                    }
                }
            }
            if (TextUtils.isEmpty(book.sourceName)) {
                int index = title.title.lastIndexOf("-");
                if (index < 0) {
                    index = title.title.lastIndexOf("_");
                }
                if (index < 0) {
                    index = title.title.lastIndexOf(" ");
                }
                if (index > 0) {
                    book.sourceName = title.title.substring(index + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    private static boolean urlInvalid(String url) {
        return url.contains("xsm.chaoshen.cc") || url.contains("qidian");
    }

    private static void logd(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.d(BaiduSearch.TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.d(BaiduSearch.TAG, str);
    }

    private static void logi(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(BaiduSearch.TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(BaiduSearch.TAG, str);
    }
}
