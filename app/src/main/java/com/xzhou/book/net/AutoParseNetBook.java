package com.xzhou.book.net;

import com.xzhou.book.MyApp;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.models.SearchModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-5-9
 * Change List:
 */
public class AutoParseNetBook {

    private static final ExecutorService sPool = Executors.newCachedThreadPool();
    private static boolean sIsParsing;
    private static List<Integer> sParseTypeList;
    private static final List<Callback> sCallbacks = new ArrayList<>();
    private static ItemCallback sItemCallback;

    public static void stopParse() {
        sIsParsing = false;
    }

    public interface Callback {
        void onParseState(boolean state, boolean success, String message);
    }

    public interface ItemCallback {
        void onParseState(SearchModel.SearchBook book);
    }

    public static void setItemCallback(ItemCallback callback) {
        sItemCallback = callback;
    }

    public static void addCallback(Callback callback) {
        if (!sCallbacks.contains(callback)) {
            sCallbacks.add(callback);
        }
    }

    public static void removeCallback(Callback callback) {
        sCallbacks.remove(callback);
    }

    private static void notifyCallback(final boolean state, final boolean success, final String message) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                for (Callback callback : sCallbacks) {
                    callback.onParseState(state, success, message);
                }
            }
        });
    }

    public static void tryParseBook(final SearchModel.SearchBook searchBook) {
        searchBook.isParsing = true;
        if (sItemCallback != null) {
            sItemCallback.onParseState(searchBook);
        }
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                if (sParseTypeList == null) {
                    sParseTypeList = new ArrayList<>(6);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_1);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_2);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_3);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_4);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_5);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_6);
                }
                boolean success = false;
                for (int type : sParseTypeList) {
                    HtmlParse parse = HtmlParseFactory.getHtmlParse(type);
                    if (parse != null) {
                        List<Entities.Chapters> list = parse.parseChapters(searchBook.readUrl);
                        if (list != null && list.size() > 0) {
                            Entities.Chapters chapters = list.get(0);
                            Entities.ChapterRead read = parse.parseChapterRead(chapters.link);
                            if (read != null && read.chapter != null
                                    && read.chapter.body != null && read.chapter.body.length() > 200) {
                                SearchModel.HostType hostType = new SearchModel.HostType();
                                hostType.host = searchBook.sourceHost;
                                hostType.parseType = type;
                                SearchModel.saveHostType(hostType);
                                success = true;
                                break;
                            }
                        }
                    }
                }
                searchBook.parseText = success ? "解析成功" : "解析失败";
                searchBook.isParsing = false;
                if (sItemCallback != null) {
                    sItemCallback.onParseState(searchBook);
                }
            }
        });
    }

    public static void tryParseBook(final String bookName, final String readUrl, final String sourceHost) {
        sIsParsing = true;
        notifyCallback(true, false, "解析《" + bookName + "》(" + sourceHost + ")");
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                if (sParseTypeList == null) {
                    sParseTypeList = new ArrayList<>(6);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_1);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_2);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_3);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_4);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_5);
                    sParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_6);
                }
                boolean success = false;
                for (int type : sParseTypeList) {
                    if (!sIsParsing) {
                        break;
                    }
                    HtmlParse parse = HtmlParseFactory.getHtmlParse(type);
                    if (parse != null) {
                        List<Entities.Chapters> list = parse.parseChapters(readUrl);
                        if (list != null && list.size() > 0) {
                            Entities.Chapters chapters = list.get(0);
                            if (!sIsParsing) {
                                break;
                            }
                            Entities.ChapterRead read = parse.parseChapterRead(chapters.link);
                            if (read != null && read.chapter != null
                                    && read.chapter.body != null && read.chapter.body.length() > 200) {
                                success = true;
                                SearchModel.HostType hostType = new SearchModel.HostType();
                                hostType.host = sourceHost;
                                hostType.parseType = type;
                                SearchModel.saveHostType(hostType);
                                break;
                            }
                        }
                    }
                }
                String msg;
                if (!sIsParsing && !success) {
                    msg = "解析中断";
                } else if (success) {
                    msg = "解析成功";
                } else {
                    msg = "解析失败，该书籍暂不支持本地阅读";
                }
                notifyCallback(false, success, msg);
            }
        });
    }


}
