package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.AutoParseNetBook;
import com.xzhou.book.net.BaiduSearch;
import com.xzhou.book.net.JsoupSearch;
import com.xzhou.book.net.SogouSearch;
import com.xzhou.book.utils.Log;

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
public class NetSearchPresenter extends BasePresenter<NetSearchContract.View> implements NetSearchContract.Presenter
        , AutoParseNetBook.ItemCallback {

    private final List<JsoupSearch> mSearchList = new ArrayList<>(2);
    private int mSearchType;
    private final ExecutorService mPool = Executors.newSingleThreadExecutor();

    NetSearchPresenter(NetSearchContract.View view) {
        super(view);
        BaiduSearch baiduSearch = new BaiduSearch();
        baiduSearch.setProgressCallback(this::updateSearchProgress);
        baiduSearch.setUrlCallback(new JsoupSearch.UrlCallback() {
            @Override
            public void onNextUrl(String url) {
                Log.i("NetSearch", "BaiduSearch onNextUrl : " + url);
                updateLoadUrl(url);
            }

            @Override
            public void onLoadEnd() {
                updateLoadingState(false);
                Log.i("NetSearch", "BaiduSearch onLoadEnd");
            }
        });
        SogouSearch sogouSearch = new SogouSearch();
        sogouSearch.setProgressCallback(this::updateSearchProgress);
        sogouSearch.setUrlCallback(new JsoupSearch.UrlCallback() {
            @Override
            public void onNextUrl(String url) {
                Log.i("NetSearch", "SogouSearch onNextUrl : " + url);
                updateLoadUrl(url);
            }

            @Override
            public void onLoadEnd() {
                Log.i("NetSearch", "SogouSearch onLoadEnd");
                updateLoadingState(false);
            }
        });
        mSearchList.add(SearchActivity.SEARCH_TYPE_BAIDU, baiduSearch);
        mSearchList.add(SearchActivity.SEARCH_TYPE_SOGOU, sogouSearch);
        AutoParseNetBook.setItemCallback(this);
    }

    @Override
    public void startSearch(int searchType, final String html) {
        updateLoadingState(true);
        mSearchType = searchType;
        mPool.execute(() -> {
            String value = handlerHtml(html);
            List<SearchModel.SearchBook> list = mSearchList.get(mSearchType).parseFirstPageHtml(value);
            updateData(list, false);
        });
    }

    @Override
    public void updateHtml(final String html) {
        mPool.execute(() -> {
            String value = handlerHtml(html);
            List<SearchModel.SearchBook> list = mSearchList.get(mSearchType).parsePageHtml(value);
            updateData(list, true);
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
        AutoParseNetBook.tryParseBook(book);
    }

    @Override
    public void stopParse() {
        AutoParseNetBook.stopParse();
    }

    @Override
    public void destroy() {
        super.destroy();
        AutoParseNetBook.setItemCallback(null);
        mPool.shutdown();
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

    private void updateLoadUrl(final String url) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onUrlLoad(url);
            }
        });
    }

    private void updateData(final List<SearchModel.SearchBook> list, final boolean add) {
        MyApp.runUI(() -> {
            if (mView != null) {
                if (add) {
                    mView.onDataAdd(list);
                } else {
                    mView.onDataChange(list);
                }
            }
        });
    }

    private void updateLoadingState(final boolean isLoading) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onLoadingState(isLoading);
            }
        });
    }

    private void updateSearchProgress(final int bookSize, final int parseSize, final String cur) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onSearchProgress(bookSize, parseSize, cur);
            }
        });
    }

    @Override
    public void onParseState(final SearchModel.SearchBook searchBook) {
        MyApp.runUI(() -> {
            if (mView != null) {
                mView.onParseState(searchBook);
            }
        });
    }
}
