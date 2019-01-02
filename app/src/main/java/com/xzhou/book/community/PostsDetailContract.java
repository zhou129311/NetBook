package com.xzhou.book.community;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.BaseContract;

import java.util.List;

public interface PostsDetailContract {
    interface Presenter extends BaseContract.Presenter {
        void loadMore();
    }

    interface View extends BaseContract.View<Presenter> {
        void onLoading(boolean isLoading);

        void onInitPostDetail(Object detail);

        void onInitData(List<MultiItemEntity> list);

        void onLoadMore(List<MultiItemEntity> list);
    }
}
