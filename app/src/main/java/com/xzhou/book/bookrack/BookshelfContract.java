package com.xzhou.book.bookrack;

import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseContract;

import java.util.List;

public interface BookshelfContract {

    interface Presenter extends BaseContract.Presenter {

        void refresh();

    }

    interface View extends BaseContract.View<Presenter> {
        void showLoading();

        void hideLoading();

        void onDataChange(List<Entities.NetBook> books);

        void onAdd(Entities.NetBook book);

        void onRemove(Entities.NetBook book);
    }
}
