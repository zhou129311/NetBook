package com.xzhou.book.read;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface CartoonContract {
    interface Presenter extends BaseContract.Presenter {

        void loadChapter(int itemPosition, int chapter);

        void reloadCurPage(int itemPosition, CartoonContent pageContent);

        void loadPreviousPage(int itemPosition, CartoonContent pageContent);

        void loadNextPage(int itemPosition, CartoonContent pageContent);
    }

    interface View extends BaseContract.View<Presenter> {

        void initChapterList(List<Entities.Chapters> list);

        void onUpdatePages(CartoonContent[] pageContent);

        void onUpdateSource(List<Entities.BookSource> list);
    }
}
