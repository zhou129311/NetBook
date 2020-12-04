package com.xzhou.book.models;

import android.text.TextUtils;

import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
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
    public List<Entities.Chapters> parseChapters(String readUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(readUrl).userAgent(USER_AGENT).timeout(30000).get();
            if (readUrl.contains("kenshu.cc")) {
                Element body = document.body();
                Elements all = body.select("div.bookbtn-txt");
                Elements a = all.select("a.catalogbtn");
                if (!a.isEmpty()) {
                    String host = AppUtils.getHostFromUrl(readUrl);
                    String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
                    readUrl = preUrl + a.attr("href");
                    document = Jsoup.connect(readUrl).timeout(30000).get();
                }
            }
            if (readUrl.endsWith("index.html")) {
                readUrl = readUrl.replace("index.html", "");
            }
            return parseChapters(readUrl, document);
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return null;
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();

        String host = AppUtils.getHostFromUrl(readUrl);
        String preUrl = readUrl.substring(0, readUrl.lastIndexOf(host) + host.length());
        logi("parseChapters::readUrl=" + readUrl + " ,preUrl = " + preUrl);

        Element body = document.body();
        Elements eList = body.select("div.article_texttitleb");
        if (eList.isEmpty()) {
            eList = body.select("ul.vol_list");
        }
        Elements li = null;
        if (eList.isEmpty()) {
            eList = body.select("ul.dirlist").select(".three").select(".clearfix");
            if (eList.isEmpty()) {
                eList = body.select("ul.clearfix").select(".chapter-list");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul.dirlist").select(".clearfix");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.clearfix").select(".dirconone");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul.list-group").select(".list-charts");
                if (!eList.isEmpty()) {
                    if (eList.size() == 2) {
                        li = eList.get(0).select("li");
                    } else if (eList.size() > 2) {
                        li = eList.get(1).select("li");
                    }
                }
            }
            if (eList.isEmpty()) {
                eList = body.select("div.book_con_list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.book_list");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul#chapters-list");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul#chapterlist");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.chapterlist");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.mulu");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.pc_list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.ml_list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div#readerlist");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.chapterCon");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul.mulu_list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.list");
            }
            if (eList.isEmpty()) {
                eList = body.select("div.chapter-list");
            }
            if (eList.isEmpty()) {
                eList = body.select("ul.chapters");
            }
            if (!eList.isEmpty() && li == null) {
                li = eList.last().select("li");
            }
        } else {
            li = eList.select("li");
        }
        if (li == null) {
            return null;
        }
        for (Element c : li) {
            if ("li".equals(c.tagName())) {
                Elements u = c.getElementsByTag("a");
                if (u.isEmpty()) {
                    continue;
                }
                String title = u.text();
                String link = u.attr("href");
                if (!link.contains("/")) {
                    link = readUrl + link;
                } else if (!link.startsWith("http")) {
                    link = preUrl + link;
                }
//                logi("title = " + title + ", link=" + link);
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
        Document document2 = null;
        if (chapterUrl.contains("www.xqianqian.com")) {
            chapterUrl = chapterUrl.replace(".html", "_2.html");
            try {
                document2 = Jsoup.connect(chapterUrl).userAgent(USER_AGENT).timeout(5000).get();
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
        Element body = document.body();
        Element body2 = null;
        if (document2 != null) {
            body2 = document2.body();
        }
//        Elements transcode = body.select("div.title");
//        if (!transcode.isEmpty()) {
//            Elements info = transcode.select("div.info");
//            if (!info.isEmpty()) {
//
//            }
//        }
        Elements content = body.select("div#book_text");
        Elements content2 = null;
        if (content.isEmpty()) {
            content = body.select("div.bookreadercontent");
        }
        if (content.isEmpty()) {
            content = body.select("div#chaptercontent");
        }
        if (content.isEmpty()) {
            content = body.select("div.panel-body").select(".content-body").select(".content-ext");
            if (!content.isEmpty() && body2 != null) {
                content2 = body2.select("div.panel-body").select(".content-body").select(".content-ext");
            }
        }
        if (content.isEmpty()) {
            content = body.select("div#content-txt");
        }
        if (content.isEmpty()) {
            content = body.select("div.article-con");
        }
        if (content.isEmpty()) {
            content = body.select("div.book_content");
        }
        if (content.isEmpty()) {
            content = body.select("div#txtContent");
        }
        if (content.isEmpty()) {
            content = body.select("div.yd_text2");
        }
        if (content.isEmpty()) {
            content = body.select("div#content1");
        }
        if (content.isEmpty()) {
            content = body.select("div#content");
        }
        if (content.isEmpty()) {
            content = body.select("div.content");
        }
        if (content.isEmpty()) {
            content = body.select("div.articlecontent");
        }
        if (content.isEmpty()) {
            content = body.select("div.articleCon");
        }
        if (content.isEmpty()) {
            content = body.select("div.nr_content");
        }
        if (content.isEmpty()) {
            content = body.select("div#BookText");
        }
        if (content.isEmpty()) {
            content = body.select("div#htmlContent");
            if (content.size() > 0) {
                content = content.get(0).children();
            }
        }
        if (content.isEmpty()) {
            content = body.select("div.content_txt");
        }
        if (content.isEmpty()) {
            content = body.select("div.contentbox");
            Log.i(TAG, "contentbox = " + content.toString());
        }
        if (content.isEmpty()) {
            content = body.select("div#contentbox");
        }
        logi("parseChapterRead::content=" + content.toString());
        read.chapter = new Entities.Chapter();
        String text = formatContent(chapterUrl, content);
        String text2 = formatContent(chapterUrl, content2);
        text = replaceCommon(text);
        text2 = replaceCommon(text2);
        if (chapterUrl.contains("www.35xs.com")) {
            text = text.replace("\n", "");
            text = text.replace("<p>", "");
            text = text.replace("</p>", "\n");
            text = text.replace(" ", "");
            text = text.replace("　", "");
            text = text.replace("www.35xs.com", "");
            text = text.replace("35xs", "");
            text = text.replace("闪舞小说网", "");
        }
        read.chapter.body = text + text2;
        logi("end ,text=" + text + text2);
        return read;
    }
}
