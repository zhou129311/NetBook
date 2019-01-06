package com.xzhou.book.read;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xzhou.book.utils.AppUtils;

public class ReadViewPager extends ViewPager {
    private boolean isCanScroll = true; //是否可以切换页面
    private boolean isCanTouch = true; //是否可以手势滑动
    private Rect mRect = new Rect();
    private float mDownX;
    private float mDownY;
    private int mCenterX;

    private OnClickChangePageListener mClickChangePageListener;
    private OnClickListener mOnClickListener;

    public interface OnClickChangePageListener {
        void onPrevious();

        void onNext();
    }

    public ReadViewPager(@NonNull Context context) {
        this(context, null);
    }

    public ReadViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCenterX = AppUtils.getScreenWidth() / 2;
        int left = AppUtils.getScreenWidth() / 3;
        int right = left * 2;
        int top = AppUtils.getScreenHeight() / 3;
        int bottom = top * 2;
        mRect.set(left, top, right, bottom);

    }

    public void setOnClickChangePageListener(OnClickChangePageListener listener) {
        mClickChangePageListener = listener;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }


    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void setScanTouch(boolean isCanScroll) {
        this.isCanTouch = isCanScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isCanScroll) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mDownX = ev.getRawX();
            mDownY = ev.getRawY();
            return true;
        case MotionEvent.ACTION_UP:
            float upX = ev.getRawX();
            float upY = ev.getRawY();
            if (Math.abs(upX - mDownX) < 5 && Math.abs(upY - mDownY) < 10) {
                if (hasClickCenterRect(upX, upY)) {
                    return performClick();
                } else if (upX <= mCenterX) {
                    if (mClickChangePageListener != null) {
                        mClickChangePageListener.onPrevious();
                    }
                } else {
                    if (mClickChangePageListener != null) {
                        mClickChangePageListener.onNext();
                    }
                }
                return true;
            }
            break;
        }
        return isCanTouch && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isCanTouch && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            return true;
        }
        return super.performClick();
    }

    private boolean hasClickCenterRect(float x, float y) {
        return x > mRect.left && x < mRect.right && y < mRect.bottom && y > mRect.top;
    }
}
