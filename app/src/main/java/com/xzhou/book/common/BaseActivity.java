package com.xzhou.book.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;

import java.lang.reflect.Method;

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
        mToolbar = findViewById(R.id.common_toolbar);
        if (mToolbar != null) {
            initToolBar();
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        mPresenter = createPresenter();
    }

    @Override
    public void setContentView(android.view.View view) {
        super.setContentView(view);
        mUnbinder = ButterKnife.bind(this);
        mToolbar = findViewById(R.id.common_toolbar);
        if (mToolbar != null) {
            initToolBar();
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        mPresenter = createPresenter();
    }

    @Override
    public void setContentView(android.view.View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mUnbinder = ButterKnife.bind(this);
        mToolbar = findViewById(R.id.common_toolbar);
        if (mToolbar != null) {
            initToolBar();
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        mPresenter = createPresenter();
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    @SuppressLint("PrivateApi") Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception ignored) {
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    protected void initToolBar() {
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitleTextAppearance(this, R.style.CommonTitleTextStyle);
    }

    public static class MyLinearLayoutManager extends LinearLayoutManager {

        private boolean mIsFixed = false;

        public MyLinearLayoutManager(Context context) {
            this(context, false);
        }

        public MyLinearLayoutManager(Context context, boolean fixed) {
            super(context);
            mIsFixed = fixed;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception ignored) {
            }
        }

        @Override
        public boolean canScrollVertically() {
            if (mIsFixed) {
                return false;
            }
            return super.canScrollVertically();
        }

        @Override
        public boolean canScrollHorizontally() {
            if (mIsFixed) {
                return false;
            }
            return super.canScrollHorizontally();
        }
    }

    public static class MyGridLayoutManager extends GridLayoutManager {
        private boolean mIsFixed = false;

        public MyGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public MyGridLayoutManager(Context context, int spanCount, boolean fixed) {
            super(context, spanCount);
            mIsFixed = fixed;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception ignored) {
            }
        }

        @Override
        public boolean canScrollVertically() {
            if (mIsFixed) {
                return false;
            }
            return super.canScrollVertically();
        }

        @Override
        public boolean canScrollHorizontally() {
            if (mIsFixed) {
                return false;
            }
            return super.canScrollHorizontally();
        }
    }
}
