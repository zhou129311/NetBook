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

//    public List<Entities.Chapters> parseChapters(String readUrl) {
//        try {
//            trustEveryone();
//            if (readUrl.contains("www.123du.cc") && !readUrl.endsWith("list/")) {
//                readUrl += "list/";
//            }
//            Document document = Jsoup.connect(readUrl).userAgent(USER_AGENT).timeout(10000).get();
//            if (readUrl.contains("www.123du.cc")) {
//                readUrl = readUrl.replace("list/", "");
//            }
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
        Elements eList = body.select("div.dccss");
        if (eList.isEmpty()) {
            eList = body.select("Div.DivTitleLine");
            if (!eList.isEmpty()) {
                eList = eList.select("span.SpanTitle");
            }
        }
        if (eList.isEmpty()) {
            eList = body.select("div.chaper0");
        }
        if (eList.isEmpty()) {
            eList = body.select("div.pt-chapter-cont-detail").select(".full");
        }
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
//            if ("www.123du.cc".equals(host) && link.startsWith("../")) {
//                link = link.replace("../", "");
//            }
            if (!link.contains("/")) {
                link = readUrl + link;
            } else if (!link.startsWith("http")) {
                link = preUrl + link;
            }
//            logi("title = " + title + ", link=" + link);
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link)) {
                list.add(new Entities.Chapters(title, link));
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
        Elements content = body.select("DIV#content");
//        if (chapterUrl.contains("www.123du.cc")) {
//            Log.i(TAG, "body = " + body);
//            content = getContent(body);
//            Log.i(TAG, "content = " + content);
//        }
        if (content.isEmpty()) {
            content = body.select("div.txt");
        }
        if (content.isEmpty()) {
            //size16 color5 pt-read-text
            content = body.select("div.size16").select(".color5").select(".pt-read-text");
        }
        read.chapter = new Entities.Chapter();
        String text = formatContent(chapterUrl, content);
        text = replaceCommon(text);
        if (chapterUrl.contains("f96.la")) {
            text = text.replace(" ", "");
            text = text.replace("　", "");
        }
        read.chapter.body = text;
        logi("end ,text=" + text);
        return read;
    }

    private Elements getContent(Element body) {
        Elements content = body.select("Div#haihai");
        if (content.isEmpty()) {
            content = body.select("Div#shenma");
        }
        if (content.isEmpty()) {
            content = body.select("Div#DivContent");
        }
        if (content.isEmpty()) {
            content = body.select("Div#haishi");
        }
        if (content.isEmpty()) {
            content = body.select("Div#zouba");
        }
        if (content.isEmpty()) {
            content = body.select("Div#laiba");
        }
        if (content.isEmpty()) {
            content = body.select("Div#woshi");
        }
        if (content.isEmpty()) {
            content = body.select("Div#shime");
        }
        if (content.isEmpty()) {
            content = body.select("Div#jiushi");
        }
        if (content.isEmpty()) {
            content = body.select("Div#zhidaoma");
        }
        return content;
    }
}
