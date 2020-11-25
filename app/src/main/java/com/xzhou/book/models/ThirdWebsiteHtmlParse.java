package com.xzhou.book.models;

import android.text.TextUtils;
import android.util.Pair;

import com.xzhou.book.net.OkHttpUtils;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-19
 * Change List:
 */
public class ThirdWebsiteHtmlParse {
    private static final String TAG = "ThirdWebsiteHtmlParse";

    public static Entities.ThirdBookData xsydw(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.readnovel.com" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }
                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.readnovel.com";
                netBook.sourceName = "小说阅读网";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();
                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
            Log.i(TAG, "xsydw  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xsydw error", e);
        }
        return data;
    }

    public static Entities.ThirdBookDetail xsydwInfo(String url, String html) {
        Entities.ThirdBookDetail data = new Entities.ThirdBookDetail();
        data.readUrl = url;
        try {
//            OkHttpUtils.logi(html);
            Document jsoup = Jsoup.parse(html);
            Elements bookinfoE = jsoup.select(".book-info");
            data.title = bookinfoE.select("h1 em").first().html();
            data.author = bookinfoE.select(".writer").first().html();
            data.image = "https:" + jsoup.select("#bookImg img").first().attr("src").replace("\r", "");
            Elements tagE = bookinfoE.select(".tag").first().children();
            StringBuilder tag = new StringBuilder();
            for (int i = 0, s = tagE.size(); i < s; i++) {
                Element e = tagE.get(i);
                if (i == s - 1) {
                    tag.append(e.html());
                } else {
                    tag.append(e.html()).append(" | ");
                }
            }
            data.tags = tag.toString();
            StringBuilder word = new StringBuilder();
            Elements wordCountE = bookinfoE.select(".total").first().children();
            for (Element e : wordCountE) {
                word.append(e.html());
            }

            data.list.add(new Pair<>("总字数", word.toString()));
            data.list.add(new Pair<>("周推荐", jsoup.select(".ticket").select(".rec-ticket").select("#recCount").html()));
            data.list.add(new Pair<>("月票", jsoup.select(".ticket").select(".month-ticket").select("#monthCount").html()));
            data.intro = bookinfoE.select(".intro").first().html().replace("<br>", "\n");
            Elements lastE = jsoup.select("div.update");
            data.lastChapter = "最新章节：" + lastE.select("a").html();
            data.lastUpdate = "最后更新：" + lastE.select("span").html();
            Log.i(TAG, "xsydwInfo  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xsydwInfo error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData xxsy(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".result-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book img").first().attr("data-src");
                String url = "https://www.xxsy.net" + element.select(".book").first().attr("href");
                Elements infoE = element.select(".info");

                String name = infoE.select("h4 a").first().html();
                Elements subtitleE = infoE.select(".subtitle");
                String desc = infoE.select(".detail").first().html();

                Element name_IconE = subtitleE.select("a").first();
                Elements icon = name_IconE.select("i");
                icon.remove();
                String author = name_IconE.html();
                subtitleE.remove(subtitleE.select("a").first());
                Elements tags = subtitleE.select("a");
                StringBuilder tag = new StringBuilder();
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html());
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                netBook.bookName = name;
                netBook.sourceHost = "www.xxsy.net";
                netBook.sourceName = "潇湘书院";
                netBook.image = "http:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }

            Elements pageE = document.select(".pages");
            Elements prerPageE = pageE.select(".page-prev");
            prerPageE.remove();
            Elements nextPageE = pageE.select(".page-next");
            nextPageE.remove();
            Elements goE = pageE.select(".go");
            goE.remove();

            String pageCount = "1";
            String pageCurrent = "1";
            if (pageE.select(".active").first() != null) {
                pageCurrent = pageE.select(".active").first().html();
            }
            if (pageE.select("a").last() != null) {
                pageCount = pageE.select("a").last().html();
            }
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = count;
            data.pageCurrent = current;
//            Log.i(TAG, "xxsy  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xxsy error", e);
        }
        return data;
    }

    public static Entities.ThirdBookDetail xxsyInfo(String url, String html) {
        Entities.ThirdBookDetail data = new Entities.ThirdBookDetail();
        data.readUrl = url;
        try {
//            OkHttpUtils.logi(html);
            Document jsoup = Jsoup.parse(html);
            Elements bookprofile = jsoup.select(".bookprofile");
            data.title = bookprofile.select(".title h1").first().html();
            data.author = bookprofile.select(".title span a").first().html();
            data.image = "https:" + bookprofile.select("img").first().attr("src");
            Elements tagE = bookprofile.select(".sub-cols span");
            StringBuilder tag = new StringBuilder();
            for (int i = 0, s = tagE.size(); i < s; i++) {
                Element e = tagE.get(i);
                if (i == s - 1) {
                } else if (i == s - 2) {
                    tag.append(e.html());
                } else {
                    tag.append(e.html()).append(" | ");
                }
            }
            data.tags = tag.toString();

            data.list.add(new Pair<>("总字数", bookprofile.select(".sub-data span em").first().html()));
            data.list.add(new Pair<>("总阅读数", bookprofile.select(".sub-data span em").get(1).html()));
            data.list.add(new Pair<>("总收藏", bookprofile.select(".sub-data span em").last().html()));

            data.intro = jsoup.select(".introcontent dd").first().html().replace("\n", "")
                    .replace("<p>", "\n").replace("</p>", "");
            Elements lastE = jsoup.select("div.sub-newest");
            data.lastChapter = "最新章节：" + lastE.select("a").last().html();
            data.lastUpdate = "最后更新：" + lastE.select("span").last().html();
            Log.i(TAG, "xxsyInfo  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "xxsyInfo error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData hxtx(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.hongxiu.com" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.hongxiu.com";
                netBook.sourceName = "红袖添香";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
//            Log.i(TAG, "hxtx  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "hxtx error", e);
        }
        return data;
    }

    public static Entities.ThirdBookDetail hxtxInfo(String url, String html) {
        Entities.ThirdBookDetail data = new Entities.ThirdBookDetail();
        data.readUrl = url;
        try {
//            OkHttpUtils.logi(html);
            Document jsoup = Jsoup.parse(html);
            Elements bookinfoE = jsoup.select(".book-info");
            data.title = bookinfoE.select("h1 em").first().html();
            data.author = bookinfoE.select(".writer").first().html();
            data.image = "https:" + jsoup.select("#bookImg img").first().attr("src").replace("\r", "");
            Elements tagE = bookinfoE.select(".tag").first().children();
            StringBuilder tag = new StringBuilder();
            for (int i = 0, s = tagE.size(); i < s; i++) {
                Element e = tagE.get(i);
                if (i == s - 1) {
                    tag.append(e.html());
                } else {
                    tag.append(e.html()).append(" | ");
                }
            }
            data.tags = tag.toString();
            StringBuilder word = new StringBuilder();
            Elements wordCountE = bookinfoE.select(".total").first().children();
            word.append(wordCountE.first().html()).append(wordCountE.get(1).html());

            data.list.add(new Pair<>("总字数", word.toString()));
            data.list.add(new Pair<>("周推荐", jsoup.select(".ticket").select(".rec-ticket").select("#recCount").html()));
            data.list.add(new Pair<>("月票", jsoup.select(".ticket").select(".month-ticket").select("#monthCount").html()));

            data.intro = bookinfoE.select(".intro").first().html().replace("<br>", "\n");
            Elements lastE = jsoup.select("div.update");
            data.lastChapter = "最新章节：" + lastE.select("a").html();
            data.lastUpdate = "最后更新：" + lastE.select("span").html();
            Log.i(TAG, "hxtxInfo  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "hxtxInfo error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData yqxsb(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".right-book-list ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img a img").first().attr("src");
                String url = "https://www.xs8.cn" + element.select(".book-img a").first().attr("href");
                Elements bookInfo = element.select(".book-info");
                String name = bookInfo.select("h3 a").first().html();
                String author = bookInfo.select("h4 a").first().html();
                StringBuilder tag = new StringBuilder();
                Elements tags = bookInfo.select(".tag span");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html()).append("字");
                    } else {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                String desc = bookInfo.select(".intro").html().trim();

                netBook.bookName = name;
                netBook.sourceHost = "www.xs8.cn";
                netBook.sourceName = "言情小说吧";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = desc;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }
            String pageCount = document.select("#page-container").attr("data-total");
            String pageCurrent = document.select("#page-container").attr("data-page");
            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = (int) Math.ceil((float) count / 10f);
            data.pageCurrent = current;
//            Log.i(TAG, "yqxsb  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "yqxsb error", e);
        }
        return data;
    }

    public static Entities.ThirdBookData qdzww(String html) {
        Entities.ThirdBookData data = new Entities.ThirdBookData();
        data.list = new ArrayList<>();
        try {
//            OkHttpUtils.logi(html);
            Document document = Jsoup.parse(html);
            Elements list = document.select(".book-img-text ul li");
            for (Element element : list) {
                SearchModel.SearchBook netBook = new SearchModel.SearchBook();
                String img = element.select(".book-img-box a img").first().attr("src");
                String url = "https:" + element.select(".book-img-box a").first().attr("href");
                Elements book_mid_infoElement = element.select(".book-mid-info");
                String title = book_mid_infoElement.select("a").first().html(); // 标题
                Elements authorElement = element.select(".author");
                String author = authorElement.select(".name").first().html(); // 作者
                String intro = book_mid_infoElement.select(".intro").first().html(); //  简介

                StringBuilder tag = new StringBuilder();
                Elements tags = authorElement.select("a");
                for (int i = 0, s = tags.size(); i < s; i++) {
                    Element tagE = tags.get(i);
                    if (i == s - 1) {
                        tag.append(tagE.html());
                    } else if (i != 0) {
                        tag.append(tagE.html()).append(" | ");
                    }
                }

                netBook.bookName = title;
                netBook.sourceHost = "www.qidian.com";
                netBook.sourceName = "起点中文网";
                netBook.image = "https:" + img;
                netBook.readUrl = url;
                netBook.author = author;
                netBook.desc = intro;
                netBook.tag = tag.toString();

                if (netBook.hasValid()) {
                    data.list.add(netBook);
                }
            }

            Elements pageElement = document.select("#page-container");
            String pageCurrent = pageElement.attr("data-page"); // 当前页
            String pageCount = pageElement.attr("data-pagemax"); // 最大页

            int count = 1;
            try {
                count = Integer.parseInt(pageCount);
            } catch (NumberFormatException ignored) {
            }
            int current = 1;
            try {
                current = Integer.parseInt(pageCurrent);
            } catch (NumberFormatException ignored) {
            }
            data.pageCount = count;
            data.pageCurrent = current;
//            Log.i(TAG, "qdzww  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "qdzww error", e);
        }
        return data;
    }

    public static Entities.ThirdBookDetail qdzwwInfo(String url, String html) {
        Entities.ThirdBookDetail data = new Entities.ThirdBookDetail();
        data.readUrl = url;
        try {
//            OkHttpUtils.logi(html);
            Document jsoup = Jsoup.parse(html);
            Elements bookinfoE = jsoup.select(".book-information");
            data.title = bookinfoE.select(".book-info").select("h1 em").first().html();
            data.author = bookinfoE.select(".book-info").select("h1 a").first().html();
            data.image = "https:" + bookinfoE.select(".book-img img").first().attr("src").replace("\r", "");
            Elements tagE = bookinfoE.select(".book-info").select(".tag").first().children();
            StringBuilder tag = new StringBuilder();
            for (int i = 0, s = tagE.size(); i < s; i++) {
                Element e = tagE.get(i);
                if (i == s - 1) {
                    tag.append(e.html());
                } else {
                    tag.append(e.html()).append(" | ");
                }
            }
            data.tags = tag.toString();

//            String style = bookinfoE.select("style").first().html();
//            if (style != null && style.contains("font-family:")) {
//                int is = style.indexOf("font-family:") + "font-family:".length();
//
//                String fileName = style.substring(is, style.indexOf(";")).trim();
//                Matcher matcher = PATTERN_TTF_URL.matcher(style);
//                if (matcher.find()) {
//                    String ttfUrl = matcher.group();
//                    ttfUrl = ttfUrl.substring(ttfUrl.lastIndexOf("https"));
//                    Log.i(TAG, "fileName = " + fileName + " ,ttfUrl = " + ttfUrl);
//                    String ttfPath = FileUtils.getCachePath(MyApp.getContext()) + File.separator + fileName + ".ttf";
//                    File file = new File(ttfPath);
//                    FileOutputStream fos = null;
//                    if (!file.exists()) {
//                        try {
//                            Response response = OkHttpUtils.getPcRelResponse(ttfUrl);
//                            fos = new FileOutputStream(file);
//                            fos.write(response.body().bytes());
//                            fos.flush();
//                            fos.close();
//                        } catch (Exception e) {
//                            Log.e(TAG, "qdzwwInfo ttf error", e);
//                        } finally {
//                            AppUtils.close(fos);
//                        }
//                    }
//                    TTFParser parser = new TTFParser();
//                    parser.parse(file);
//                    CMap cMap = (CMap) parser.getTable(CMap.TAG);
//                    EncodingFormat4 format4 = (EncodingFormat4) cMap.getEncoding();
//                    Elements pairE = bookinfoE.select("." + fileName);
//                    Elements citeE = bookinfoE.select("cite");
//                    for (int i = 0; i < pairE.size(); i++) {
//                        if (i == 0 || i == 1) {
//                            StringBuilder sb = new StringBuilder(pairE.get(i).html());
//                            for (int j = 0, s = sb.length(); j < s; j++) {
//                                int codePoint = sb.codePointAt(j);
//                                Log.i(TAG, "codePoint = " + codePoint);
//                                int index = format4.getIndexForCodePoint(codePoint);
//                                String value = TTFEncodeMap.QDZWW.get(index - 2);
//                                if (value != null) {
//                                    sb.replace(j, j + 1, value);
//                                }
//                            }
//                            if (citeE.get(i).html().contains("万")) {
//                                sb.append("万");
//                            }
//                            if (i == 0) {
//                                data.list.add(new Pair<>("总字数", sb.toString()));
//                            } else if (i == 1) {
//                                data.list.add(new Pair<>("总推荐", sb.toString()));
//                            }
//                        }
//                    }
//                }
//            }

            String count = jsoup.select("div.nav-wrap").select(".fl").select("span#J-catalogCount").html()
                    .replace("(", "").replace(")", "");
            if (TextUtils.isEmpty(count)) {
                count = "--";
            }
            data.list.add(new Pair<>("总章节数", count));
            data.list.add(new Pair<>("周推荐", jsoup.select(".ticket").select(".rec-ticket").select("#recCount").html()));
            data.list.add(new Pair<>("月票", jsoup.select(".ticket").select(".month-ticket").select("#monthCount").html()));

            data.intro = jsoup.select(".book-intro p").html().replace("<br>", "\n");
            Elements lastE = jsoup.select("li.update").select(".detail");
            data.lastChapter = "最新章节：" + lastE.select("p.cf a").html();
            data.lastUpdate = "最后更新：" + lastE.select("p.cf em").html();
            Log.i(TAG, "qdzwwInfo  = " + data);
        } catch (Exception e) {
            Log.e(TAG, "qdzwwInfo error", e);
        }
        return data;
    }

    private static final Pattern PATTERN_TTF_URL = Pattern.compile("https(.*?)\\.ttf");
}
