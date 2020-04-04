package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.BaiduSearch;
import com.xzhou.book.net.JsoupSearch;
import com.xzhou.book.net.SogouSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * packageName：com.xzhou.book.search
 * ProjectName：NetBook
 * Description：
 * Author：zhouxian
 * Date：2019/12/21 19:39
 */
public class NetSearchPresenter extends BasePresenter<NetSearchContract.View> implements NetSearchContract.Presenter {

    private List<Integer> mParseTypeList;
    private List<JsoupSearch> mSearchList = new ArrayList<>(2);
    private int mSearchType;
    private ExecutorService mPool = Executors.newSingleThreadExecutor();
    private boolean mIsParseing;

    NetSearchPresenter(NetSearchContract.View view) {
        super(view);
        BaiduSearch baiduSearch = new BaiduSearch();
        baiduSearch.setProgressCallback(new JsoupSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, int parseSize, String curResult) {
                updateSearchProgress(size, parseSize, curResult);
            }
        });
        baiduSearch.setUrlCallback(new JsoupSearch.UrlCallback() {
            @Override
            public void onNextUrl(String url) {
                updateLoadUrl(url);
            }

            @Override
            public void onLoadEnd() {
                updateLoadingState(false);
            }
        });
        SogouSearch sogouSearch = new SogouSearch();
        sogouSearch.setProgressCallback(new JsoupSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, int parseSize, String curResult) {
                updateSearchProgress(size, parseSize, curResult);
            }
        });
        sogouSearch.setUrlCallback(new JsoupSearch.UrlCallback() {
            @Override
            public void onNextUrl(String url) {
                updateLoadUrl(url);
            }

            @Override
            public void onLoadEnd() {
                updateLoadingState(false);
            }
        });
        mSearchList.add(SearchActivity.SEARCH_TYPE_BAIDU, baiduSearch);
        mSearchList.add(SearchActivity.SEARCH_TYPE_SOGOU, sogouSearch);
    }

    @Override
    public void startSearch(int searchType, final String html) {
        updateLoadingState(true);
        mSearchType = searchType;
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                String value = handlerHtml(html);
                List<SearchModel.SearchBook> list = mSearchList.get(mSearchType).parseFirstPageHtml(value);
                updateData(list, false);
            }
        });
    }

    @Override
    public void updateHtml(final String html) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                String value = handlerHtml(html);
                List<SearchModel.SearchBook> list = mSearchList.get(mSearchType).parsePageHtml(value);
                updateData(list, true);
            }
        });
    }

    @Override
    public void cancel() {
        updateLoadingState(false);
        for (JsoupSearch search : mSearchList) {
            search.setCancel(true);
        }
    }

    @Override
    public void tryParseBook(final SearchModel.SearchBook book) {
        mIsParseing = true;
        updateParseResult(true, false, "解析《" + book.bookName + "》(" + book.sourceHost + ")");
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                if (mParseTypeList == null) {
                    mParseTypeList = new ArrayList<>(6);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_1);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_2);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_3);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_4);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_5);
                    mParseTypeList.add(SearchModel.ParseType.PARSE_TYPE_6);
                }
                boolean success = false;
                for (int type : mParseTypeList) {
                    if (!mIsParseing) {
                        break;
                    }
                    HtmlParse parse = HtmlParseFactory.getHtmlParse(type);
                    if (parse != null) {
                        List<Entities.Chapters> list = parse.parseChapters(book.readUrl);
                        if (list != null && list.size() > 0) {
                            Entities.Chapters chapters = list.get(0);
                            if (!mIsParseing) {
                                break;
                            }
                            Entities.ChapterRead read = parse.parseChapterRead(chapters.link);
                            if (read != null && read.chapter != null
                                    && read.chapter.body != null && read.chapter.body.length() > 200) {
                                success = true;
                                SearchModel.HostType hostType = new SearchModel.HostType();
                                hostType.host = book.sourceHost;
                                hostType.parseType = type;
                                SearchModel.saveHostType(hostType);
                                break;
                            }
                        }
                    }
                }
                String msg;
                if (!mIsParseing && !success) {
                    msg = "解析中断";
                } else if (success) {
                    msg = "解析成功";
                } else {
                    msg = "解析失败，该书籍暂不支持本地阅读";
                }
                updateParseResult(false, success, msg);
            }
        });
    }

    @Override
    public void stopParse() {
        mIsParseing = false;
    }

    @Override
    public void destroy() {
        super.destroy();
        for (JsoupSearch search : mSearchList) {
            search.setProgressCallback(null);
            search.setUrlCallback(null);
        }
    }

    private String handlerHtml(String html) {
        String value = html;
        value = value.replace("\\u003C", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");
        value = value.replace("&amp;", "&");
        value = value.replace("\\\"", "\"");
//        value = value.replace("\\n", "\n");
//        value = value.replace("\\t", "    ");
        value = "<html>" + value + "</html>";
        return value;
    }

    private void updateParseResult(final boolean state, final boolean success, final String message) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onParseState(state, success, message);
                }
            }
        });
    }

    private void updateLoadUrl(final String url) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onUrlLoad(url);
                }
            }
        });
    }

    private void updateData(final List<SearchModel.SearchBook> list, final boolean add) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    if (add) {
                        mView.onDataAdd(list);
                    } else {
                        mView.onDataChange(list);
                    }
                }
            }
        });
    }

    private void updateLoadingState(final boolean isLoading) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoadingState(isLoading);
                }
            }
        });
    }

    private void updateSearchProgress(final int bookSize, final int parseSize, final String cur) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onSearchProgress(bookSize, parseSize, cur);
                }
            }
        });
    }
}
