package com.xzhou.book.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class SwipeItemLayout extends ViewGroup {

    enum Mode {
        RESET, DRAG, FLING, TAP
    }

    private Mode mTouchMode;
    private boolean mSwipeEnabled = true;

    private View mMainView;
    private View mSideView;

    private ScrollRunnable mScrollRunnable;
    private int mScrollOffset;
    private int mMaxScrollOffset;

    private boolean mInLayout;
    private boolean mIsLaidOut;
    private int mTouchSlop;

    public SwipeItemLayout(Context context) {
        this(context, null);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchMode = Mode.RESET;
        mScrollOffset = 0;
        mIsLaidOut = false;

        mScrollRunnable = new ScrollRunnable(context);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public boolean isOpen() {
        return mScrollOffset != 0;
    }

    Mode getTouchMode() {
        return mTouchMode;
    }

    void setTouchMode(Mode mode) {
        switch (mTouchMode) {
        case FLING:
            mScrollRunnable.abort();
            break;
        case RESET:
            break;
        }

        mTouchMode = mode;
    }

    public void open() {
        if (mScrollOffset != -mMaxScrollOffset) {
            if (mTouchMode == Mode.FLING && mScrollRunnable.isScrollToLeft())
                return;

            if (mTouchMode == Mode.FLING /* && !mScrollRunnable.mScrollToLeft */)
                mScrollRunnable.abort();

            mScrollRunnable.startScroll(mScrollOffset, -mMaxScrollOffset);
        }
    }

    public void close() {
        if (mScrollOffset != 0) {
            if (mTouchMode == Mode.FLING && !mScrollRunnable.isScrollToLeft()) {
                return;
            }

            if (mTouchMode == Mode.FLING /* && mScrollRunnable.mScrollToLeft */)
                mScrollRunnable.abort();

            mScrollRunnable.startScroll(mScrollOffset, 0);
        }
    }

    void fling(int xVel) {
        mScrollRunnable.startFling(mScrollOffset, xVel);
    }

    void revise() {
        if (mScrollOffset < -mMaxScrollOffset / 2)
            open();
        else
            close();
    }

    boolean trackMotionScroll(int deltaX) {
        if (deltaX == 0)
            return false;

        boolean over = false;
        int newLeft = mScrollOffset + deltaX;
        if ((deltaX > 0 && newLeft > 0) || (deltaX < 0 && newLeft < -mMaxScrollOffset)) {
            over = true;
            newLeft = Math.min(newLeft, 0);
            newLeft = Math.max(newLeft, -mMaxScrollOffset);
        }

        offsetChildrenLeftAndRight(newLeft - mScrollOffset);
        mScrollOffset = newLeft;
        return over;
    }

    private boolean ensureChildren() {
        int childCount = getChildCount();

        if (childCount != 2)
            return false;

        View childView = getChildAt(0);
        mMainView = childView;

        childView = getChildAt(1);
        mSideView = childView;
        return true;
    }

    public boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public void setSwipeEnabled(boolean mSwipeEnabled) {
        this.mSwipeEnabled = mSwipeEnabled;
        if (!mSwipeEnabled) {
            close();
        }
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        View childView = getChildAt(0);
        return childView.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureChildren();
        measureChild(mMainView, widthMeasureSpec, heightMeasureSpec);
        int heightSpec = MeasureSpec.makeMeasureSpec(mMainView.getMeasuredHeight(), MeasureSpec.EXACTLY);
        measureChild(mSideView, widthMeasureSpec, heightSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mSideView == null) {
            if (!ensureChildren()) {
                return;
            }
        }
        mInLayout = true;

        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();

        MarginLayoutParams mainLp = (MarginLayoutParams) mMainView.getLayoutParams();
        MarginLayoutParams sideParams = (MarginLayoutParams) mSideView.getLayoutParams();
        int childLeft = pl + mainLp.leftMargin;
        int childTop = pt + mainLp.topMargin;
        int childRight = getWidth() - (pr + mainLp.rightMargin);
        int childBottom = getHeight() - (mainLp.bottomMargin + pb);
        mMainView.layout(childLeft, childTop, childRight, childBottom);

        childLeft = childRight + sideParams.leftMargin;
        childTop = pt + sideParams.topMargin;
        childRight = childLeft + sideParams.leftMargin + sideParams.rightMargin + mSideView.getMeasuredWidth();
        childBottom = getHeight() - (sideParams.bottomMargin + pb);
        mSideView.layout(childLeft, childTop, childRight, childBottom);
        mMaxScrollOffset = mSideView.getWidth() + sideParams.leftMargin + sideParams.rightMargin;
        mScrollOffset = mScrollOffset < -mMaxScrollOffset / 2 ? -mMaxScrollOffset : 0;

        offsetChildrenLeftAndRight(mScrollOffset);
        mInLayout = false;
        mIsLaidOut = true;
    }

    void offsetChildrenLeftAndRight(int delta) {
        ViewCompat.offsetLeftAndRight(mMainView, delta);
        ViewCompat.offsetLeftAndRight(mSideView, delta);
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams ? p : new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mScrollOffset != 0 && mIsLaidOut) {
            offsetChildrenLeftAndRight(-mScrollOffset);
            mScrollOffset = 0;
        } else
            mScrollOffset = 0;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mScrollOffset != 0 && mIsLaidOut) {
            offsetChildrenLeftAndRight(-mScrollOffset);
            mScrollOffset = 0;
        } else
            mScrollOffset = 0;
        removeCallbacks(mScrollRunnable);
    }

    int mMotionX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeEnabled) {
            return false;
        }
        final int x = (int) ev.getX();
        final int deltaX = x - mMotionX;
        mMotionX = x;
        final int action = ev.getActionMasked();
        switch (action) {
        case MotionEvent.ACTION_DOWN: {
            mMotionX = (int) ev.getX();
            break;
        }

        case MotionEvent.ACTION_MOVE:
            if (Math.abs(deltaX) > mTouchSlop) {
                return true;
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return false;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (getVisibility() != View.VISIBLE) {
            mScrollOffset = 0;
            invalidate();
        }
    }

    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    class ScrollRunnable implements Runnable {
        private static final int FLING_DURATION = 200;
        private Scroller mScroller;
        private boolean mAbort;
        private int mMinVelocity;
        private boolean mScrollToLeft;

        ScrollRunnable(Context context) {
            mScroller = new Scroller(context, sInterpolator);
            mAbort = false;
            mScrollToLeft = false;

            ViewConfiguration configuration = ViewConfiguration.get(context);
            mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        }

        void startScroll(int startX, int endX) {
            if (startX != endX) {
                setTouchMode(Mode.FLING);
                mAbort = false;
                mScrollToLeft = endX < startX;
                mScroller.startScroll(startX, 0, endX - startX, 0, 400);
                ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
            }
        }

        void startFling(int startX, int xVel) {

            if (xVel > mMinVelocity && startX != 0) {
                startScroll(startX, 0);
                return;
            }

            if (xVel < -mMinVelocity && startX != -mMaxScrollOffset) {
                startScroll(startX, -mMaxScrollOffset);
                return;
            }

            startScroll(startX, startX > -mMaxScrollOffset / 2 ? 0 : -mMaxScrollOffset);
        }

        void abort() {
            if (!mAbort) {
                mAbort = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    removeCallbacks(this);
                }
            }
        }

        boolean isScrollToLeft() {
            return mScrollToLeft;
        }

        @Override
        public void run() {
            if (!mAbort) {
                boolean more = mScroller.computeScrollOffset();
                int curX = mScroller.getCurrX();

                boolean atEdge = trackMotionScroll(curX - mScrollOffset);
                if (more && !atEdge) {
                    ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
                    return;
                }

                if (atEdge) {
                    removeCallbacks(this);
                    if (!mScroller.isFinished())
                        mScroller.abortAnimation();
                    setTouchMode(Mode.RESET);
                }

                if (!more) {
                    setTouchMode(Mode.RESET);
                    if (mScrollOffset != 0) {
                        if (Math.abs(mScrollOffset) > mMaxScrollOffset / 2)
                            mScrollOffset = -mMaxScrollOffset;
                        else
                            mScrollOffset = 0;
                        ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
                    }
                }
            }
        }
    }

    public static class OnSwipeItemTouchListener implements RecyclerView.OnItemTouchListener {
        private SwipeItemLayout mCaptureItem;
        private float mLastMotionX;
        private float mLastMotionY;
        private VelocityTracker mVelocityTracker;

        private int mActivePointerId;

        private int mTouchSlop;
        private int mMaximumVelocity;

        private boolean mDealByParent;
        private boolean mIsProbeParent;

        public OnSwipeItemTouchListener(Context context) {
            ViewConfiguration configuration = ViewConfiguration.get(context);
            mTouchSlop = configuration.getScaledTouchSlop();
            mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
            mActivePointerId = -1;
            mDealByParent = false;
            mIsProbeParent = false;
        }

        private float[] mTempPoint;

        private float[] getTempPoint() {
            if (mTempPoint == null) {
                mTempPoint = new float[2];
            }
            return mTempPoint;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
            if (mIsProbeParent)
                return false;

            boolean intercept = false;
            final int action = ev.getActionMasked();

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);

            switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(0);
                final float x = ev.getX();
                final float y = ev.getY();
                mLastMotionX = x;
                mLastMotionY = y;

                boolean pointOther = false;
                SwipeItemLayout pointItem = null;
                View pointView = findTopChildUnder(rv, (int) x, (int) y);
                if (pointView instanceof SwipeItemLayout) {
                    pointItem = (SwipeItemLayout) pointView;
                    if (!pointItem.isSwipeEnabled()) {
                        pointOther = true;
                    }
                } else {
                    pointOther = true;
                }

                if (!pointOther && (mCaptureItem == null || mCaptureItem != pointItem)) {
                    pointOther = true;
                }

                if (!pointOther) {
                    Mode touchMode = mCaptureItem.getTouchMode();

                    boolean disallowIntercept = false;
                    if (touchMode == Mode.FLING) {
                        mCaptureItem.setTouchMode(Mode.DRAG);
                        disallowIntercept = true;
                        intercept = true;
                    } else {
                        mCaptureItem.setTouchMode(Mode.TAP);
                        // if (mCaptureItem.isOpen())
                        // disallowIntercept = true;
                    }

                    if (disallowIntercept) {
                        final ViewParent parent = rv.getParent();
                        if (parent != null)
                            parent.requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    if (mCaptureItem != null && mCaptureItem.isOpen()) {
                        mCaptureItem.close();
                        mCaptureItem = null;
                        intercept = true;
                    }
                    if (pointItem != null) {
                        mCaptureItem = pointItem;
                        mCaptureItem.setTouchMode(Mode.TAP);
                    } else {
                        mCaptureItem = null;
                    }
                }

                mIsProbeParent = true;
                mDealByParent = rv.onInterceptTouchEvent(ev);
                mIsProbeParent = false;
                if (mDealByParent) {
                    intercept = false;
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int actionIndex = ev.getActionIndex();
                mActivePointerId = ev.getPointerId(actionIndex);

                mLastMotionX = ev.getX(actionIndex);
                mLastMotionY = ev.getY(actionIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int actionIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(actionIndex);
                if (pointerId == mActivePointerId) {
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newIndex);

                    mLastMotionX = ev.getX(newIndex);
                    mLastMotionY = ev.getY(newIndex);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1)
                    break;

                if (mDealByParent || (mCaptureItem != null && !mCaptureItem.isSwipeEnabled())) {
                    if (mCaptureItem != null && mCaptureItem.isOpen())
                        mCaptureItem.close();
                    return false;
                }

                final int x = (int) (ev.getX(activePointerIndex) + .5f);
                final int y = (int) ((int) ev.getY(activePointerIndex) + .5f);

                int deltaX = (int) (x - mLastMotionX);
                int deltaY = (int) (y - mLastMotionY);
                final int xDiff = Math.abs(deltaX);
                final int yDiff = Math.abs(deltaY);

                if (mCaptureItem != null && !mDealByParent) {
                    Mode touchMode = mCaptureItem.getTouchMode();

                    if (touchMode == Mode.TAP) {
                        if (xDiff > mTouchSlop && xDiff > yDiff) {
                            mCaptureItem.setTouchMode(Mode.DRAG);
                            final ViewParent parent = rv.getParent();
                            parent.requestDisallowInterceptTouchEvent(true);

                            deltaX = deltaX > 0 ? deltaX - mTouchSlop : deltaX + mTouchSlop;
                        } else {// if(yDiff>mTouchSlop){
                            mIsProbeParent = true;
                            boolean isParentConsume = rv.onInterceptTouchEvent(ev);
                            mIsProbeParent = false;
                            if (isParentConsume) {
                                mDealByParent = true;
                                mCaptureItem.close();
                            }
                        }
                    }

                    touchMode = mCaptureItem.getTouchMode();
                    if (touchMode == Mode.DRAG) {
                        intercept = true;
                        mLastMotionX = x;
                        mLastMotionY = y;

                        mCaptureItem.trackMotionScroll(deltaX);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mCaptureItem != null) {
                    Mode touchMode = mCaptureItem.getTouchMode();
                    if (touchMode == Mode.DRAG) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int xVel = (int) velocityTracker.getXVelocity(mActivePointerId);
                        mCaptureItem.fling(xVel);

                        intercept = true;
                    }
                }
                cancel();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mCaptureItem != null)
                    mCaptureItem.revise();
                cancel();
                break;
            }

            return intercept;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent ev) {
            if (mCaptureItem != null && !mCaptureItem.isSwipeEnabled()) {
                return;
            }
            final int action = ev.getActionMasked();
            final int actionIndex = ev.getActionIndex();

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);

            switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mActivePointerId = ev.getPointerId(actionIndex);

                mLastMotionX = ev.getX(actionIndex);
                mLastMotionY = ev.getY(actionIndex);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int pointerId = ev.getPointerId(actionIndex);
                if (pointerId == mActivePointerId) {
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newIndex);

                    mLastMotionX = ev.getX(newIndex);
                    mLastMotionY = ev.getY(newIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1)
                    break;

                final float x = ev.getX(activePointerIndex);
                final float y = (int) ev.getY(activePointerIndex);

                int deltaX = (int) (x - mLastMotionX);

                if (mCaptureItem != null && mCaptureItem.getTouchMode() == Mode.DRAG && mCaptureItem.isSwipeEnabled()) {
                    mLastMotionX = x;
                    mLastMotionY = y;

                    mCaptureItem.trackMotionScroll(deltaX);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mCaptureItem != null) {
                    Mode touchMode = mCaptureItem.getTouchMode();
                    if (touchMode == Mode.DRAG) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int xVel = (int) velocityTracker.getXVelocity(mActivePointerId);
                        mCaptureItem.fling(xVel);
                    }
                }
                cancel();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mCaptureItem != null)
                    mCaptureItem.revise();

                cancel();
                break;

            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        void cancel() {
            mDealByParent = false;
            mActivePointerId = -1;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }
    }

    static View findTopChildUnder(ViewGroup parent, int x, int y) {
        final int childCount = parent.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    public static void closeAllItems(RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child instanceof SwipeItemLayout) {
                SwipeItemLayout swipeItemLayout = (SwipeItemLayout) child;
                if (swipeItemLayout.isOpen())
                    swipeItemLayout.close();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE && isOpen()) {
            close();
        }
    }
}
