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

public class OtherPresenter extends BasePresenter<OtherContract.View> implements OtherContract.Presenter {

    private ExecutorService mPool = Executors.newSingleThreadExecutor();
    private List<JsoupSearch> mSearchList = new ArrayList<>();
    private String mKey;
    private boolean isStart;

    OtherPresenter(OtherContract.View view, String key) {
        super(view);
        mKey = key;
        BaiduSearch baiduSearch = new BaiduSearch();
        baiduSearch.setProgressCallback(new JsoupSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, int parseSize, String curResult) {
                updateSearchProgress(size, parseSize, curResult);
            }
        });
        SogouSearch sogouSearch = new SogouSearch();
        sogouSearch.setProgressCallback(new JsoupSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, int parseSize, String curResult) {
                updateSearchProgress(size, parseSize, curResult);
            }
        });
        mSearchList.add(baiduSearch);
        mSearchList.add(sogouSearch);
    }

    @Override
    public boolean start() {
        if (!isStart) {
            isStart = true;
            search(mKey);
            return true;
        }
        return super.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        for (JsoupSearch search : mSearchList) {
            search.setProgressCallback(null);
        }
    }

    @Override
    public void search(final String key) {
        updateLoadingState(true);
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                List<SearchModel.SearchBook> list = null;
                for (JsoupSearch search : mSearchList) {
                    list = search.parseSearchKey(key);
                    if (list != null && list.size() > 0) {
                        break;
                    }
                }
                updateSearchResult(list);
                updateLoadingState(false);
            }
        });
    }

    @Override
    public void getChapterList(final String readUrl, final String host) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                HtmlParse parse = HtmlParseFactory.getHtmlParse(host);
                if (parse != null) {
                    List<Entities.Chapters> list = parse.parseChapters(readUrl);
                    if (list != null && list.size() > 0) {
                        parse.parseChapterRead(list.get(0).link);
                    }
                }
            }
        });
    }

    @Override
    public void cancel() {
        for (JsoupSearch search : mSearchList) {
            search.setCancel(true);
        }
    }

    private void updateSearchResult(final List<SearchModel.SearchBook> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onSearchResult(list);
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
