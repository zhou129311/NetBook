package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.models.BaiduModel;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
import com.xzhou.book.net.BaiduSearch;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaiduPresenter extends BasePresenter<BaiduContract.View> implements BaiduContract.Presenter {

    private ExecutorService mPool = Executors.newSingleThreadExecutor();
    private BaiduSearch mBaiduSearch;
    private String mKey;
    private boolean isStart;

    BaiduPresenter(BaiduContract.View view, String key) {
        super(view);
        mKey = key;
        mBaiduSearch = new BaiduSearch();
        mBaiduSearch.setProgressCallback(new BaiduSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, String curResult) {
                updateSearchProgress(size, curResult);
            }
        });
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
        mBaiduSearch.setProgressCallback(null);
    }

    @Override
    public void search(final String key) {
        updateLoadingState(true);
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                List<BaiduModel.BaiduBook> list = mBaiduSearch.parseSearchKey(key);
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
        mBaiduSearch.setCancel(true);
    }


    private void updateSearchResult(final List<BaiduModel.BaiduBook> list) {
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

    private void updateSearchProgress(final int bookSize, final String cur) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onSearchProgress(bookSize, cur);
                }
            }
        });
    }
}
