package com.xzhou.book.ui.main;

public interface BaseContract {

    interface BasePresenter {
        void start();

        void destroy();
    }

    interface BaseView<T> {
        void setPresenter(T presenter);
    }

}
