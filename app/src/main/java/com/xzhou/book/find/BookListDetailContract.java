package com.xzhou.book.find;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

public interface BookListDetailContract {
    interface Presenter extends BaseContract.Presenter {
    }

    interface View extends BaseContract.View<Presenter> {
        void onInitData(Entities.BookListDetail detail);
    }
}
