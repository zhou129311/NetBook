package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.BaiduModel;

import java.util.List;

public interface BaiduContract {
    interface Presenter extends BaseContract.Presenter {

        void search(String key);

        void getChapterList(String readUrl, String host);

        void cancel();
    }

    interface View extends BaseContract.View<Presenter> {

        void onLoadingState(boolean loading);

        void onSearchProgress(int bookSize, String curHost);

        void onSearchResult(List<BaiduModel.BaiduBook> list);
    }
}
