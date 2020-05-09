package com.xzhou.book.search;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.HtmlParse;
import com.xzhou.book.models.HtmlParseFactory;
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
public class NetSearchPresenter extends BasePresenter<NetSearchContract.View> implements NetSearchContract.Presenter, AutoParseNetBook.Callback {

    private List<JsoupSearch> mSearchList = new ArrayList<>(2);
    private int mSearchType;
    private ExecutorService mPool = Executors.newSingleThreadExecutor();

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
        sogouSearch.setProgressCallback(new JsoupSearch.ProgressCallback() {
            @Override
            public void onCurParse(int size, int parseSize, String curResult) {
                updateSearchProgress(size, parseSize, curResult);
            }
        });
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
        AutoParseNetBook.addCallback(this);
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
        AutoParseNetBook.tryParseBook(book.bookName, book.readUrl, book.sourceHost);
    }

    @Override
    public void stopParse() {
        AutoParseNetBook.stopParse();
    }

    @Override
    public void destroy() {
        super.destroy();
        AutoParseNetBook.removeCallback(this);
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

    @Override
    public void onParseState(final boolean state, final boolean success, final String message) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onParseState(state, success, message);
                }
            }
        });
    }
}
