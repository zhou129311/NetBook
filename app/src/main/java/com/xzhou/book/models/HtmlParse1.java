package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParse1 extends HtmlParse {

    HtmlParse1() {
        TAG = "HtmlParse1";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);
        Element body = document.body();
        Elements eList = body.select("div#list");
        if (eList.isEmpty()) {
            eList = body.select("div.listmain");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.list");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.novel_list");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.ocon");
        }
        if (eList.isEmpty()) {
            eList = body.select("div#cl_content");
        }
        if (eList.isEmpty()) {
            eList = body.select("div#list1");
        }
        if (eList.isEmpty()) {
            eList = body.select("div#list-chapterAll");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.book_list");
        }
        Elements dl = eList.last().select("dl").first().children();
        for (Element c : dl) {
            if ("dd".equals(c.tagName())) {
                Elements u = c.getElementsByTag("a");
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else if (!link.startsWith("http")) {
                    link = preUrl + link;
                }
//                logi("title = " + title + ",link = " + link);
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                    list.add(new Entities.Chapters(title, link));
                }
            }
        }
        list = sortAndRemoveDuplicate(list, host);
        return list.size() > 0 ? list : null;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("div#content");
        if (content.isEmpty()) {
            content = body.select("div.content");
        }
        if (content.isEmpty()) {
            content = body.select("div.zhangjieTXT");
        }
        if (content.isEmpty()) {
            content = body.select("div.panel-body");
        }
        if (content.isEmpty()) {
            content = body.select("div.showtxt");
        }

        read.chapter = new Entities.Chapter();
        String text = formatContent(content);
        text = text.replace("<!--go-->", "");
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
