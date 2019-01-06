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

public class ReadPager extends RelativeLayout {
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
    private boolean isPageEnd;
    //    private ReadActivity mActivity;
    private ReadPageListener mListener;

    public interface ReadPageListener {
        void onInit();

        void onRetryLoad();
    }

    public ReadPager(Context context) {
        this(context, null);
//        mActivity = (ReadActivity) context;
    }

    public ReadPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setReadPageListener(ReadPageListener listener) {
        mListener = listener;
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.read_pager, this);
        ButterKnife.bind(this, view);

        mChapterContent.setTextColor(context.getResources().getColor(R.color.common_h1));
        mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.getFontSize());
        mChapterContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mChapterContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mListener != null) {
                    mListener.onInit();
                }
            }
        });

        mTheme = AppSettings.getReadTheme();
        setReadTheme(mTheme);
        initLoadingView(context);
        mRetryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRetryLoad();
                }
            }
        });
    }

    private void initLoadingView(Context context) {
        if (mLoadingView == null) {
            mLoadingView = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.common_load_view, null);
            mLoadingView.setVisibility(VISIBLE);
        }
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

    public boolean setFontSize(int fontSizePx) {
        if (fontSizePx != (int) mChapterContent.getTextSize()) {
            mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx);
            AppSettings.saveFontSize(fontSizePx);
            return true;
        }
        return false;
    }

    public void setPageContent(PageContent pageContent, String title, String number, @ReadPresenter.Error int error) {
        isPageEnd = false;
        mPageContent = pageContent;
        reset();
        mChapterTitle.setText(title == null ? "" : title);
        mPageNumber.setText(number == null ? "" : number);
        if (pageContent != null) {
            mChapterContent.setText(mPageContent.getPageContent());
        } else {
            if (error == ReadPresenter.Error.END) {
                isPageEnd = true;
                return;
            }
            mChapterContent.setText("");
            switch (error) {
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
            setErrorView(true);
        }
    }

    public boolean isPageEnd() {
        return isPageEnd;
    }

    public PageContent getPageContent() {
        return mPageContent;
    }

    public void saveReadProgress() {
        if (mPageContent != null) {
            AppSettings.saveReadProgress(mPageContent.bookId, mPageContent.chapter, mPageContent.startPos, mPageContent.endPos);
        }
    }

    public void reset() {
        mChapterTitle.setText("");
        mChapterContent.setText("");
        mPageNumber.setText("");
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
            mErrorView.setVisibility(VISIBLE);
        } else {
            mErrorView.setVisibility(GONE);
        }
    }
}