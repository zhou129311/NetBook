package com.xzhou.book.common;

public class BasePresenter<V extends BaseContract.BaseView> implements BaseContract.BasePresenter {
    protected V mView;

    public BasePresenter(V view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public void destroy() {
        mView = null;
    }
}
