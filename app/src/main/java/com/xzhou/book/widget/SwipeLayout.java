package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.xzhou.book.R;

public class SwipeLayout extends LinearLayout {
    public static final int STATE_CLOSED = 0;//关闭状态
    public static final int STATE_OPEN = 1;//打开状态
    public static final int STATE_MOVING_LEFT = 2;//左滑将要打开状态
    public static final int STATE_MOVING_RIGHT = 3;//右滑将要关闭状态

    public int currentState = STATE_CLOSED;
    private int menuWidth;//菜单总长度
    private OverScroller mScroller;
    private int mScaledTouchSlop;
    private int mRightId;//右边隐藏菜单id
    private View rightMenuView;//右边的菜单按钮
    private View leftView;
    private float mDownX, mDownY, mLastX;
    //    private boolean isCanScroll;
    private OnStateListener mStateListener;

    public interface OnStateListener {
        void onOpen();

        void onClose();
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mScaledTouchSlop = configuration.getScaledTouchSlop();
//        configuration.getScaledMaximumFlingVelocity();
//        configuration.getScaledMinimumFlingVelocity();
        //获取右边菜单id
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        mRightId = typedArray.getResourceId(R.styleable.SwipeLayout_right_id, 0);
        typedArray.recycle();
    }

//    public void setCanScroll(boolean canScroll) {
//        isCanScroll = canScroll;
//    }

    public void setOnStateListener(OnStateListener listener) {
        mStateListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mRightId != 0) {
            rightMenuView = findViewById(mRightId);
        }
        leftView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        menuWidth = rightMenuView.getMeasuredWidth();
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mDownX = event.getX();
            mDownY = event.getY();
            mLastX = mDownX;
            break;
        //return true;
        case MotionEvent.ACTION_MOVE:
            int dx = (int) (mDownX - event.getX());
            int dy = (int) (mDownY - event.getY());
            //如果Y轴偏移量大于X轴偏移量 不再滑动
            if (Math.abs(dy) > Math.abs(dx)) return false;

            int deltaX = (int) (mLastX - event.getX());
            if (onMove(deltaX)) {
                return true;
            }
            mLastX = event.getX();
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            onUpOrCancel();
            //如果小于滑动距离并且菜单是关闭状态 此时Item可以有点击事件
            int deltx = (int) (mDownX - event.getX());
            boolean intercept = Math.abs(deltx) < mScaledTouchSlop && isMenuClosed();
            return !intercept || super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public boolean onMove(int deltaX) {
        if (deltaX > 0) {
            currentState = STATE_MOVING_LEFT;
            if (deltaX >= menuWidth || getScrollX() + deltaX >= menuWidth) {
                //右边缘检测
                scrollTo(menuWidth, 0);
                currentState = STATE_OPEN;
                if (mStateListener != null) {
                    mStateListener.onOpen();
                }
                return true;
            }
        } else {
            //向右滑动
            currentState = STATE_MOVING_RIGHT;
            if (deltaX + getScrollX() <= 0) {
                //左边缘检测
                scrollTo(0, 0);
                currentState = STATE_CLOSED;
                if (mStateListener != null) {
                    mStateListener.onClose();
                }
                return true;
            }
        }
        scrollBy(deltaX, 0);
        return false;
    }

    public void onUpOrCancel() {
        if (currentState == STATE_MOVING_LEFT) {
            //左滑打开
            mScroller.startScroll(getScrollX(), 0, menuWidth - getScrollX(), 0, 300);
            invalidate();
        } else if (currentState == STATE_MOVING_RIGHT) {
            //右滑关闭
            smoothToCloseMenu();
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return isCanScroll || super.onInterceptTouchEvent(ev);
//    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // Get current x and y positions
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();
            scrollTo(currX, currY);
            postInvalidate();
        }
        if (isMenuOpen()) {
            currentState = STATE_OPEN;
            if (mStateListener != null) {
                mStateListener.onOpen();
            }
        } else if (isMenuClosed()) {
            currentState = STATE_CLOSED;
            if (mStateListener != null) {
                mStateListener.onClose();
            }
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public boolean isMenuOpen() {
        return getScrollX() >= menuWidth;
    }

    public boolean isMenuClosed() {
        return getScrollX() <= 0;
    }

    public void smoothToCloseMenu() {
        if (currentState != STATE_CLOSED) {
            int sx = getScrollX();
            mScroller.startScroll(sx, 0, -sx, 0, 300);
            invalidate();
        }
    }

    public void smoothToOpenMenu() {
        if (currentState == STATE_CLOSED) {
            int sx = getScrollX();
            mScroller.startScroll(sx, 0, menuWidth - sx, 0, 300);
            invalidate();
        }
    }
}