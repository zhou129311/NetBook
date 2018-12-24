package com.xzhou.book.main;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.BaseContract;

import java.util.List;

public interface BookListContract {
    interface Presenter extends BaseContract.Presenter {
    }

    interface View extends BaseContract.View<Presenter> {
        void onDataChange(List<MultiItemEntity> list);

        void onLoadMore(List<MultiItemEntity> list);
    }
}
