package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface ReadContract {
    interface Presenter extends BaseContract.Presenter {

        void setTextViewParams(int maxLineCount, Paint paint, int width, PageLines pageLines);

        void loadChapter(int itemPosition, int chapter);

        void reloadCurPage(int itemPosition);

        void loadPreviousPage(int itemPosition);

        void loadNextPage(int itemPosition);
    }

    interface View extends BaseContract.View<Presenter> {

        void initChapterList(List<Entities.Chapters> list);

        void onUpdatePages(PageContent[] pageContent);
    }
}
