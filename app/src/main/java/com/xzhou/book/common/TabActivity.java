package com.xzhou.book.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.widget.RVPIndicator;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class TabActivity extends BaseActivity {
    private static final String TAG = "TabActivity";
    private static final String EXTRA_DATA = "extra_args";

    @BindView(R.id.indicator)
    RVPIndicator mIndicator;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private Entities.TabData mTabData;
    private List<String> mTabNames;
    private final SparseArrayCompat<TabFragment> mFragments = new SparseArrayCompat<>();
    private final SparseArrayCompat<TabContract.Presenter> mPresenterList = new SparseArrayCompat<>();

    public static void startActivity(Activity activity, Entities.TabData data) {
        if (data == null) {
            throw new NullPointerException("title or args do not be null");
        }
        Intent intent = new Intent(activity, TabActivity.class);
        intent.putExtra(EXTRA_DATA, data);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);
        initChildFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_DATA, mTabData);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTabData = savedInstanceState.getParcelable(EXTRA_DATA);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        if (mTabData == null) {
            mTabData = getIntent().getParcelableExtra(EXTRA_DATA);
        }
        if (mTabData == null) {
            return;
        }

        Log.i(TAG, "mTabData = " + mTabData.toString());
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setTitle(mTabData.title);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
    }

    private void initChildFragment() {
        if (mTabData == null) {
            finish();
            return;
        }

        switch (mTabData.source) {
        case Constant.TabSource.SOURCE_CATEGORY_SUB:
            mTabNames = Arrays.asList(getResources().getStringArray(R.array.category_sub_tabs));
            break;
        case Constant.TabSource.SOURCE_RANK_SUB:
            if (AppUtils.isEmpty(mTabData.monthRankId) || AppUtils.isEmpty(mTabData.totalRankId)) {
                mTabNames = new ArrayList<>();
                mTabNames.add("");
            } else {
                mTabNames = Arrays.asList(getResources().getStringArray(R.array.sub_rank_tabs));
            }
            break;
        case Constant.TabSource.SOURCE_TOPIC:
            mTabNames = Arrays.asList(getResources().getStringArray(R.array.topic_tabs));
            break;
        }

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (int i = 0; i < fragmentList.size(); i++) {
                Fragment fragment = fragmentList.get(i);
                if (fragment instanceof TabFragment) {
                    TabFragment f = (TabFragment) fragment;
                    mFragments.put(f.getPosition(), f);
                    mPresenterList.put(f.getPosition(), new TabPresenter(f, mTabData, f.getPosition()));
                }
            }
        }
        for (int i = 0; i < mTabNames.size(); i++) {
            TabFragment fragment = mFragments.get(i);
            if (fragment == null) {
                fragment = TabFragment.newInstance(mTabData, i);
                mFragments.put(i, fragment);
                mPresenterList.put(i, new TabPresenter(fragment, mTabData, i));
            }
        }

        initViewPage();
    }

    private void initViewPage() {
        if (mTabNames.size() != mFragments.size()) {
            throw new IllegalStateException("mTabNames.size() must be equals mFragments.size()");
        }

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public TabFragment getItem(int position) {
                return mFragments.get(position);
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(mFragments.size() - 1);
        if (mFragments.size() == 1) {
            mIndicator.setVisibility(View.GONE);
        } else {
            mIndicator.setTabItemTitles(mTabNames);
            mIndicator.setViewPager(mViewPager, 0);
        }
    }
}
