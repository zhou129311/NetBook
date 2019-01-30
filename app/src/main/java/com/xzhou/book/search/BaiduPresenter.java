package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.BaiduSearch;
import com.xzhou.book.models.BaiduEntities;
import com.xzhou.book.models.Entities;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaiduPresenter extends BasePresenter<BaiduContract.View> implements BaiduContract.Presenter {

    private ExecutorService mPool = Executors.newSingleThreadExecutor();

    BaiduPresenter(BaiduContract.View view) {
        super(view);
    }

    @Override
    public void search(final String key) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                List<BaiduEntities.BaiduBook> list = BaiduSearch.parseSearchKey(key);
                updateSearchResult(list);
            }
        });
    }

    @Override
    public void getChapterList(final String readUrl) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.Chapters> list = BaiduSearch.parseChapterListTxbc(readUrl, null);
                if (list != null && list.size() > 0) {

                }
            }
        });
    }

    private void updateSearchResult(final List<BaiduEntities.BaiduBook> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onSearchResult(list);
                }
            }
        });
    }
}
