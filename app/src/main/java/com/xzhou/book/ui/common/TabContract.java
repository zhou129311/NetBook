package com.xzhou.book.ui.common;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

public interface TabContract {
    interface Presenter extends BaseContract.BasePresenter {

        void refresh();

        void loadMore();

    }

    interface View extends BaseContract.BaseView<Presenter> {

        void onDataChange(List<MultiItemEntity> list);

        void onLoadMore(List<MultiItemEntity> list);
    }
}
