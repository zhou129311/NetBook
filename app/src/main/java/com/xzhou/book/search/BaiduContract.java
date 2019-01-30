package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.BaiduEntities;

import java.util.List;

public interface BaiduContract {
    interface Presenter extends BaseContract.Presenter {

        void search(String key);

        void getChapterList(String readUrl);
    }

    interface View extends BaseContract.View<Presenter> {

        void onSearchResult(List<BaiduEntities.BaiduBook> list);
    }
}
