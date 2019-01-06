package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface ReadContract {
    interface Presenter extends BaseContract.Presenter {

        void setTextViewParams(int maxLineCount, Paint paint, int width);

        void previous();

        void next();
    }

    interface View extends BaseContract.View<Presenter> {

        void initChapterList(List<Entities.Chapters> list, PageContent pageContent, String chapterTitle, String pageNumber, @ReadPresenter.Error int error);

        void onUpdatePrePage(PageContent pageContent, String chapterTitle, String pageNumber, @ReadPresenter.Error int error);

        void onUpdateNextPage(PageContent pageContent, String chapterTitle, String pageNumber, @ReadPresenter.Error int error);

    }
}
