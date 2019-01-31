package com.xzhou.book.models;

import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParse1 extends HtmlParse {

    HtmlParse1(){
        TAG = "HtmlParse1";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();
        logi("parseChapters::readUrl=" + readUrl);
        Element body = document.body();
        Element eList = body.getElementById("list");
        Elements dl = eList.getElementsByTag("dl");
        Elements cd = dl.first().children();
        int i = readUrl.lastIndexOf("/");
        String ru;
        if (i + 1 == readUrl.length()) {
            ru = readUrl.substring(0, i);
        } else {
            ru = readUrl;
        }
        String preUrl = ru.substring(0, ru.lastIndexOf("/"));
        int dtSize = 0;
        for (Element c : cd) {
            if ("dt".equals(c.tagName())) {
                dtSize++;
            }
            if (dtSize > 1 && "dd".equals(c.tagName())) {
                Elements u = c.getElementsByTag("a");
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else {
                    link = preUrl + link;
                }
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
        Elements content = body.select("div#content");
        if (content != null) {
            read.chapter = new Entities.Chapter();
            String text = content.toString().replace("<div id=\"content\">", "");
            text = text.replace("</div>", "");
            logi("start ,text=" + text);
            text = text.replace("\n", "");
            text = text.replace("<br>", "\n");
            text = text.replace("&nbsp;", "");
            read.chapter.body = text;
            logi("end ,text=" + text);
        }
        return read;
    }
}
