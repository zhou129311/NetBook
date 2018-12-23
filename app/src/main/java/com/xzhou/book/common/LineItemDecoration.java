package com.xzhou.book.common;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;

public class LineItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private boolean isShowBottom;
    private int mSpanCount;

    public LineItemDecoration() {
        this(false);
    }

    public LineItemDecoration(int spanCount) {
        this(true);
        mSpanCount = spanCount;
    }

    public LineItemDecoration(boolean isShowBottom) {
        mDivider = new ColorDrawable(ContextCompat.getColor(MyApp.getContext(), R.color.common_divider_narrow));
        this.isShowBottom = isShowBottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (isShowDivider(view, parent)) {
            if (mSpanCount > 1) {
                int position = parent.getChildAdapterPosition(view); // item position
                //int count = parent.getAdapter().getItemCount();
                int itemType = parent.getAdapter().getItemViewType(position); // item type
                if (itemType == Constant.ITEM_TYPE_TEXT_GRID) {
                    outRect.left = 0;
                    outRect.right = 1;
                    outRect.bottom = 1;
                } else {
                    outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
                }
            } else {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = 0;
        if (mSpanCount <= 1) {
            left = parent.getPaddingLeft() + AppUtils.dip2px(60);
        }
        int right = parent.getWidth() - parent.getPaddingRight();

        if (parent.getChildCount() > 0) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (!isShowDivider(child, parent)) {
                    continue;
                }

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + 1;
                if (mSpanCount > 1) {
                    int position = parent.getChildAdapterPosition(child);
                    int itemType = parent.getAdapter().getItemViewType(position); // item type
                    if (itemType == Constant.ITEM_TYPE_TEXT_GRID) {
                        mDivider.setBounds(left, top, right, bottom);
                        drawHorizontalDivider(c, child, params);
                        if (right > child.getRight() + 1) {
                            drawVerticalDivider(c, child, params);
                        }
                        continue;
                    }
                } else {
                    mDivider.setBounds(left, top, right, bottom);
                }
                mDivider.draw(c);
            }
        }
    }

    private void drawHorizontalDivider(Canvas c, View child, RecyclerView.LayoutParams params) {
        int left = child.getLeft();
        int right = child.getRight();
        int top = child.getBottom() + params.bottomMargin;
        int bottom = top + 1;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    private void drawVerticalDivider(Canvas c, View child, RecyclerView.LayoutParams params) {
        int left = child.getRight();
        int right = left + 1;
        int top = child.getTop();
        int bottom = child.getBottom();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    private boolean isShowDivider(View view, RecyclerView parent) {
        int position = parent.getChildAdapterPosition(view);
        if ((position + 1) >= parent.getAdapter().getItemCount() && !isShowBottom) {
            return false;
        }
        return true;
    }
}
