package com.xzhou.book.community;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.BaseContract;

import java.util.HashMap;
import java.util.List;

public interface DiscussContract {

    interface Presenter extends BaseContract.Presenter {
        void refresh();

        void loadMore();

        void setParams(HashMap<String, String> params);
    }

    interface View extends BaseContract.View<Presenter> {

        void onRefreshStateChange(boolean isRefreshing);

        void onDataChange(List<MultiItemEntity> list);

        void onLoadMore(List<MultiItemEntity> list);
    }

}
