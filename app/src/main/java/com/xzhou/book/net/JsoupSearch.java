package com.xzhou.book.net;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * File Description: 使用Jsoup解析其他搜索接口的数据
 * Author: zhouxian
 * Create Date: 19-11-23
 * Change List:
 */
public abstract class JsoupSearch {
    String TAG;

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

    protected ProgressCallback mCallback;
    protected boolean mCancel = false;
    protected int mCurSize;
    protected int mCurParseSize;
    protected List<String> mBookHosts = new ArrayList<>();

    public JsoupSearch(String name) {
        TAG = name;
    }

    public void setCancel(boolean cancel) {
        mCancel = cancel;
    }

    public void setProgressCallback(ProgressCallback callback) {
        mCallback = callback;
    }

    protected void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public abstract List<SearchModel.SearchBook> parseSearchKey(String key);

    protected List<SearchModel.SearchBook> getBookListForDocument(Document document) {
        List<SearchModel.SearchBook> bookList = null;
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
                    BaiduSearch.Title t = new Gson().fromJson(title, BaiduSearch.Title.class);
                    if (t == null || t.title.contains("网盘")) {
                        continue;
                    }
                    SearchModel.SearchBook book = parseResult(t);
                    if (book != null && book.hasValid() && !mBookHosts.contains(book.sourceHost)) {
                        if (urlInvalid(book.readUrl)) {
                            continue;
                        }
                        Log.i(TAG, "book = " + book);
                        bookList.add(book);
                        mCurSize += 1;
                        mBookHosts.add(book.sourceHost);
                        if (SearchModel.hasSupportLocalRead(book.sourceHost)) {
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

    protected SearchModel.SearchBook parseResult(Title title) {
        SearchModel.SearchBook book = new SearchModel.SearchBook();
        try {
            trustEveryone();

            Document document = Jsoup.parse(OkHttpUtils.getPcString(title.url));
            Log.d(TAG, "title= " + title.title + ",url=" + title.url);
            if (mCallback != null && !mCancel) {
                mCallback.onCurParse(mCurSize, mCurParseSize, title.title + "-" + document.baseUri());
            }
            Element head = document.head();
            Elements metas = head.getElementsByTag("meta");
//            logi("metas:" + metas.toString());
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
                } else if (property.equals("og:novel:read_url") || property.equals("og:url")) {
                    String read_url = attributes.get("content");
                    if (read_url != null && read_url.toLowerCase().startsWith("http")) {
                        if (read_url.contains("m.23us.la")) {
                            read_url = read_url.replace("m.23us.la", "www.23us.la");
                        } else if (read_url.contains("m.f96.la")) {
                            read_url = read_url.replace("m.f96.la", "www.f96.la");
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

    protected static boolean urlInvalid(String url) {
        return url.contains("xsm.chaoshen.cc") || url.contains("qidian");
    }

    protected void logd(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.d(TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.d(TAG, str);
    }

    protected void logi(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(TAG, str);
    }
}
