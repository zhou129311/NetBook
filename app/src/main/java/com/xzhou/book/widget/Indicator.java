package com.xzhou.book.widget;

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
import com.xzhou.book.utils.AppUtils;

import java.util.List;

public class Indicator extends LinearLayout {

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

    private float mTextSize = 15;
    private int mTextColorNormal = D_TEXT_COLOR_NORMAL;
    private int mTextColorHighlight = D_TEXT_COLOR_HIGHLIGHT;
    private int mIndicatorColor = D_INDICATOR_COLOR;

    private Paint mPaint;
    private Rect mRectF;
    private Bitmap mBitmap;
    private int mIndicatorHeight = 5;
    private int mIndicatorWidth;

    private float mTranslationX;
    private int mIndicatorStyle = STYLE_LINE;
    private Path mPath;
    private int mPosition = 0;

    public Indicator(Context context) {
        this(context, null);
    }

    public Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Indicator);
        mTabVisibleCount = a.getInt(R.styleable.Indicator_item_count, D_TAB_COUNT);
        mTextColorNormal = a.getColor(R.styleable.Indicator_text_color_normal, D_TEXT_COLOR_NORMAL);
        mTextColorHighlight = a.getColor(R.styleable.Indicator_text_color_hightlight, D_TEXT_COLOR_HIGHLIGHT);
        mTextSize = a.getDimension(R.styleable.Indicator_text_size, 15);
        mIndicatorColor = a.getColor(R.styleable.Indicator_indicator_color, D_INDICATOR_COLOR);
        mIndicatorStyle = a.getInt(R.styleable.Indicator_indicator_style, STYLE_LINE);
        Drawable drawable = a.getDrawable(R.styleable.Indicator_indicator_src);

        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                mBitmap = ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof NinePatchDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                mBitmap = bitmap;
            }
        } else {
            if (mIndicatorStyle == STYLE_BITMAP) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg_tab_widget_v3);
            }
        }
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mIndicatorColor);
        mPaint.setStyle(Style.FILL);

        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        switch (mIndicatorStyle) {
        case STYLE_LINE:
            mIndicatorWidth = w / mTabVisibleCount;
            mIndicatorHeight = h / 10;
            mTranslationX = 0;
            mRectF = new Rect(0, 0, mIndicatorWidth, mIndicatorHeight);
            break;
        case STYLE_SQUARE:
        case STYLE_BITMAP:
            mIndicatorWidth = w / mTabVisibleCount;
            mIndicatorHeight = h;
            mTranslationX = 0;
            mRectF = new Rect(0, 0, mIndicatorWidth, mIndicatorHeight);
            break;
        case STYLE_TRIANGLE:
            mIndicatorWidth = w / 25;
            mIndicatorHeight = (int) (mIndicatorWidth / 2 / Math.sqrt(2));
            mTranslationX = 0;
            break;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
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
            mPath.reset();
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
        mTabVisibleCount = datas.size();
        initTabItem();
        setHighLightTextView(mPosition);
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
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(AppUtils.getScreenWidth() / mTabVisibleCount, LayoutParams.MATCH_PARENT);
                TextView view = createTextView(title);
                addView(view, lp);
            }
            setItemClickEvent();
        }
        invalidate();
        requestLayout();
    }

    private void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(j, true);
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
                this.scrollTo(position * tabWidth + (int) (tabWidth * offset), 0);
            }
        }

        invalidate();
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(mTextColorNormal);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
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
