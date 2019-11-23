package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.SearchModel;

import java.util.List;

public interface OtherContract {
    interface Presenter extends BaseContract.Presenter {

        void search(String key);

        void getChapterList(String readUrl, String host);

        void cancel();
    }

    interface View extends BaseContract.View<Presenter> {

        void onLoadingState(boolean loading);

        void onSearchProgress(int bookSize, int parseSize, String curHost);

        void onSearchResult(List<SearchModel.SearchBook> list);
    }
}
