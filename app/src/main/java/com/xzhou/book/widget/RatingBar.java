package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

public class RatingBar extends LinearLayout {

    private int mStarSize; //px
    private int mStarCount;
    private int mStarPadding; //px
    private int mActiveCount;

    public RatingBar(Context context) {
        this(context, null);
    }

    public RatingBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);
        mStarSize = typedArray.getDimensionPixelSize(R.styleable.RatingBar_starSize, AppUtils.dip2px(10));
        mStarCount = typedArray.getInt(R.styleable.RatingBar_starCount, 5);
        mActiveCount = typedArray.getInt(R.styleable.RatingBar_activeCount, 5);
        mStarPadding = typedArray.getDimensionPixelSize(R.styleable.RatingBar_starPadding, AppUtils.dip2px(3));
        typedArray.recycle();
        initView();
    }

    private void initView() {
        int count = getChildCount();
        if (count == mStarCount) {
            for (int i = 0; i < mStarCount; i++) {
                ImageView view = (ImageView) getChildAt(i);
                view.setActivated(mActiveCount - i > 0);
            }
        } else {
            for (int i = 0; i < mStarCount; i++) {
                ImageView imageView = new ImageView(getContext());
                LayoutParams lp;
                if (mStarSize == 0) {
                    lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                } else {
                    lp = new LayoutParams(mStarSize, mStarSize);
                }
                if (i > 0) {
                    lp.leftMargin = mStarPadding;
                }
                addView(imageView, lp);
                imageView.setImageResource(R.drawable.sel_rating);
                imageView.setActivated(mActiveCount - i > 0);
            }
        }
    }

    public void setActiveCount(int activeCount) {
        mActiveCount = activeCount;
        initView();
    }
}
