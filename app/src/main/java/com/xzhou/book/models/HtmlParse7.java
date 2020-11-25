package com.xzhou.book.models;

import com.xzhou.book.MyApp;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stud.task.TTFParser;
import stud.task.encoding.EncodingFormat4;
import stud.task.table.required.CMap;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-21
 * Change List:
 */
public class HtmlParse7 extends HtmlParse {
    HtmlParse7() {
        TAG = "HtmlParse7";
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Document document) {
        List<Entities.Chapters> list = new ArrayList<>();
        Log.i(TAG, "parseChapters::readUrl=" + readUrl);
        Element body = document.body();
        Elements eList;
        if (readUrl.contains("qidian") || readUrl.contains("readnovel")) {
            eList = body.select("div.catalog-content-wrap").select(".hidden").select("ul.cf").first().children();
            for (Element e : eList) {
                Elements a = e.getElementsByTag("a");
                String title = a.html();
                String link = "https:" + a.attr("href");
                list.add(new Entities.Chapters(title, link));
            }
        } else if (readUrl.contains("xs8.cn") || readUrl.contains("hongxiu")) {
            eList = body.select("div.volume-wrap").select("ul.cf");
            for (Element e : eList) {
                for (Element c : e.children()) {
                    Elements a = c.getElementsByTag("a");
                    String title = a.html();
                    String link = "https:" + a.attr("href");
                    list.add(new Entities.Chapters(title, link));
                }
            }
        }
        return list;
    }

    @Override
    public List<Entities.Chapters> parseChapters(String readUrl, Connection.Response response) {
        Document document;
        String bookId = readUrl.substring(readUrl.lastIndexOf("/") + 1, readUrl.lastIndexOf("."));
        try {
            List<Entities.Chapters> list = new ArrayList<>();
            Map<String, String> cookies = response.cookies();
            document = response.parse();
            Elements authE = document.select("dl.bookprofile input");
            String authorinfot = authE.attr("value");
            Log.i(TAG, "authorinfot = " + authorinfot);
            String chapUrl = "https://www.xxsy.net/partview/GetChapterList?bookid=" + bookId +
                    "&special=0&maxFreeChapterId=0&isMonthly=0&authorinfot=" + authorinfot;
            document = Jsoup.connect(chapUrl).userAgent(USER_AGENT).cookies(cookies)
                    .header("Referer", readUrl)
                    .timeout(15000).get();
            Elements eList = document.select("ul.catalog-list").select(".cl");
//            logi(document.html());
            for (Element e : eList) {
//                Log.i(TAG, "e : " + e.html());
                for (Element c : e.children()) {
                    Elements a = c.getElementsByTag("a");
                    String title = a.html();
                    String link = "https://www.xxsy.net" + a.attr("href");
                    Log.i(TAG, "link : " + link);
                    list.add(new Entities.Chapters(title, link));
                }
            }
//            logi("parseChapters::document=" + document.html());
            return list;
        } catch (IOException e) {
            Log.e(TAG, e);
        }
        return null;
    }

    @Override
    public Entities.ChapterRead parseChapterRead(String chapterUrl, Document document) {
        Log.i(TAG, "parseChapterRead::chapterUrl=" + chapterUrl);
//        OkHttpUtils.logi(document.outerHtml());
        Entities.ChapterRead read = new Entities.ChapterRead();
        read.chapter = new Entities.Chapter();
        Element body = document.body();
        Elements content = body.select("div.read-content").select(".j_readContent");
        if (content.isEmpty() && chapterUrl.contains("xxsy.net")) {
            read.chapter.body = parseXXSY(body);
            return read;
        }
        if (content.isEmpty()) {
            return null;
        }
        String text = formatContent(chapterUrl, content);
        text = replaceCommon(text);
        text = replaceEncodeWord(text);
        read.chapter.body = text;
//        logi("end ,text=" + text);
        return read;
    }

    private String parseXXSY(Element body) {
        Elements content = body.select("div.chapter-read").select("p.chapter-comment-item");
        StringBuilder sb = new StringBuilder();
        for (Element e : content) {
            sb.append(e.text().replace(" 0", "\n")
                    .replace(" 1", "\n")
                    .replace(" 2", "\n")
                    .replace(" 3", "\n")
                    .replace(" 4", "\n"));
        }
        for (int i = 0, s = sb.length(); i < s; i++) {
            int codePoint = sb.codePointAt(i);
            if (codePoint > 59000 && codePoint < 60000) {
                String value = TTFEncodeMap.XXSY.get(codePoint);
                if (value != null) {
                    sb.replace(i, i + 1, value);
                }
            }
        }
        String text = sb.toString().replace("　", "");
//        logi("content = " + text);
        return text;
    }

    private String replaceEncodeWord(String text) {
        if (text.startsWith("<style>")) {
            String fontName = text.substring(text.indexOf("font-family:"), text.indexOf(";")).trim();
            Log.i(TAG, "fontName = " + fontName);
            String fileName = FileUtils.getCachePath(MyApp.getContext()) + File.separator + fontName + ".ttf";
            File file = new File(fileName);
            String base64ttf = null;
            if (!file.exists()) {
                int s = text.indexOf("data:font/ttf;base64,") + "data:font/ttf;base64,".length();
                int e = text.indexOf("format('ttf')");
                if (e > s) {
                    base64ttf = text.substring(s, e).replace(")", "").trim();
                }
//                logi("base64ttf=" + base64ttf);
            }
            text = text.substring(text.indexOf("</style>") + "</style>".length());
            FileOutputStream fos = null;
            RandomAccessFile raf = null;
            try {
                if (base64ttf != null) {
                    fos = new FileOutputStream(fileName);
                    fos.write(Base64.decodeBase64(base64ttf.getBytes()));
                    fos.flush();
                    fos.close();
                }
                if (file.exists()) {
                    TTFParser parser = new TTFParser();
                    parser.parse(file);
                    CMap cMap = (CMap) parser.getTable(CMap.TAG);
                    EncodingFormat4 format4 = (EncodingFormat4) cMap.getEncoding();
                    StringBuilder sb = new StringBuilder(text);
                    for (int i = 0, s = text.length(); i < s; i++) {
                        int codePoint = text.codePointAt(i);
//                    Log.i(TAG, "codePoint = " + codePoint);
                        if (codePoint > 58000 && codePoint < 58999) {
                            int index = format4.getIndexForCodePoint(codePoint);
                            String value = TTFEncodeMap.XSYDW.get(index - 2);
                            if (value != null) {
                                sb.replace(i, i + 1, value);
                            }
                        }
                    }
                    text = sb.toString();
                }
//                int index =  format4.getIndexForCodePoint()
//                OkHttpUtils.logi(sb.toString());
            } catch (Exception e) {
                Log.e(TAG, "replaceEncodeWord error", e);
            } finally {
                AppUtils.close(fos);
                AppUtils.close(raf);
            }
        }
        return text;
    }

}
