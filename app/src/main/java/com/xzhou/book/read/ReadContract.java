package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface ReadContract {
    interface Presenter extends BaseContract.Presenter {

        void setTextViewParams(int maxLineCount, Paint paint, int width, PageLines pageLines);

        void loadChapter(int itemPosition, int chapter);

        void reloadCurPage(int itemPosition, PageContent pageContent);

        void loadPreviousPage(int itemPosition, PageContent pageContent);

        void loadNextPage(int itemPosition, PageContent pageContent);

        void loadAllSource();
    }

    interface View extends BaseContract.View<Presenter> {

        void initChapterList(List<Entities.Chapters> list);

        void onUpdatePages(PageContent[] pageContent);

        void onUpdateSource(List<Entities.BookSource> list);
    }
}
