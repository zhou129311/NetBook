package com.xzhou.book.community;

import com.xzhou.book.common.BaseContract;

public interface DiscussContract {

    interface Presenter extends BaseContract.Presenter {

    }

    interface View extends BaseContract.View<Presenter> {
    }

}
