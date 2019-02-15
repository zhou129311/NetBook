package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName：com.xzhou.book.models
 * ProjectName：NetBook
 * Description：
 * Author：zhouxian
 * Date：2019/1/31 22:22
 */
public class HtmlParse4 extends HtmlParse {
    HtmlParse4() {
        TAG = "HtmlParse4";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(readUrl).timeout(10000).get();
            if (readUrl.contains("kenshu.cc")) {
                Element body = document.body();
                Elements all = body.select("div.bookbtn-txt");
                Elements a = all.select("a.catalogbtn");
                if (!a.isEmpty()) {
                    String host = AppUtils.getHostFromUrl(readUrl);
                    String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
                    readUrl = preUrl + a.attr("href");
                    document = Jsoup.connect(readUrl).timeout(10000).get();
                }
            }
            return parseChapters(readUrl, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("div.article_texttitleb");
        Elements li = null;
        if (eList.isEmpty()) {
            eList = body.select("ul.dirlist").select(".three").select(".clearfix");
            if (eList.isEmpty()) {
                eList = body.select("ul.clearfix").select(".chapter-list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.book_con_list");
            }
            if (!eList.isEmpty()) {
                li = eList.last().select("li");
            }
        } else {
            li = eList.select("li");
        }
        if (li == null) {
            return null;
        }
        for (Element c : li) {
            if ("li".equals(c.tagName())) {
                Elements u = c.getElementsByTag("a");
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else {
                    link = preUrl + link;
                }
                logi("title = " + title + ", link=" + link);
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                    list.add(new Entities.Chapters(title, link));
                }
            }
        }
        return list.size() == 0 ? null : list;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("div#book_text");
        if (content.isEmpty()) {
            content = body.select("div#chaptercontent");
        }
        if (content.isEmpty()) {
            content = body.select("div.article-con");
        }
        if (content.isEmpty()) {
            content = body.select("div.book_content");
        }
        content.select("div.con_l").remove();
        content.select("div#stsm").remove();
        read.chapter = new Entities.Chapter();
        String text = subFirstDiv(content);
        text = text.replace("</div>", "");
        logi("start ,text=" + text);
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
