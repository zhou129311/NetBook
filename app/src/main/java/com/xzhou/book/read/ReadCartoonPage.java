package com.xzhou.book.read;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.widget.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadCartoonPage extends RelativeLayout {

    @BindView(R.id.photo_view)
    PhotoView mPhotoView;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.page_number)
    TextView mPageNumber;
    @BindView(R.id.wifi_view)
    TextView mWifiView;
    @BindView(R.id.battery_view)
    TextView mBatteryView;
    @BindView(R.id.load_error_view)
    TextView mErrorView;

    private ReadLoadView mLoadingView;
    private ValueAnimator mLoadAnimator;
    private CartoonContent mContent;
    private OnReloadListener mReloadListener;

    public interface OnReloadListener {
        void onReload();
    }

    public ReadCartoonPage(Context context) {
        this(context, null);
    }

    public ReadCartoonPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setOnReloadListener(OnReloadListener listener) {
        mReloadListener = listener;
    }

    public CartoonContent getContent() {
        return mContent;
    }

    public void setOnClickChangePageListener(PhotoView.OnClickChangePageListener listener) {
        mPhotoView.setOnClickChangePageListener(listener);
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.read_cartoon_page, this);
        ButterKnife.bind(this, view);
        mLoadingView = new ReadLoadView(context);
        mPhotoView.setEnableScale(true);
        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReloadListener != null) {
                    mReloadListener.onReload();
                }
            }
        });
    }

    public void checkLoading() {
        if (mContent == null || mContent.bitmap == null) {
            setLoadState(true);
        }
    }

    public boolean isPageEnd() {
        return mContent != null && mContent.isEnd;
    }

    public boolean isPageStart() {
        return mContent != null && mContent.isStart;
    }

    public void reset() {
        mContent = null;
        mPageTitle.setText("");
        mPageNumber.setText("");
        mPhotoView.setVisibility(GONE);
        setLoadState(false);
        setErrorView(false);
    }

    public void setPageContent(CartoonContent content) {
        mContent = content;
        if (mContent == null) {
            reset();
            return;
        }
        setLoadState(content.isLoading);
        setErrorView(content.error != ReadPresenter.Error.NONE);
        if (content.bitmap != null && !content.bitmap.isRecycled()) {
            mPhotoView.setImageBitmap(content.bitmap);
        }
        mPhotoView.setMaxScale(content.maxScale);
        mPageTitle.setText(content.title);
        mPageNumber.setText((content.curPage + 1) + "/" + content.totalPage);
        if (content.isShow) {
            AppSettings.saveReadProgress(content.bookId, content.chapter, content.curPage);
        }
    }

    public void setBattery(int curBattery) {
        mBatteryView.setText(String.valueOf(curBattery));
    }

    public void updateWiFiState(boolean state) {
        mWifiView.setVisibility(state ? VISIBLE : GONE);
    }

    public void setErrorView(boolean visible) {
        if (visible) {
            setLoadState(false);
            mPhotoView.setVisibility(GONE);
            mErrorView.setVisibility(VISIBLE);
        } else {
            mErrorView.setVisibility(GONE);
        }
    }

    public void setLoadState(boolean isLoading) {
        if (isLoading) {
            mPhotoView.setVisibility(GONE);
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
                } else {
                    mLoadAnimator.removeAllUpdateListeners();
                    mLoadAnimator.cancel();
                }
                mLoadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        mLoadingView.setProgress(value);
                        if (value == 70) {
                            mLoadAnimator.cancel();
                            ValueAnimator animator = ValueAnimator.ofInt(71, 85);
                            animator.addUpdateListener(this);
                            animator.setRepeatCount(0);
                            animator.setDuration(5000);
                            animator.start();
                        }
                    }
                });
                mLoadAnimator.start();
            }
        } else {
            mPhotoView.setVisibility(VISIBLE);
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
}
