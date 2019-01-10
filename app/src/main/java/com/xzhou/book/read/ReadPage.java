package com.xzhou.book.read;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
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

    private ProgressBar mLoadingView;
    private @Constant.ReadTheme
    int mTheme;
    private PageContent mPageContent;
    //    private ReadActivity mActivity;
    private TextLayoutListener mListener;
    private OnReloadListener mOnReloadListener;
    private int mPreHeight;

    public interface TextLayoutListener {
        void onLayout(boolean isFirst);
    }

    public interface OnReloadListener {
        void onReload();
    }

    public ReadPage(Context context) {
        this(context, null);
//        mActivity = (ReadActivity) context;
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

        mChapterContent.setTextColor(context.getResources().getColor(R.color.common_h1));
        int fontSize = AppSettings.getFontSize();
        mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        mChapterContent.setLineSpacing(0, 1.1f);
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
        setReadTheme(mTheme);
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
        mLoadingView = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.common_load_view, null);
        mLoadingView.setVisibility(VISIBLE);
    }

    public void setBattery(int battery) {
        mBatteryView.setText(String.valueOf(battery));
    }

    public void setReadTheme(int theme) {
        if (mTheme != theme) {
            Log.i(TAG, "setReadTheme:" + mTheme);
            mTheme = theme;
            int batteryRes = R.mipmap.reader_battery_bg_normal;
            switch (mTheme) {
            case Constant.ReadTheme.BROWN:
                batteryRes = R.mipmap.reader_battery_bg_brown;
                break;
            case Constant.ReadTheme.GREEN:
                batteryRes = R.mipmap.reader_battery_bg_green;
                break;
            }
            mBatteryView.setBackgroundResource(batteryRes);
            setBackgroundColor(AppUtils.getThemeColor(theme));
        }
    }

    public void decFontSize() {
        int curSize = (int) mChapterContent.getTextSize();
        if (curSize > AppUtils.dip2px(16)) {
            int size = curSize - 2;
            mChapterContent.setLineSpacing(size / 3, 1);
            mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            AppSettings.saveFontSize(size);
            if (mListener != null) {
                mListener.onLayout(false);
            }
        }
    }

    public void incFontSize() {
        int curSize = (int) mChapterContent.getTextSize();
        if (curSize < AppUtils.dip2px(28)) {
            int size = curSize + 2;
            mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            AppSettings.saveFontSize(size);
            if (mListener != null) {
                mListener.onLayout(false);
            }
        }
    }

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
        mChapterTitle.setText(page.chapterTitle);
        mPageNumber.setText(page.getCurPagePos());
        mChapterContent.setText(mPageContent.getPageContent());
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
            Log.i(TAG, "saveReadProgress::" + mPageContent);
            AppSettings.saveReadProgress(mPageContent.bookId, mPageContent.chapter, mPageContent.mPageLines.startPos);
        }
    }

    public void reset() {
        mPageContent = null;
        mChapterTitle.setText("");
        mPageNumber.setText("");
        mChapterContent.setText("");
        setErrorView(false);
        setLoadState(false);
    }

    public void setLoadState(boolean isLoading) {
        if (isLoading) {
            if (indexOfChild(mLoadingView) == -1) {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                addView(mLoadingView, lp);
            }
        } else {
            removeView(mLoadingView);
        }
    }

    public void setErrorView(boolean visible) {
        if (visible) {
            setLoadState(false);
            mChapterContent.setText("");
            mErrorView.setVisibility(VISIBLE);
        } else {
            mErrorView.setVisibility(GONE);
        }
    }
}
