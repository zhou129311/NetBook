package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingItemView extends RelativeLayout {

    @BindView(R.id.setting_title)
    TextView mSettingTitle;
    @BindView(R.id.setting_value_tv)
    TextView mSettingValueTv;
//    @BindView(R.id.setting_loading)
//    ProgressBar mProgressBar;

    private String mTitle;
    private String mValue;

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);
        mTitle = ta.getString(R.styleable.SettingItemView_titleText);
        mValue = ta.getString(R.styleable.SettingItemView_valueText);
        ta.recycle();
        initView(context);
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.settings_item_view, this);
        ButterKnife.bind(this, view);
        if (mTitle != null) {
            mSettingTitle.setText(mTitle);
        }
        if (mValue != null) {
            mSettingValueTv.setText(mValue);
        }
    }

    public void setValue(String value) {
        mValue = value;
        if (mValue != null) {
            mSettingValueTv.setText(mValue);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        mProgressBar.setVisibility(enabled ? GONE : VISIBLE);
//        mSettingValueTv.setVisibility(enabled ? VISIBLE : GONE);
    }
}
