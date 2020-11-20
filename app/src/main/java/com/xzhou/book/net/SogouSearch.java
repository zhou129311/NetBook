package com.xzhou.book.net;

import android.text.TextUtils;

import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description: 搜狗搜索引擎
 * Author: zhouxian
 * Create Date: 19-11-23
 * Change List:
 */
public class SogouSearch extends JsoupSearch {

    public SogouSearch() {
        super("SogouSearch");
    }

//    /**
//     * @return 搜狗验证码机制是通过cookie触发，每次搜索对cookie进行设置可以避免验证码
//     */
//    private Map<String, String> getCookies() {
//        Map<String, String> cookies = new HashMap<>();
//        String url = "https://www.sogou.com/web?query=%E6%90%9C%E7%8B%97%E8%81%94%E7%9B%9F%E7%99%BB%E5%BD%95&page=1";
//        Response req = OkHttpUtils.getPcRel(url);
//        if (req != null) {
//            List<String> cookieList = req.headers().values("Set-Cookie");
//            Log.i(TAG, "cookieList = " + cookieList);
//            if (cookieList != null) {
//                for (String cookie : cookieList) {
//                    if (cookie.contains("BAIDUID=")) {
//                        cookies.put("BAIDUID", cookie.substring(9));
//                    }
//                }
//            }
//        }
//        return cookies;
//    }
//
//    public List<SearchModel.SearchBook> parseSearchKey(String key) {
//        mCurSize = 0;
//        mCurParseSize = 0;
//        mCancel = false;
//        mBookHosts.clear();
//        List<SearchModel.SearchBook> bookList = new ArrayList<>();
//        try {
//            trustEveryone();
//            String url = "https://www.sogou.com/web?query=" + key;
//
//            Document document = Jsoup.connect(url).cookies(getCookies()).userAgent(USER_AGENT).timeout(10000).get();
//            Elements page = document.getElementsByClass("p");
//            Log.i(TAG, "page: " + page.toString());
//            Elements a = page.select("a");
//            List<String> pages = new ArrayList<>();
//            if (a != null) {
//                for (Element element : a) {
//                    String link = element.attr("href");
//                    if (link != null && link.startsWith("?")) {
//                        pages.add("https://www.sogou.com/web" + link);
//                    }
//                    if (pages.size() > 8) {
//                        break;
//                    }
//                    Log.i(TAG, "page: " + link);
//                }
//            }
//            List<SearchModel.SearchBook> list1 = getBookListForDocument(document);
//            if (list1 != null) {
//                bookList.addAll(list1);
//            }
//            for (String pageUrl : pages) {
//                if (mCancel) {
//                    break;
//                }
//                Document pageDocument = Jsoup.connect(pageUrl).userAgent(USER_AGENT).timeout(10000).get();
//                List<SearchModel.SearchBook> list2 = getBookListForDocument(pageDocument);
//                if (list2 != null) {
//                    bookList.addAll(list2);
//                }
////                    if (bookList.size() > 10 && mCurParseSize > 0) {
////                        break;
////                    }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e);
//        }
//        return bookList;
//    }

    @Override
    public List<SearchModel.SearchBook> parseFirstPageHtml(String html) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        mBookHosts.clear();
        List<SearchModel.SearchBook> list = null;
        try {
            Document document = Jsoup.parse(html);
            Element body = document.body();
            Elements page = body.select("div#pagebar_container");
            Elements a = page.select("a");
            if (mPageUrlList == null) {
                mPageUrlList = new ArrayList<>();
            } else {
                mPageUrlList.clear();
            }
            if (a != null) {
                for (Element element : a) {
                    String link = element.attr("href");
                    if (link != null && link.startsWith("?")) {
                        mPageUrlList.add("https://www.sogou.com/web" + link);
                    }
                    if (mPageUrlList.size() > 8) {
                        break;
                    }
                    Log.i(TAG, "page: " + link);
                }
            }
            Log.i(TAG, "mPageUrlList: " + mPageUrlList.size());
            list = getBookListForDocument(document);
            if (mUrlCallback != null) {
                if (mPageUrlList.size() > 0 && !mCancel) {
                    String url = mPageUrlList.remove(0);
                    mUrlCallback.onNextUrl(url);
                } else {
                    mUrlCallback.onLoadEnd();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return list;
    }

    protected List<SearchModel.SearchBook> getBookListForDocument(Document document) {
        List<SearchModel.SearchBook> bookList = null;
        try {
            Elements result = document.getElementsByClass("results");
            logd("result size=" + result.size());
            Elements h3 = result.get(0).select("h3");
            bookList = new ArrayList<>();
            if (h3 != null) {
                for (Element element : h3) {
                    if (mCancel) {
                        return bookList;
                    }
                    Elements a = element.select("a");
                    String link = a.attr("href");
                    if (link != null) {
                        Title t = new Title();
                        t.title = a.select("em") != null ? a.select("em").text() : "";
                        if (link.startsWith("http")) {
                            t.url = link;
                        } else if (link.startsWith("/")) {
                            t.url = "https://www.sogou.com" + link;
                        }
                        if (t.url == null) {
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
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return bookList;
    }

    protected SearchModel.SearchBook parseResult(Title title) {
        if (mCancel) {
            return null;
        }
        SearchModel.SearchBook book = new SearchModel.SearchBook();
        try {
            trustEveryone();
            Document document = Jsoup.connect(title.url).userAgent(UA).get();
            Log.d(TAG, "title= " + title.title + ",url=" + title.url + " ,charset=" + document.charset());
            Element head = document.head();
            Elements metas = head.getElementsByTag("meta");
            if (metas == null || metas.isEmpty()) {
                metas = document.getElementsByTag("meta");
            }
            for (Element meta : metas) {
                Attributes attributes = meta.attributes();
                String refresh = attributes.get("http-equiv");
                logi("http-equiv:" + refresh);
                if ("refresh".equals(refresh)) {
                    String content = attributes.get("content");
                    logi("refresh content:" + content);
                    if (!TextUtils.isEmpty(content)) {
                        String url = content.substring(content.indexOf("URL='") + 5, content.length() - 1);
                        logi("refresh url = " + url);
                        title.url = url;
                        return parseResult(title);
                    }
                }
            }
            if (mCallback != null && !mCancel) {
                mCallback.onCurParse(mCurSize, mCurParseSize, title.title + "-" + title.url);
            }
//            logi("metas:" + metas.toString());
            for (Element meta : metas) {
                Attributes attributes = meta.attributes();
//                logi(TAG, "meta = " + attributes.toString() + ",size = " + attributes.size());
                //http-equiv="Content-Type" content="text/html; charset=gbk"
                String property = attributes.get("property");
                String content = attributes.get("content");
                logi("attributes =" + attributes.toString());
                if (property.equals("og:novel:author")) {
                    book.author = content;
                } else if (property.equals("author")) {
                    book.author = content;
                } else if (property.equals("og:novel:book_name")) {
                    book.bookName = content;
                } else if (property.equals("og:novel:read_url") || property.equals("og:url")) {
                    String read_url = content;
                    if (read_url.toLowerCase().startsWith("http")) {
                        if (read_url.contains("m.23us.la")) {
                            read_url = read_url.replace("m.23us.la", "www.23us.la");
                        } else if (read_url.contains("m.f96.la")) {
                            read_url = read_url.replace("m.f96.la", "www.f96.la");
                        }
                        book.readUrl = read_url;
                        book.sourceHost = AppUtils.getHostFromUrl(read_url);
                    }
                } else if (property.equals("og:novel:latest_chapter_name")) {
                    book.latestChapterName = content;
                } else if (property.equals("og:novel:latest_chapter_url")) {
                    book.latestChapterUrl = content;
                } else if (property.equals("og:image")) {
                    if (content.startsWith("http")) {
                        book.image = content;
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
            if (TextUtils.isEmpty(book.sourceName) && title.title != null) {
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
            if (TextUtils.isEmpty(book.sourceName) && book.sourceHost != null) {
                int i1 = book.sourceHost.indexOf(".");
                int i2 = book.sourceHost.lastIndexOf(".");
                if (i2 > i1 && i1 > 0) {
                    book.sourceName = book.sourceHost.substring(i1 + 1, i2);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResult error", e);
        }
        return book;
    }
}
