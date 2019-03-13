package com.xzhou.book.net;

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

    public interface ProgressCallback {
        void onCurParse(int size, int parseSize, String curResult);
    }

    private ProgressCallback mCallback;
    private boolean mCancel = false;
    private int mCurSize;
    private int mCurParseSize;

    public void setCancel(boolean cancel) {
        mCancel = cancel;
    }

    public void setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
    }

    private void trustEveryone() {
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

    public List<BaiduModel.BaiduBook> parseSearchKey(String key) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        List<BaiduModel.BaiduBook> bookList = new ArrayList<>();
        try {
            trustEveryone();
            key = URLEncoder.encode(key, "gb2312");
            String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
            Document document = Jsoup.connect(url).timeout(10000).get();
            Element body = document.body();
            Elements page = body.select("div#page");
            Elements a = page.select("a");
            List<String> pages = new ArrayList<>();
            if (a != null) {
                for (Element element : a) {
                    String link = element.attr("href");
                    if (link != null && link.startsWith("/s")) {
                        pages.add("http://www.baidu.com" + link);
                    }
                    if (pages.size() > 8) {
                        break;
                    }
                }
            }
            List<BaiduModel.BaiduBook> list1 = getBookListForDocument(document);
            if (list1 != null) {
                bookList.addAll(list1);
            }
            if (bookList.size() <= 4) {
                for (String pageUrl : pages) {
                    if (mCancel) {
                        break;
                    }
                    Document pageDocument = Jsoup.connect(pageUrl).timeout(10000).get();
                    List<BaiduModel.BaiduBook> list2 = getBookListForDocument(pageDocument);
                    if (list2 != null) {
                        bookList.addAll(list2);
                    }
//                    if (bookList.size() > 10 && mCurParseSize > 0) {
//                        break;
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e);
        }
        return bookList;
    }

    private List<BaiduModel.BaiduBook> getBookListForDocument(Document document) {
        List<BaiduModel.BaiduBook> bookList = null;
        try {
            Elements result = document.getElementsByClass("result c-container ");
            logd("title=" + document.title() + ",result size=" + result.size());
            bookList = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                if (mCancel) {
                    break;
                }
                Element e = result.get(i);
                logi("result id=" + e.id());
                Elements f13 = e.getElementsByClass("f13");
                for (int j = 0; j < f13.size(); j++) {
                    if (mCancel) {
                        break;
                    }
                    Element child = f13.get(j);
                    String title = child.getElementsByClass("c-tools").attr("data-tools");
                    Title t = new Gson().fromJson(title, Title.class);
                    if (t == null || t.title.contains("网盘")) {
                        continue;
                    }
                    BaiduModel.BaiduBook book = parseResult(t);
                    if (book != null && book.hasValid()) {
                        if (urlInvalid(book.readUrl)) {
                            continue;
                        }
                        Log.i(TAG, "book = " + book);
                        bookList.add(book);
                        mCurSize += 1;
                        if (BaiduModel.hasSupportLocalRead(book.sourceHost)) {
                            mCurParseSize += 1;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return bookList;
    }

    private BaiduModel.BaiduBook parseResult(Title title) {
        BaiduModel.BaiduBook book = new BaiduModel.BaiduBook();
        try {
            trustEveryone();
            Document document = Jsoup.connect(title.url).timeout(10000).get();
            Log.i(TAG, "title= " + title.title + ",baseUri=" + document.baseUri());
            if (mCallback != null && !mCancel) {
                mCallback.onCurParse(mCurSize, mCurParseSize, title.title + "-" + document.baseUri());
            }
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
                        if (read_url.contains("m.23us.la")) {
                            read_url = read_url.replace("m.23us.la", "www.23us.la");
                        }
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
                }/* else if (property.equals("mobile-agent")) {
//                    book.readUrl = read_url;
                }*/
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
                if (index < 0) {
                    index = title.title.lastIndexOf(",");
                }
                if (index > 0) {
                    book.sourceName = title.title.substring(index + 1);
                }
            }
            if (TextUtils.isEmpty(book.sourceName)) {
                int i1 = book.sourceHost.indexOf(".");
                int i2 = book.sourceHost.lastIndexOf(".");
                if (i2 > i1 && i1 > 0) {
                    book.sourceName = book.sourceHost.substring(i1 + 1, i2);
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
