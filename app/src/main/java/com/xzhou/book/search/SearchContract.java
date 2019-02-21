package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface SearchContract {
    interface Presenter extends BaseContract.Presenter {

        void search(String key);

        void autoComplete(String key);

        void loadMore();
    }

    interface View extends BaseContract.View<Presenter> {

        void onSearchResult(List<Entities.SearchBook> list);

        void onLoadMore(List<Entities.SearchBook> list);

        void onAutoComplete(List<Entities.Suggest> list);

        void onLoadState(boolean loading);
    }
}
