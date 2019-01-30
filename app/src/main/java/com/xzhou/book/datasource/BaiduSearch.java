package com.xzhou.book.datasource;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xzhou.book.models.BaiduEntities;
import com.xzhou.book.models.Entities;
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
            logd("title=" + document.title() + ",result size=" + result.size());
            bookList = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                Element e = result.get(i);
                logi("result id=" + e.id());
//                logd(TAG, "result e=" + e.html());
                Elements f13 = e.getElementsByClass("f13");
//                logd(TAG, "result f13=" + f13);
                for (int j = 0; j < f13.size(); j++) {
                    Element child = f13.get(j);
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Title t = new Gson().fromJson(title, Title.class);
                    logd("title=" + t.toString());
                    if (t.title.contains("网盘")) {
                        continue;
                    }
                    BaiduEntities.BaiduBook book = parseResult(t);
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

    private static BaiduEntities.BaiduBook parseResult(Title title) {
        BaiduEntities.BaiduBook book = new BaiduEntities.BaiduBook();
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
                    book.image = attributes.get("content");
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
            Integer type = BaiduEntities.BOOK_HOSTS.get(book.sourceHost);
            if (book.hasValid() && type != null && type == BaiduEntities.PARSE_TXBC) {
                book.chaptersList = parseChapterListTxbc(book.readUrl, document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    public static List<Entities.Chapters> parseChapterListTxbc(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();
        try {
            logi("parseChapterListTxbc::readUrl=" + readUrl);
            trustEveryone();
            if (document == null) {
                document = Jsoup.connect(readUrl).timeout(5000).get();
            }
            Element body = document.body();
            Element eList = body.getElementById("list");
            Elements dl = eList.getElementsByTag("dl");
            Elements cd = dl.first().children();
            int i = readUrl.lastIndexOf("/");
            String ru;
            if (i + 1 == readUrl.length()) {
                ru = readUrl.substring(0, i);
            } else {
                ru = readUrl;
            }
            String preUrl = ru.substring(0, ru.lastIndexOf("/"));
            int dtSize = 0;
            for (Element c : cd) {
                if ("dt".equals(c.tagName())) {
                    dtSize++;
                }
                if (dtSize > 1 && "dd".equals(c.tagName())) {
                    Elements u = c.getElementsByTag("a");
                    String title = u.text();
                    String link = u.attr("href");
                    if (!link.contains("/")) {
                        link = readUrl + link;
                    } else {
                        link = preUrl + link;
                    }
//                    logi("title = " + title + ",link=" + link);
                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                        list.add(new Entities.Chapters(title, link));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.size() == 0 ? null : list;
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
