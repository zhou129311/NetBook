package com.xzhou.book.read;

import android.animation.ValueAnimator;
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

    private Paint progressBarPaint;
    private Paint progressBackgroundPaint;
    private Paint backgroundPaint;

    private float mRadius;
    private RectF mArcBounds = new RectF();
    private RectF mBackgroundBounds = new RectF();

    float drawUpto = 0;

    private int progressColor;
    private int progressBackgroundColor;
    private int backgroundColor;
    private float strokeWidth;
    private float backgroundWidth;
    private float maxValue;
    private ValueAnimator mAnimator = ValueAnimator.ofInt(0, 100);

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
        progressColor = getResources().getColor(R.color.read_load_progress);
        progressBackgroundColor = getResources().getColor(R.color.read_load_bg_progress);
        backgroundColor = getResources().getColor(R.color.read_load_bg);
        strokeWidth = 4;
        backgroundWidth = 4;
        maxValue = 100;

        progressBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBarPaint.setStyle(Paint.Style.FILL);
        progressBarPaint.setColor(progressColor);
        progressBarPaint.setStyle(Paint.Style.STROKE);
        progressBarPaint.setStrokeWidth(AppUtils.dip2px(strokeWidth));
        progressBarPaint.setStrokeCap(Paint.Cap.ROUND);
        String pc = String.format("#%06X", (0xFFFFFF & progressColor));
        progressBarPaint.setColor(Color.parseColor(pc));

        progressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBackgroundPaint.setStyle(Paint.Style.FILL);
        progressBackgroundPaint.setColor(progressBackgroundColor);
        progressBackgroundPaint.setStyle(Paint.Style.STROKE);
        progressBackgroundPaint.setStrokeWidth(AppUtils.dip2px(backgroundWidth));
        progressBackgroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        String bc = String.format("#%06X", (0xFFFFFF & progressBackgroundColor));
        progressBackgroundPaint.setColor(Color.parseColor(bc));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundPaint.setColor(backgroundColor);
        String pbc = String.format("#%06X", (0xFFFFFF & backgroundColor));
        backgroundPaint.setColor(Color.parseColor(pbc));

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

        canvas.drawArc(mBackgroundBounds, 0f, 360f, false, backgroundPaint);
        canvas.drawArc(mArcBounds, 0f, 360f, false, progressBackgroundPaint);
        canvas.drawArc(mArcBounds, 270f, drawUpto / getMaxValue() * 360, false, progressBarPaint);
    }

    public void setProgress(float f) {
        drawUpto = f;
        invalidate();
    }

    public float getProgress() {
        return drawUpto;
    }

    public float getProgressPercentage() {
        return drawUpto / getMaxValue() * 100;
    }

    public void setProgressColor(int color) {
        progressColor = color;
        progressBarPaint.setColor(color);
        invalidate();
    }

    public void setProgressColor(String color) {
        progressBarPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundColor(String color) {
        backgroundPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void setProgressBackgroundColor(int color) {
        progressBackgroundColor = color;
        progressBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setProgressBackgroundColor(String color) {
        progressBackgroundPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float max) {
        maxValue = max;
        invalidate();
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        invalidate();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setBackgroundWidth(float width) {
        backgroundWidth = width;
        invalidate();
    }

    public float getBackgroundWidth() {
        return backgroundWidth;
    }
}


