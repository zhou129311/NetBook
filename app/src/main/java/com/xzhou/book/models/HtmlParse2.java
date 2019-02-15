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
        if (eList.isEmpty()) {
            eList = body.select("dl.chapterlist").select(".cate");
        }
        Elements chapterList = eList.last().children();
        int dtSize = 0;
        for (Element c : chapterList) {
            if ("dt".equals(c.tagName())) {
                dtSize++;
            }
            if (dtSize == 1 && "dd".equals(c.tagName())) {
                Elements dd_a = c.getElementsByTag("a");
                String title = dd_a.text();
                String link = dd_a.attr("href");
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
        read.chapter = new Entities.Chapter();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("div.readcontent");
        if (content.isEmpty()) {
            content = body.select("div#BookText");
        }
        content.select("div.kongwen").remove();
        content.select("div.readmiddle").remove();
        content.select("p").remove();
        String text = subFirstDiv(content);
        text = text.replace("</div>", "");
        if (chapterUrl.contains("milepub")) {
            text = text.replace("<div id=\"BookText\">", "");
            text = text.replace("&lt;div id=\"pagecontent\"&gt;", "");
            text = text.replace("&lt;script language=\"javascript\"&gt;outputcontent('/75','75976','27614716','0');&lt;/script&gt;", "");
        }
        logi("start ,text=" + text);
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
