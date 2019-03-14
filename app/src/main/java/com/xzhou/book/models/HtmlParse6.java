package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParse6 extends HtmlParse {
    HtmlParse6() {
        TAG = "HtmlParse6";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("div.dccss");
        Elements a = null;
        if (!eList.isEmpty()) {
            a = eList.select("a");
        }
        if (a == null) {
            return null;
        }
        for (Element element : a) {
            String title = element.text();
            String link = element.attr("href");
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
        return list;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("DIV#content");
        read.chapter = new Entities.Chapter();
        String text = formatContent(chapterUrl, content);
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
