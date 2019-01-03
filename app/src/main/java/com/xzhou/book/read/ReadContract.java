package com.xzhou.book.read;

import com.xzhou.book.common.BaseContract;

public interface ReadContract {
    interface Presenter extends BaseContract.Presenter {
    }

    interface View extends BaseContract.View<Presenter> {
    }
}
