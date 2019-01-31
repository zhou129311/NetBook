package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.BaiduSearch;
import com.xzhou.book.models.BaiduModel;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;

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
                List<BaiduModel.BaiduBook> list = BaiduSearch.parseSearchKey(key);
                updateSearchResult(list);
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
}
