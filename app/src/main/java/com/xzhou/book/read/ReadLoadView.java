package com.xzhou.book.read;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppUtils;

public class ReadLoadView extends View {

    private Paint mProgressPaint;
    private Paint mProgressBackgroundPaint;
    private Paint mBackgroundPaint;

    private float mRadius;
    private RectF mArcBounds = new RectF();
    private RectF mBackgroundBounds = new RectF();

    private float mProgress = 0;
    private int mProgressColor;
    private int mProgressBackgroundColor;
    private int mBackgroundColor;
    private float mStrokeWidth;
    private float mBackgroundWidth;
    private float mMaxValue;

    public ReadLoadView(Context context) {
        this(context, null);
    }

    public ReadLoadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        mProgressColor = getResources().getColor(R.color.read_load_progress);
        mProgressBackgroundColor = getResources().getColor(R.color.read_load_bg_progress);
        mBackgroundColor = getResources().getColor(R.color.read_load_bg);
        mStrokeWidth = 4;
        mBackgroundWidth = 4;
        mMaxValue = 100;

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(AppUtils.dip2px(mStrokeWidth));
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        String pc = String.format("#%06X", (0xFFFFFF & mProgressColor));
        mProgressPaint.setColor(Color.parseColor(pc));

        mProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressBackgroundPaint.setStyle(Paint.Style.FILL);
        mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
        mProgressBackgroundPaint.setStyle(Paint.Style.STROKE);
        mProgressBackgroundPaint.setStrokeWidth(AppUtils.dip2px(mBackgroundWidth));
        mProgressBackgroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        String bc = String.format("#%06X", (0xFFFFFF & mProgressBackgroundColor));
        mProgressBackgroundPaint.setColor(Color.parseColor(bc));

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint.setColor(mBackgroundColor);
        String pbc = String.format("#%06X", (0xFFFFFF & mBackgroundColor));
        mBackgroundPaint.setColor(Color.parseColor(pbc));

        //paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w, h) / 2f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float mouthInset = mRadius / 2.5f;
        mArcBounds.set(mouthInset, mouthInset, mRadius * 2 - mouthInset, mRadius * 2 - mouthInset);

        mBackgroundBounds.set(0, 0, mRadius * 2, mRadius * 2);

        canvas.drawArc(mBackgroundBounds, 0f, 360f, false, mBackgroundPaint);
        canvas.drawArc(mArcBounds, 0f, 360f, false, mProgressBackgroundPaint);
        canvas.drawArc(mArcBounds, 270f, mProgress / getMaxValue() * 360, false, mProgressPaint);
    }

    public void setProgress(float f) {
        mProgress = f;
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public float getProgressPercentage() {
        return mProgress / getMaxValue() * 100;
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
        mProgressPaint.setColor(color);
        invalidate();
    }

    public void setProgressColor(String color) {
        mProgressPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundColor(String color) {
        mBackgroundPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void setProgressBackgroundColor(int color) {
        mProgressBackgroundColor = color;
        mProgressBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setProgressBackgroundColor(String color) {
        mProgressBackgroundPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(float max) {
        mMaxValue = max;
        invalidate();
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
        invalidate();
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setBackgroundWidth(float width) {
        mBackgroundWidth = width;
        invalidate();
    }

    public float getBackgroundWidth() {
        return mBackgroundWidth;
    }
}


