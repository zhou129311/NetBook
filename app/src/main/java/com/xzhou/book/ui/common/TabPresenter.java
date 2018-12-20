package com.xzhou.book.ui.common;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {
    private int mSource;

    public TabPresenter(TabContract.View view, int source) {
        super(view);
        mSource = source;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void loadMore() {

    }
}
