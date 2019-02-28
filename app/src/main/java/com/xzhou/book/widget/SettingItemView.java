package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
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
    @BindView(R.id.arrow)
    ImageView mArrowView;

    private static final int ARROW_VISIBLE = 0;
    private static final int ARROW_INVISIBLE = 1;
    private static final int ARROW_GONE = 2;

    private String mTitle;
    private String mValue;
    private int mVisibleState;

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);
        mTitle = ta.getString(R.styleable.SettingItemView_titleText);
        mValue = ta.getString(R.styleable.SettingItemView_valueText);
        mVisibleState = ta.getInt(R.styleable.SettingItemView_arrowVisibility, ARROW_VISIBLE);
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
        switch (mVisibleState) {
        case ARROW_VISIBLE:
            mArrowView.setVisibility(VISIBLE);
            break;
        case ARROW_INVISIBLE:
            mArrowView.setVisibility(INVISIBLE);
            break;
        case ARROW_GONE:
            mArrowView.setVisibility(GONE);
            break;
        }
    }

    public void setValue(String value) {
        mValue = value;
        if (mValue != null) {
            mSettingValueTv.setText(mValue);
        }
    }

    public void setArrowShow(boolean isShow) {
        mArrowView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }
}
