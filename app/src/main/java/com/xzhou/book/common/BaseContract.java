package com.xzhou.book.common;

public interface BaseContract {

    interface Presenter {
        boolean start();

        void destroy();
    }

    interface View<T> {
        void setPresenter(T presenter);
    }

}
