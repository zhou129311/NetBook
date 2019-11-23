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
 * File Description:
 * Author: zhouxian
 * Create Date: 19-11-23
 * Change List:
 */
public class SogouSearch extends JsoupSearch {

    public SogouSearch() {
        super("SogouSearch");
    }


    public List<SearchModel.SearchBook> parseSearchKey(String key) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        mBookHosts.clear();
        List<SearchModel.SearchBook> bookList = new ArrayList<>();
        try {
            trustEveryone();
            //   https://www.sogou.com?query=%E8%B6%85%E7%A5%9E%E6%9C%BA%E6%A2%B0%E5%B8%88&cid=&s_from=result_up&sut=4945&sst0=1574488956100&lkt=0%2C0%2C0&sugsuv=002FC07CD24B12C55D382CDA3F2B6593&sugtime=1574488956100&page=2&ie=utf8&p=40040100&dp=1&w=01029901&dr=1
            String url = "https://www.sogou.com/web?query=" + key;

            String html = OkHttpUtils.getPcString(url);

            Document document = Jsoup.parse(html);
//            Document document = Jsoup.connect(url).timeout(10000).get();
//            Element body = document.body();
            Elements page = document.getElementsByClass("p");
            Log.i(TAG, "page: " + page.toString());
            Elements a = page.select("a");
            List<String> pages = new ArrayList<>();
            if (a != null) {
                for (Element element : a) {
                    String link = element.attr("href");
                    if (link != null && link.startsWith("?")) {
                        pages.add("https://www.sogou.com/web" + link);
                    }
                    if (pages.size() > 8) {
                        break;
                    }
                    Log.i(TAG, "page: " + link);
                }
            }
            List<SearchModel.SearchBook> list1 = getBookListForDocument(document);
            if (list1 != null) {
                bookList.addAll(list1);
            }
            for (String pageUrl : pages) {
                if (mCancel) {
                    break;
                }
                Document pageDocument = Jsoup.parse(OkHttpUtils.getPcString(pageUrl));
                List<SearchModel.SearchBook> list2 = getBookListForDocument(pageDocument);
                if (list2 != null) {
                    bookList.addAll(list2);
                }
//                    if (bookList.size() > 10 && mCurParseSize > 0) {
//                        break;
//                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e);
        }
        return bookList;
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
            if (metas == null || metas.isEmpty()) {
                metas = document.getElementsByTag("meta");
//                logi("metas:" + metas.toString());
                for (Element meta : metas) {
                    Attributes attributes = meta.attributes();
                    String refresh = attributes.get("http-equiv");
                    if ("refresh".equals(refresh)) {
                        String content = attributes.get("content");
                        if (!TextUtils.isEmpty(content)) {
                            String url = content.substring(content.indexOf("URL='") + 5, content.length() - 1);
                            logi("refresh url = " + url);
                            title.url = url;
                            return parseResult(title);
                        }
                    }
                }
            }
//            logi("metas:" + metas.toString());
            for (Element meta : metas) {
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

}
