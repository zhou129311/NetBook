package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
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
//            logi("eList = " + eList);
        }
        Elements dl = eList.select("dl").first().children();
        int dtSize = 0;
        int target = 0;
        if (host.contains("tianxiabachang") || host.contains("booktxt")
                || host.contains("yangguiweihuo") || host.contains("biqugexsw")
                || host.contains("qu.la") || host.contains("uxiaoshuo")) {
            target = 2;
        } else if (host.contains("x4399")) {
            target = 1;
        }
        for (Element c : dl) {
            if ("dt".equals(c.tagName())) {
                dtSize++;
            }
            if ((dtSize == 0 || dtSize >= target) && "dd".equals(c.tagName())) {
                Elements u = c.getElementsByTag("a");
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else {
                    link = preUrl + link;
                }
                logi("title = " + title + ",link = " + link);
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                    list.add(new Entities.Chapters(title, link));
                }
            }
        }
        if (list.size() > 0) {
            try {
                Collections.sort(list, sComparator);
            } catch (Exception e) {
                Log.e(TAG, e);
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
        if (content.isEmpty()) {
            content = body.select("div.zhangjieTXT");
        }
        content.select("script").remove();
        content.select("div.bottem").remove();
        content.select("p").remove();
        content.select("b").remove();
        content.select("fon").remove();
        content.select("font").remove();

        read.chapter = new Entities.Chapter();
        String text = content.toString().replace("<div id=\"content\">", "");
        text = text.replace("<div id=\"content\" class=\"showtxt\">", "");
        text = text.replace("</div>", "");
        text = text.replace("<div class=\"zhangjieTXT\" id=\"TXT\">", "");
        text = text.replace("<!--go-->", "");
        logi("start ,text=" + text);
        text = text.replace("\n", "");
        text = text.replace("<br>", "\n");
        text = text.replace("&nbsp;", "");
        text = text.replace("　", "");
        text = text.replace(" ", "");
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }
}
