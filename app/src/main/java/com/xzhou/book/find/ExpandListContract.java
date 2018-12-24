package com.xzhou.book.find;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.BaseContract;

import java.util.List;

public interface ExpandListContract {

    interface Presenter extends BaseContract.Presenter {

    }

    interface View extends BaseContract.View<Presenter> {
        void initData(List<MultiItemEntity> list);
    }
}
