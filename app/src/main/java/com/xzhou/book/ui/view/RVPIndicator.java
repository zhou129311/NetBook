package com.xzhou.book.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xzhou.book.R;

import java.util.List;

/**
 * Created by Administrator on 2016/8/4.
 */
public class RVPIndicator extends LinearLayout {

    private static final int STYLE_BITMAP = 0;
    private static final int STYLE_LINE = 1;
    private static final int STYLE_SQUARE = 2;
    private static final int STYLE_TRIANGLE = 3;

    private static final int D_TAB_COUNT = 3;
    private static final int D_TEXT_COLOR_NORMAL = 0x000000;
    private static final int D_TEXT_COLOR_HIGHLIGHT = 0xFF0000;
    private static final int D_INDICATOR_COLOR = 0xF29B76;

    private List<String> mTabTitles;

    private int mTabVisibleCount = D_TAB_COUNT;

    public ViewPager mViewPager;

    private int mTextSize = 16;
    private int mTextColorNormal = D_TEXT_COLOR_NORMAL;
    private int mTextColorHighlight = D_TEXT_COLOR_HIGHLIGHT;
    private int mIndicatorColor = D_INDICATOR_COLOR;

    private Paint mPaint;
    private Rect mRectF;
    private Bitmap mBitmap;
    private int mIndicatorHeight = 5;
    private int mIndicatorWidth = getWidth() / mTabVisibleCount;

    private static final float RADIO_TRIANGEL = 1.0f / 6;
    private float mTranslationX;
    private int mIndicatorStyle = STYLE_LINE;
    private Path mPath;
    private int mPosition = 0;

    public RVPIndicator(Context context) {
        this(context, null);
    }

    public RVPIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RVPIndicator);
        mTabVisibleCount = a.getInt(R.styleable.RVPIndicator_item_count, D_TAB_COUNT);
        mTextColorNormal = a.getColor(R.styleable.RVPIndicator_text_color_normal, D_TEXT_COLOR_NORMAL);
        mTextColorHighlight = a.getColor(R.styleable.RVPIndicator_text_color_hightlight, D_TEXT_COLOR_HIGHLIGHT);
        mTextSize = a.getDimensionPixelSize(R.styleable.RVPIndicator_text_size, 16);
        mIndicatorColor = a.getColor(R.styleable.RVPIndicator_indicator_color, D_INDICATOR_COLOR);
        mIndicatorStyle = a.getInt(R.styleable.RVPIndicator_indicator_style, STYLE_LINE);

        Drawable drawable = a.getDrawable(R.styleable.RVPIndicator_indicator_src);

        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                mBitmap = ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof NinePatchDrawable) {
                // .9图处理
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                mBitmap = bitmap;
            }
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.heart_love);
        }
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mIndicatorColor);
        mPaint.setStyle(Style.FILL);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        switch (mIndicatorStyle) {
        case STYLE_LINE:
            /*
             * 下划线指示器:宽与item相等,高是item的1/10
             */
            mIndicatorWidth = w / mTabVisibleCount;
            mIndicatorHeight = h / 10;
            mTranslationX = 0;
            mRectF = new Rect(0, 0, mIndicatorWidth, mIndicatorHeight);

            break;
        case STYLE_SQUARE:
        case STYLE_BITMAP:
            /*
             * 方形/图标指示器:宽,高与item相等
             */
            mIndicatorWidth = w / mTabVisibleCount;
            mIndicatorHeight = h;
            mTranslationX = 0;
            mRectF = new Rect(0, 0, mIndicatorWidth, mIndicatorHeight);
            break;
        case STYLE_TRIANGLE:
            /*
             * 三角形指示器:宽与item(1/4)相等,高是item的1/4
             */
            //mIndicatorWidth = w / mTabVisibleCount / 4;
            // mIndicatorHeight = h / 4;
            mIndicatorWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGEL);// 1/6 of  width  ;
            mIndicatorHeight = (int) (mIndicatorWidth / 2 / Math.sqrt(2));
            mTranslationX = 0;
            break;
        }
        initTabItem();
        setHighLightTextView(mPosition);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 保存画布
        canvas.save();

        switch (mIndicatorStyle) {
        case STYLE_BITMAP:
            canvas.translate(mTranslationX, 0);
            canvas.drawBitmap(mBitmap, null, mRectF, mPaint);
            break;
        case STYLE_LINE:
            canvas.translate(mTranslationX, getHeight() - mIndicatorHeight);
            canvas.drawRect(mRectF, mPaint);
            break;
        case STYLE_SQUARE:
            canvas.translate(mTranslationX, 0);
            canvas.drawRect(mRectF, mPaint);
            break;
        case STYLE_TRIANGLE:
            canvas.translate(mTranslationX, 0);
            // 笔锋圆滑度
            // mPaint.setPathEffect(new CornerPathEffect(10));
            mPath = new Path();
            int midOfTab = getWidth() / mTabVisibleCount / 2;
            mPath.moveTo(midOfTab, getHeight() - mIndicatorHeight);
            mPath.lineTo(midOfTab - mIndicatorWidth / 2, getHeight());
            mPath.lineTo(midOfTab + mIndicatorWidth / 2, getHeight());
            mPath.close();
            canvas.drawPath(mPath, mPaint);
            break;
        }
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    public void setTabItemTitles(List<String> datas) {
        this.mTabTitles = datas;
    }

    public void setViewPager(ViewPager viewPager, int pos) {
        this.mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                setHighLightTextView(position);

                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onScroll(position, positionOffset);

                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });

        mViewPager.setCurrentItem(pos);
        mPosition = pos;
    }

    private void initTabItem() {
        if (mTabTitles != null && mTabTitles.size() > 0) {
            if (this.getChildCount() != 0) {
                this.removeAllViews();
            }
            for (String title : mTabTitles) {
                addView(createTextView(title));
            }
            setItemClickEvent();
        }
    }

    private void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

    private void setHighLightTextView(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                if (i == position) {
                    ((TextView) view).setTextColor(mTextColorHighlight);
                } else {
                    ((TextView) view).setTextColor(mTextColorNormal);
                }
            }
        }
    }

    private void onScroll(int position, float offset) {

        // 不断改变偏移量，invalidate
        mTranslationX = getWidth() / mTabVisibleCount * (position + offset);

        int tabWidth = getWidth() / mTabVisibleCount;

        // 容器滚动，当移动到倒数第二个的时候，开始滚动
        if (offset > 0 && position >= (mTabVisibleCount - 2)
                && getChildCount() > mTabVisibleCount
                && position < (getChildCount() - 2)) {
            if (mTabVisibleCount != 1) {

                int xValue = (position - (mTabVisibleCount - 2)) * tabWidth
                        + (int) (tabWidth * offset);
                this.scrollTo(xValue, 0);
            } else {
                // 为count为1时 的特殊处理
                this.scrollTo(position * tabWidth + (int) (tabWidth * offset),
                        0);
            }
        }

        invalidate();
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.width = getWidth() / mTabVisibleCount;
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(mTextColorNormal);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
        tv.setLayoutParams(lp);
        return tv;
    }

    public interface PageChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

    private PageChangeListener onPageChangeListener;

    public void setOnPageChangeListener(PageChangeListener pageChangeListener) {
        this.onPageChangeListener = pageChangeListener;
    }
}
