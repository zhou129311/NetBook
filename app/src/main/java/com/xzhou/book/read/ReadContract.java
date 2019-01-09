package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface ReadContract {
    interface Presenter extends BaseContract.Presenter {

        void setTextViewParams(int maxLineCount, Paint paint, int width);

        void loadChapter(int chapter);

        void reloadCurPage();

        void loadPreviousPage();

        void loadNextPage();
    }

    interface View extends BaseContract.View<Presenter> {

        void initChapterList(List<Entities.Chapters> list);

        void onUpdatePages(PageContent[] pageContent);
    }
}
