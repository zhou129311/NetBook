package com.xzhou.book.common;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpanCount;
    private int mEdgeSpace;
    private int mSpace;

    public GridItemDecoration(int spanCount, int space, int edgeSpace) {
        mSpanCount = spanCount;
        mSpace = space;
        mEdgeSpace = edgeSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int childPosition = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        setGridOffset(mSpanCount, outRect, childPosition, itemCount);
    }

    private void setGridOffset(int spanCount, Rect outRect, int childPosition, int itemCount) {
        float totalSpace = mSpace * (spanCount - 1) + mEdgeSpace * 2; // 总共的padding值
        float eachSpace = totalSpace / spanCount; // 分配给每个item的padding值
        int column = childPosition % spanCount; // 当前child所在列数 0-(spanCount-1)
        int row = childPosition / spanCount;// 当前child所在行数 0-(itemCount / spanCount - 1)
        float left;
        float right;
        float top;
        float bottom;
        top = 0;
        bottom = mSpace;
        if (mEdgeSpace == 0) { //无边距
            left = column * eachSpace / (spanCount - 1);
            right = eachSpace - left;
            if (itemCount / spanCount == (row + 1)) {
                bottom = 0;
            }
        } else {
            if (childPosition < spanCount) {
                top = mEdgeSpace;
            } else if (itemCount / spanCount == row) {
                bottom = mEdgeSpace;
            }
            left = column * (eachSpace - mEdgeSpace - mEdgeSpace) / (spanCount - 1) + mEdgeSpace;
            right = eachSpace - left;
        }
        outRect.set((int) left, (int) top, (int) right, (int) bottom);
    }
}
