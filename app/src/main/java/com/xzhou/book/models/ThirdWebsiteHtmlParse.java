package com.xzhou.book.models;

import com.xzhou.book.net.OkHttpUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-19
 * Change List:
 */
public class ThirdWebsiteHtmlParse {
    private static final String TAG = "ThirdWebsiteHtmlParse";

    public static Entities.ThirdBookData xsydw(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.readnovel.com" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }
                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.readnovel.com";
                netBook.sourceName = "小说阅读网";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();
                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
//            Log.i(TAG, "xsydw  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xsydw error", e);
        }
        return data;
    }

    public static Entities.BookDetail xsydwInfo(String html) {
        Entities.BookDetail data = new Entities.BookDetail();
        try {
            OkHttpUtils.logi(html);
            Document jsoup = Jsoup.parse(html);
            Elements bookinfoE = jsoup.select(".book-info");
            data.title = bookinfoE.select("h1 em").first().html();
            data.author = bookinfoE.select(".writer").first().html();
            data.cover = "https:" + jsoup.select("#bookImg img").first().attr("src").replace("\r", "");
            Elements tagE = bookinfoE.select(".tag").first().children();
            data.tags = new ArrayList<>();
            for (Element e : tagE) {
                data.tags.add(e.html());
            }
            Elements wordCountE = bookinfoE.select(".total").first().children();
            for (Element e : wordCountE) {
                Log.i(TAG, "wordCountE  = " + e.html());
            }

            data.longIntro = bookinfoE.select(".intro").first().html();
            data.chaptersCount = Integer.parseInt(jsoup.select("#J-catalogCount").html());

            Log.i(TAG, "xsydwInfo  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xsydwInfo error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData xxsy(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".result-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book img").first().attr("data-src");
                String url = "https://www.xxsy.net" + element.select(".book").first().attr("href");
                Elements infoE = element.select(".info");

                String name = infoE.select("h4 a").first().html();
                Elements subtitleE = infoE.select(".subtitle");
                String desc = infoE.select(".detail").first().html();

                Element name_IconE = subtitleE.select("a").first();
                Elements icon = name_IconE.select("i");
                icon.remove();
                String author = name_IconE.html();
                subtitleE.remove(subtitleE.select("a").first());
                Elements tags = subtitleE.select("a");
                StringBuilder tag = new StringBuilder();
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html());
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                netBook.bookName = name;
                netBook.sourceHost = "www.xxsy.net";
                netBook.sourceName = "潇湘书院";
                netBook.image = "http:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }

            Elements pageE = document.select(".pages");
            Elements prerPageE = pageE.select(".page-prev");
            prerPageE.remove();
            Elements nextPageE = pageE.select(".page-next");
            nextPageE.remove();
            Elements goE = pageE.select(".go");
            goE.remove();

            String pageCount = "1";
            String pageCurrent = "1";
            if (pageE.select(".active").first() != null) {
                pageCurrent = pageE.select(".active").first().html();
            }
            if (pageE.select("a").last() != null) {
                pageCount = pageE.select("a").last().html();
            }
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = count;
            data.pageCurrent = current;
//            Log.i(TAG, "xxsy  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xxsy error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData hxtx(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.hongxiu.com" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.hongxiu.com";
                netBook.sourceName = "红袖添香";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
//            Log.i(TAG, "hxtx  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "hxtx error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData yqxsb(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.xs8.cn" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.xs8.cn";
                netBook.sourceName = "言情小说吧";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
//            Log.i(TAG, "yqxsb  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "yqxsb error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData qdzww(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".book-img-text ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img-box a img").first().attr("src");
                String url = "https:" + element.select(".book-img-box a").first().attr("href");
                Elements book_mid_infoElement = element.select(".book-mid-info");
                String title = book_mid_infoElement.select("a").first().html(); // 标题
                Elements authorElement = element.select(".author");
                String author = authorElement.select(".name").first().html(); // 作者
                String intro = book_mid_infoElement.select(".intro").first().html(); //  简介

                StringBuilder tag = new StringBuilder();
                Elements tags = authorElement.select("a");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html());
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                netBook.bookName = title;
                netBook.sourceHost = "www.qidian.com";
                netBook.sourceName = "起点中文网";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = intro;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }

            Elements pageElement = document.select("#page-container");
            String pageCurrent = pageElement.attr("data-page"); // 当前页
            String pageCount = pageElement.attr("data-pagemax"); // 最大页

            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = count;
            data.pageCurrent = current;

            Log.i(TAG, "qdzww  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "qdzww error", e);
        }
        return data;
    }
}
