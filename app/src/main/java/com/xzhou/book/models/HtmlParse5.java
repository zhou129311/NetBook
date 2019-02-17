package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;

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
        Elements cd = eList.first().children();
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
                } else {
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
        Elements content = body.select("p.Text");
        read.chapter = new Entities.Chapter();
        String text = formatContent(content);
        text = replaceCommon(text);
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
