package com.xzhou.book.net;

import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BaiduSearch extends JsoupSearch {

    public BaiduSearch() {
        super("BaiduSearch");
    }


    public List<SearchModel.SearchBook> parseSearchKey(String key) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        mBookHosts.clear();
        List<SearchModel.SearchBook> bookList = new ArrayList<>();
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
            List<SearchModel.SearchBook> list1 = getBookListForDocument(document);
            if (list1 != null) {
                bookList.addAll(list1);
            }
            for (String pageUrl : pages) {
                if (mCancel) {
                    break;
                }
                Document pageDocument = Jsoup.connect(pageUrl).timeout(10000).get();
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
}
