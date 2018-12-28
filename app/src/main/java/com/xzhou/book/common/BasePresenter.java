package com.xzhou.book.common;

public class BasePresenter<V extends BaseContract.View> implements BaseContract.Presenter {
    protected V mView;

    public BasePresenter(V view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void destroy() {
        mView = null;
    }
}
