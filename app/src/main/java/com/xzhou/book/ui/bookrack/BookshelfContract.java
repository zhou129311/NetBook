package com.xzhou.book.ui.bookrack;

import com.xzhou.book.models.Entities;
import com.xzhou.book.ui.common.BaseContract;

import java.util.List;

public interface BookshelfContract {

    interface Presenter extends BaseContract.BasePresenter {

        void refresh();

    }

    interface View extends BaseContract.BaseView<Presenter> {
        void showLoading();

        void hideLoading();

        void onDataChange(List<Entities.Book> books);

        void onAdd(Entities.Book book);

        void onRemove(Entities.Book book);
    }
}
