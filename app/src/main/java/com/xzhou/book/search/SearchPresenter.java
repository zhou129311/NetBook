package com.xzhou.book.search;

import android.text.TextUtils;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter extends BasePresenter<SearchContract.View> implements SearchContract.Presenter {
    private static final int PAGE_SIZE = 20;

    private int mDataNumber;
    private String mKey;

    SearchPresenter(SearchContract.View view) {
        super(view);
    }

    @Override
    public void search(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mKey = key;
        mView.onAutoComplete(null);
        mView.onLoadState(true);
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                mDataNumber = 0;
                List<Entities.SearchBook> list = getSearchResult();
                if (list != null && list.size() > 0) {
                    mDataNumber = list.size();
                }
                updateSearchResult(list, false);
            }
        });
    }

    @Override
    public void autoComplete(final String key) {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.Suggest> list = null;
                Entities.AutoSuggest autoSuggest = ZhuiShuSQApi.getAutoSuggest(key);
//                Entities.AutoComplete autoComplete = ZhuiShuSQApi.getAutoComplete(key);
                if (autoSuggest != null && autoSuggest.keywords != null) {
                    list = autoSuggest.keywords;
                    for (Entities.Suggest suggest : list) {
                        if (suggest.isCat()) {
                            Entities.CategoryListLv2 categoryListLv2 = ZhuiShuSQApi.getCategoryListLv2();
                            if (categoryListLv2 == null) {
                                list.remove(suggest);
                                break;
                            }
                            if ("male".equals(suggest.gender)) {
                                suggest.minors = getMinors(suggest.major, categoryListLv2.male);
                            } else {
                                suggest.minors = getMinors(suggest.major, categoryListLv2.female);
                            }
                            break;
                        }
                    }
                }
                updateAutoComplete(list);
            }
        });
    }

    private List<String> getMinors(String major, List<Entities.CategoryListLv2.Category> categories) {
        for (Entities.CategoryListLv2.Category category : categories) {
            if (TextUtils.equals(major, category.major)) {
                return category.mins;
            }
        }
        return null;
    }

    @Override
    public void loadMore() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.SearchBook> list = getSearchResult();
                if (list != null && list.size() > 0) {
                    mDataNumber += list.size();
                }
                updateSearchResult(list, true);
            }
        });
    }

    private List<Entities.SearchBook> getSearchResult() {
        if (mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>();
        }
        List<Entities.SearchBook> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        Entities.SearchResult result = ZhuiShuSQApi.getSearchResult(mKey, start, limit);
        if (result != null) {
            list = new ArrayList<>();
            if (result.books != null && result.books.size() > 0) {
                list.addAll(result.books);
            }
        }
        return list;
    }

    private void updateSearchResult(final List<Entities.SearchBook> list, final boolean hasLoadMore) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                mView.onLoadState(false);
                if (mView != null) {
                    if (hasLoadMore) {
                        mView.onLoadMore(list);
                    } else {
                        mView.onSearchResult(list);
                        mView.onAutoComplete(null);
                    }
                }
            }
        });
    }

    private void updateAutoComplete(final List<Entities.Suggest> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onAutoComplete(list);
                }
            }
        });
    }
}
