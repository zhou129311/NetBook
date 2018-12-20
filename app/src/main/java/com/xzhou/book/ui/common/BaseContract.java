package com.xzhou.book.ui.common;

public interface BaseContract {

    interface BasePresenter {
        boolean start();

        void destroy();
    }

    interface BaseView<T> {
        void setPresenter(T presenter);
    }

}
