package com.xzhou.book.common;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity<P extends BaseContract.Presenter> extends AppCompatActivity {
    protected Activity mActivity;
    protected MyApp mApp;
    protected Toolbar mToolbar;
    private Unbinder mUnbinder;
    protected P mPresenter;

    protected P createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (MyApp) getApplication();
        mActivity = this;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mUnbinder = ButterKnife.bind(this);
        configToolBar();
        mPresenter = createPresenter();
    }

    @Override
    public void setContentView(android.view.View view) {
        super.setContentView(view);
        mUnbinder = ButterKnife.bind(this);
        configToolBar();
        mPresenter = createPresenter();
    }

    @Override
    public void setContentView(android.view.View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mUnbinder = ButterKnife.bind(this);
        configToolBar();
        mPresenter = createPresenter();
    }

    private void configToolBar() {
        mToolbar = findViewById(R.id.common_toolbar);
        if (mToolbar != null) {
            initToolBar();
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!onNavBackClick()) {
                        onBackPressed();
                    }
                }
            });
        }
    }

    protected boolean onNavBackClick() {
        return false;
    }

    protected View getContentView() {
        return this.findViewById(android.R.id.content);
    }

    @Override
    public void finish() {
        super.finish();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    protected void initToolBar() {
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitleTextAppearance(this, R.style.CommonTitleTextStyle);
    }

    private long clickTime;

    public boolean doubleClick() {
        if (SystemClock.uptimeMillis() - clickTime < 500) {
            return true;
        }
        clickTime = SystemClock.uptimeMillis();
        return false;
    }
}
