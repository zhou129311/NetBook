package com.xzhou.book.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.ui.view.RVPIndicator;
import com.xzhou.book.utils.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class TabActivity extends BaseActivity {

    private static final String EXTRA_DATA = "extra_args";

    @BindView(R.id.indicator)
    RVPIndicator mIndicator;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private Entities.TabData mTabData;
    private List<String> mTabNames;
    private List<TabFragment> mFragments = new ArrayList<>();
    private List

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
    protected void initToolBar() {
        super.initToolBar();
        mTabData = getIntent().getParcelableExtra(EXTRA_DATA);
        mToolbar.setTitle(mTabData.title);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
    }

    private void initChildFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof TabFragment) {

                }
            }
        }
        switch (mTabData.source) {
        case Constant.TabSource.SOURCE_CATEGORY_SUB:
            initCategorySubData();
            break;
        case Constant.TabSource.SOURCE_RANK_SUB:

            break;
        case Constant.TabSource.SOURCE_TOPIC:

            break;
        }

        final int size = mFragments.size();
        if (size < 1) {
            return;
        }

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return size;
            }

            @Override
            public TabFragment getItem(int position) {
                return mFragments.get(position);
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(size - 1);
        if (size == 1) {
            mIndicator.setVisibility(View.GONE);
        } else {
            mIndicator.setTabItemTitles(mTabNames);
            mIndicator.setViewPager(mViewPager, 0);
        }
    }

    private void initCategorySubData() {
        mTabNames = Arrays.asList(getResources().getStringArray(R.array.category_sub_tabs));
        for (int i = 0; i < mTabNames.size(); i++) {

        }
    }
}
