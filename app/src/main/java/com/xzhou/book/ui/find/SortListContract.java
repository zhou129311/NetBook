package com.xzhou.book.ui.find;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.ui.common.BaseContract;

import java.util.List;

public interface SortListContract {

    interface Presenter extends BaseContract.BasePresenter {

    }

    interface View extends BaseContract.BaseView<Presenter> {
        void initData(List<MultiItemEntity> list);
    }
}
