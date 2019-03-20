package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * packageName：com.xzhou.book.models
 * ProjectName：NetBook
 * Description：
 * Author：zhouxian
 * Date：2019/1/31 22:23
 */
public class HtmlParse5 extends HtmlParse {

    HtmlParse5() {
        TAG = "HtmlParse5";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("dl.book");
        Elements cd;
        if (eList.isEmpty()) {
            eList = body.select("div.listmain");
            if (eList.isEmpty()) {
                eList = body.select("div#list");
            }
            cd = eList.select("dl").first().children();
        } else {
            cd = eList.first().children();
        }
        if (cd.isEmpty()) {
            return null;
        }
        int dtSize = 0;
        for (Element c : cd) {
            if ("dt".equals(c.tagName())) {
                dtSize++;
            }
            if (dtSize == 2 && "dd".equals(c.tagName())) {
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
        if (list.size() > 0) {
            Set<Entities.Chapters> set = new HashSet<>();
            List<Entities.Chapters> newList = new ArrayList<>();
            for (Entities.Chapters element : list) {
                if (set.add(element)) {
                    newList.add(element);
                }
            }
            return newList;
        }
//        list = sortAndRemoveDuplicate(list, host);
        return list.size() > 0 ? list : null;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Entities.ChapterRead read = new Entities.ChapterRead();
        logi("parseChapterRead::chapterUrl=" + chapterUrl);
        Element body = document.body();
        Elements content = body.select("p.Text");
        if (content.isEmpty()) {
            content = body.select("div#content");
        }
        if (content.isEmpty()) {
            content = body.select("div.content");
        }
        read.chapter = new Entities.Chapter();
        String text = formatContent(content);
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
