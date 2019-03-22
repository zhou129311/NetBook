package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParse2 extends HtmlParse {
    HtmlParse2() {
        TAG = "HtmlParse2";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("div#list-chapterAll");
        Elements dd = null;
        if (eList.isEmpty()) {
            eList = body.select("dl.chapterlist").select(".cate");
        }
        if (eList.isEmpty()) {
            eList = body.select("dl.chapterlist");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.fulllistall");
        }
        if (eList.isEmpty()) {
            eList = body.select("dl.zjlist");
        }
        if (eList.isEmpty()) {
            eList = body.select("dl.panel-body").select(".panel-chapterlist");
            if (!eList.isEmpty() && eList.size() == 2) {
                dd = eList.get(0).select("dd");
            }
        }
        if (dd == null || dd.isEmpty()) {
            dd = eList.select("dd");
        }
        if (dd.isEmpty()) {
            return null;
        }
        for (Element c : dd) {
            if ("dd".equals(c.tagName())) {
                Elements dd_a = c.getElementsByTag("a");
                String title = dd_a.text();
                String link = dd_a.attr("href");
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
        list = sortAndRemoveDuplicate(list, host);
        return list.size() > 0 ? list : null;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        read.chapter = new Entities.Chapter();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("div.readcontent");
        if (content.isEmpty()) {
            content = body.select("div.chaptercontent").select(".dus52");
        }
        if (content.isEmpty()) {
            content = body.select("div#BookText");
        }
        if (content.isEmpty()) {
            content = body.select("div.panel-body");
        }
        if (content.isEmpty()) {
            content = body.select("div.page-content");
        }
        String text = formatContent(content);
        if (chapterUrl.contains("milepub")) {
            text = text.replace("<div id=\"BookText\">", "");
            text = text.replace("&lt;div id=\"pagecontent\"&gt;", "");
            text = text.replace("&lt;script language=\"javascript\"&gt;outputcontent('/75','75976','27614716','0');&lt;/script&gt;", "");
        }
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
