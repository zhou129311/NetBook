package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.SearchModel;

import java.util.List;

/**
 * packageName：com.xzhou.book.search
 * ProjectName：NetBook
 * Description：
 * Author：zhouxian
 * Date：2019/12/21 19:35
 */
public interface NetSearchContract {
    interface Presenter extends BaseContract.Presenter {

        void startSearch(int searchType, String html);

        void updateHtml(String html);

        void cancel();

        void tryParseBook(SearchModel.SearchBook book);

        void stopParse();
    }

    interface View extends BaseContract.View<Presenter> {

        void onUrlLoad(String url);

        void onLoadingState(boolean loading);

        void onSearchProgress(int bookSize, int parseSize, String curHost);

        void onDataChange(List<SearchModel.SearchBook> list);

        void onDataAdd(List<SearchModel.SearchBook> list);

        void onParseState(SearchModel.SearchBook searchBook);
    }
}
