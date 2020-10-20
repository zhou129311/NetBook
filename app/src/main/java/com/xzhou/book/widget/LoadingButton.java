package com.xzhou.book.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.xzhou.book.R;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-6-4
 * Change List:
 */
public class LoadingButton extends LinearLayout {
    private ProgressBar mLoadingView;
    private TextView mTextView;

    private String mLoadingText;
    private String mNormalText;

    public LoadingButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.loading_button, this);
        @SuppressLint("CustomViewStyleable") TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingButtonStyle);
        mLoadingText = tArray.getString(R.styleable.LoadingButtonStyle_loading_text);
        mNormalText = tArray.getString(R.styleable.LoadingButtonStyle_normal_text);
        tArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLoadingView = findViewById(R.id.loading);
        mTextView = findViewById(R.id.text);
        setLoading(false);
    }

    public void initText(@StringRes int loading, @StringRes int normal) {
        initText(getContext().getString(loading), getContext().getString(normal));
    }

    public void initText(String loading, String normal) {
        mLoadingText = loading;
        mNormalText = normal;
    }

    public void setLoading(boolean loading) {
        if (loading) {
            setEnabled(false);
            mLoadingView.setVisibility(VISIBLE);
            mTextView.setText(mLoadingText);
        } else {
            setEnabled(true);
            mLoadingView.setVisibility(GONE);
            mTextView.setText(mNormalText);
        }
    }
}
