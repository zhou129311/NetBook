package com.xzhou.book.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;

public class BookListActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitle("");
    }
}
