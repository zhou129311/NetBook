package com.xzhou.book.models;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-21
 * Change List:
 */
public class HtmlParse7 extends HtmlParse {
    HtmlParse7() {
        TAG = "HtmlParse7";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();
        logi("parseChapters::readUrl=" + readUrl);
        Element body = document.body();
        if (readUrl.contains("qidian") || readUrl.contains("readnovel")) {
            Elements eList = body.select("div.catalog-content-wrap").select(".hidden").select("ul.cf").first().children();
            for (Element e : eList) {
                Elements a = e.getElementsByTag("a");
                String title = a.html();
                String link = "https:" + a.attr("href");
                list.add(new Entities.Chapters(title, link));
            }
        } else if (readUrl.contains("xxsy")) {

        }
        return list;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Entities.ChapterRead read = new Entities.ChapterRead();
        read.chapter = new Entities.Chapter();
        Element body = document.body();
        Elements content = null;
        if (chapterUrl.contains("qidian") || chapterUrl.contains("readnovel")) {
            content = body.select("div.read-content").select(".j_readContent");
        } else if (chapterUrl.contains("xxsy")) {

        }
        if (content == null || content.isEmpty()) {
            return null;
        }
        String text = formatContent(chapterUrl, content);
        text = replaceCommon(text);
        if (text.startsWith("<style>")) {
            text = text.substring(text.indexOf("</style>"));
        }
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
