package com.xzhou.book.common;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

public interface TabContract {
    interface Presenter extends BaseContract.Presenter {

        void setFiltrate(String filtrate);

        void refresh();

        void loadMore();

    }

    interface View extends BaseContract.View<Presenter> {

        void onRefreshStateChange(boolean isRefreshing);

        void onDataChange(List<MultiItemEntity> list);

        void onLoadMore(List<MultiItemEntity> list);
    }
}
