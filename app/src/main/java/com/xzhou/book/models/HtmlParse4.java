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
 * Date：2019/1/31 22:22
 */
public class HtmlParse4 extends HtmlParse {
    HtmlParse4() {
        TAG = "HtmlParse4";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("div.article_texttitleb");
        Elements li = eList.select("li");
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
        read.chapter = new Entities.Chapter();
        String text = content.toString().replace("<div id=\"book_text\">", "");
        text = text.replace("<div id=\"ali\">", "");
        text = text.replace("</div> </div>", "");
        logi("start ,text=" + text);
        text = text.replace("\n", "");
        text = text.replace("<br>", "\n");
        text = text.replace("&nbsp;", "");
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
