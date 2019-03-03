package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParse3 extends HtmlParse {

    HtmlParse3() {
        TAG = "HtmlParse3";
    }

//    public List<Entities.Chapters> parseChapters(String readUrl) {
//        try {
//            trustEveryone();
//            readUrl += "all.html";
//            Document document = Jsoup.connect(readUrl).timeout(10000).get();
//            return parseChapters(readUrl, document);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();
        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);
        Element body = document.body();
        Elements eList = body.select("div.readerListShow");
        Elements cd = eList.first().children();
        for (Element c : cd) {
            if ("p".equals(c.tagName())) {
                if ("#bottom".equals(c.attr("href"))) {
                    continue;
                }
                Elements u = c.getElementsByTag("a");
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else if (!link.startsWith("http")) {
                    link = preUrl + link;
                }
                logi("title = " + title + ", link=" + link);
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                    list.add(new Entities.Chapters(title, link));
                }
            }
        }
        list = sortAndRemoveDuplicate(list);
        return list;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("div#content");
        read.chapter = new Entities.Chapter();
        String text = formatContent(content);
        text = text.replace("</fon>", "");
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
