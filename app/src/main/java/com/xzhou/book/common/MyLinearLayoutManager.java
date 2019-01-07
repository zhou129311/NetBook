package com.xzhou.book.common;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MyLinearLayoutManager extends LinearLayoutManager {
    private boolean mIsFixed = false;

    public MyLinearLayoutManager(Context context) {
        this(context, false);
    }

    public MyLinearLayoutManager(Context context, boolean fixed) {
        super(context);
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
