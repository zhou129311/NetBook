package com.xzhou.book.widget;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.xzhou.book.R;

public class CommonLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.common_loadmore_view;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_fail;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_no_more;
    }
}
