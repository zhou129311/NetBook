package com.xzhou.book.common;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyGridLayoutManager extends GridLayoutManager {
    private boolean mIsFixed = false; //是否可以滑动

    public MyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public MyGridLayoutManager(Context context, int spanCount, boolean fixed) {
        super(context, spanCount);
        mIsFixed = fixed;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean canScrollVertically() {
        if (mIsFixed) {
            return false;
        }
        return super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        if (mIsFixed) {
            return false;
        }
        return super.canScrollHorizontally();
    }
}
