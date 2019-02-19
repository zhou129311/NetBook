package com.xzhou.book.common;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

public class MyLinearLayoutManager extends LinearLayoutManager {
    private boolean mIsFixed;

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

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return MyLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            //控制速度。
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return super.calculateSpeedPerPixel(displayMetrics);
            }
            @Override
            protected int calculateTimeForScrolling(int dx) {
                if (dx > 3000) {
                    dx = 3000;
                }
                return super.calculateTimeForScrolling(dx);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
