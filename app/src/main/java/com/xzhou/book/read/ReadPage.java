package com.xzhou.book.read;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.ThemeUtils;
import com.xzhou.book.widget.JustifyTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadPage extends RelativeLayout {
    private static final String TAG = "ReadPager";
    @BindView(R.id.chapter_title)
    TextView mChapterTitle;
    @BindView(R.id.chapter_content)
    JustifyTextView mChapterContent;
    @BindView(R.id.page_number)
    TextView mPageNumber;
    @BindView(R.id.battery_view)
    TextView mBatteryView;

    @BindView(R.id.read_page_error)
    LinearLayout mErrorView;
    @BindView(R.id.error_image)
    ImageView mErrorImage;
    @BindView(R.id.error_hint)
    TextView mErrorHint;
    @BindView(R.id.retry_btn)
    TextView mRetryBtn;

    private ReadLoadView mLoadingView;
    private @Constant.ReadTheme
    int mTheme;
    private PageContent mPageContent;
    private TextLayoutListener mListener;
    private OnReloadListener mOnReloadListener;
    private int mPreHeight;
    private ValueAnimator mLoadAnimator;

    public interface TextLayoutListener {
        void onLayout(boolean isFirst);
    }

    public interface OnReloadListener {
        void onReload();
    }

    public ReadPage(Context context) {
        this(context, null);
    }

    public ReadPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setTextLayoutListener(TextLayoutListener listener) {
        mListener = listener;
    }

    public void setOnReloadListener(OnReloadListener listener) {
        mOnReloadListener = listener;
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.read_pager, this);
        ButterKnife.bind(this, view);

        int fontSize = AppSettings.getFontSizeSp();
        mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        mChapterContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mChapterContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPreHeight = mChapterContent.getMeasuredHeight();
                if (mListener != null) {
                    mListener.onLayout(true);
                }
            }
        });

        mTheme = AppSettings.getReadTheme();
        setReadTheme(mTheme, AppSettings.isNight());
        initLoadingView(context);
        mRetryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnReloadListener != null) {
                    mOnReloadListener.onReload();
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mPreHeight != mChapterContent.getMeasuredHeight()) {
            mPreHeight = mChapterContent.getMeasuredHeight();
            if (mListener != null) {
                mListener.onLayout(false);
            }
        }
    }

    private void initLoadingView(Context context) {
        mLoadingView = new ReadLoadView(context);
    }

    public void setBattery(int battery) {
        mBatteryView.setText(String.valueOf(battery));
    }

    public void setReadTheme(int theme, boolean isNight) {
        int contentColor;
        int titleColor;
        int backgroundColor;
        int batteryRes;
        if (AppSettings.isNight()) {
            backgroundColor = AppUtils.getColor(R.color.read_theme_night);
            batteryRes = R.mipmap.reader_battery_bg_night;
            contentColor = AppUtils.getColor(R.color.chapter_content_night);
            titleColor = AppUtils.getColor(R.color.chapter_title_night);
        } else {
            backgroundColor = ThemeUtils.getThemeColor(theme);
            contentColor = AppUtils.getColor(R.color.chapter_content_day);
            titleColor = AppUtils.getColor(R.color.chapter_title_day);
            batteryRes = R.mipmap.reader_battery_bg_normal;
            switch (mTheme) {
            case Constant.ReadTheme.BROWN:
                batteryRes = R.mipmap.reader_battery_bg_brown;
                break;
            case Constant.ReadTheme.GREEN:
                batteryRes = R.mipmap.reader_battery_bg_green;
                break;
            }
        }
        mTheme = theme;
        mBatteryView.setBackgroundResource(batteryRes);
        setBackgroundColor(backgroundColor);
        mChapterTitle.setTextColor(titleColor);
        mChapterContent.setTextColor(contentColor);
    }

    public int getTheme() {
        return mTheme;
    }

    public void decFontSize() {
        int curSize = AppUtils.px2dip((int) mChapterContent.getTextSize());
        if (curSize >= 16) {
            int size = curSize - 2;
            mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            AppSettings.saveFontSizeSp(size);
        }
    }

    public void incFontSize() {
        int curSize = AppUtils.px2dip((int) mChapterContent.getTextSize());
        if (curSize <= 32) {
            int size = curSize + 2;
            mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            AppSettings.saveFontSizeSp(size);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setPageContent(PageContent page) {
        mPageContent = page;
        if (mPageContent == null) {
            reset();
            return;
        }
        if (page.isShow) {
            saveReadProgress();
        }
        setLoadState(page.isLoading);
        if (page.chapter >= 0) {
            mChapterTitle.setText((page.chapter + 1) + ". " + page.chapterTitle);
        }
        mPageNumber.setText(page.getCurPagePos());
        //mChapterContent.setText(page.getPageContent());
        mChapterContent.setLines(page.isLoading ? null : page.getLines());
        setErrorView(page.error != ReadPresenter.Error.NONE);
        switch (page.error) {
        case ReadPresenter.Error.CONNECTION_FAIL:
            mErrorImage.setImageResource(R.mipmap.ic_reader_connection_error);
            mErrorHint.setText(R.string.read_error_connect_fail);
            break;
        case ReadPresenter.Error.NO_CONTENT:
            mErrorImage.setImageResource(R.mipmap.ic_reader_error_no_content);
            mErrorHint.setText(R.string.read_error_no_content);
            break;
        case ReadPresenter.Error.NO_NETWORK:
            mErrorImage.setImageResource(R.mipmap.ic_reader_no_network);
            mErrorHint.setText(R.string.read_error_no_network);
            break;
        }
    }

    public void checkLoading() {
        if (mPageContent == null || mPageContent.mPageLines == null) {
            setLoadState(true);
        }
    }

    public boolean isPageEnd() {
        return mPageContent != null && mPageContent.isEnd;
    }

    public boolean isPageStart() {
        return mPageContent != null && mPageContent.isStart;
    }

    public PageContent getPageContent() {
        return mPageContent;
    }

    public void saveReadProgress() {
        if (mPageContent != null && mPageContent.mPageLines != null) {
//            Log.i(TAG, "saveReadProgress::" + mPageContent);
            AppSettings.saveReadProgress(mPageContent.bookId, mPageContent.chapter, mPageContent.mPageLines.startPos);
        }
    }

    public void reset() {
        mPageContent = null;
        mChapterTitle.setText("");
        mPageNumber.setText("");
        mChapterContent.setLines(null);
        setErrorView(false);
        setLoadState(false);
    }

    public void setLoadState(boolean isLoading) {
        if (isLoading) {
            mLoadingView.setProgress(0);
            if (indexOfChild(mLoadingView) == -1) {
                int radius = AppUtils.dip2px(45);
                LayoutParams lp = (LayoutParams) mLoadingView.getLayoutParams();
                if (lp == null) {
                    lp = new LayoutParams(radius, radius);
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                }
                addView(mLoadingView, lp);
                if (mLoadAnimator == null) {
                    mLoadAnimator = ValueAnimator.ofInt(0, 70);
                    mLoadAnimator.setRepeatCount(0);
                    mLoadAnimator.setDuration(5000);
                }
                mLoadAnimator.removeAllUpdateListeners();
                mLoadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        mLoadingView.setProgress(value);
                    }
                });
                mLoadAnimator.start();
            }
        } else {
            if (mLoadAnimator != null) {
                mLoadAnimator.removeAllUpdateListeners();
                mLoadAnimator.cancel();
            }
            int curProgress = (int) mLoadingView.getProgress();
            if (curProgress < 100) {
                ValueAnimator animator = ValueAnimator.ofInt(curProgress, 100);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        mLoadingView.setProgress(value);
                        if (value >= 100) {
                            removeView(mLoadingView);
                        }
                    }
                });
                animator.setRepeatCount(0);
                animator.setDuration((100 - curProgress) * 4);
                animator.start();
            } else {
                removeView(mLoadingView);
            }
        }
    }

    public void setErrorView(boolean visible) {
        if (visible) {
            setLoadState(false);
            mChapterContent.setLines(null);
            mErrorView.setVisibility(VISIBLE);
        } else {
            mErrorView.setVisibility(GONE);
        }
    }
}
