package com.xzhou.book.read;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.Constant;
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

    private ProgressBar mLoadingView;
    private View mErrorView;

    public ReadPager(Context context) {
        this(context, null);
    }

    public ReadPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.read_pager, this);
        ButterKnife.bind(this, view);
        mChapterContent.setTextColor(context.getResources().getColor(R.color.common_h1));
        mChapterContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.getFontSize());
        int theme = AppSettings.getReadTheme();
        int batteryRes = R.mipmap.reader_battery_bg_normal;
        switch (theme) {
        case Constant.ReadTheme.BROWN:
            batteryRes = R.mipmap.reader_battery_bg_brown;
            break;
        case Constant.ReadTheme.GREEN:
            batteryRes = R.mipmap.reader_battery_bg_green;
            break;
        }
        mBatteryView.setBackgroundResource(batteryRes);
        mChapterContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mChapterContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
        initLoadingView(context);
        initErrorView(context);
    }

    public void reset() {
        mChapterContent.setText("");
    }

    private void initLoadingView(Context context) {
        if (mLoadingView == null) {
            mLoadingView = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.common_load_view, null);
            mLoadingView.setIndeterminateDrawable(context.getDrawable(R.drawable.progress_bar_read));
        }
    }

    private void initErrorView(Context context) {
        if (mErrorView == null) {
            mErrorView = LayoutInflater.from(context).inflate(R.layout.read_error_view, null);
        }
    }

    public void initData(String bookId) {

    }

    private void setLoadState(boolean isLoading) {
        if (isLoading) {
            if (indexOfChild(mLoadingView) == -1) {
                addView(mLoadingView);
            }
        } else {
            removeView(mLoadingView);
        }
    }

    private void setErrorView(boolean visible) {
        if (visible) {
            if (indexOfChild(mErrorView) == -1) {
                addView(mErrorView);
            }
        } else {
            removeView(mErrorView);
        }
    }

}
