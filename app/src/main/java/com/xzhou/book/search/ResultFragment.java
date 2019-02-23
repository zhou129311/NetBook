package com.xzhou.book.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.TabContract;
import com.xzhou.book.common.TabFragment;
import com.xzhou.book.common.TabPresenter;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.widget.Indicator;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_KEY;

public class ResultFragment extends BaseFragment {
    private static final String TAG = "ResultFragment";
    @BindView(R.id.search_indicator)
    Indicator mIndicator;
    @BindView(R.id.search_rel_view_pager)
    ViewPager mViewPager;

    private String mKey;
    private Entities.TabData mTabData;
    private final SparseArrayCompat<TabFragment> mFragments = new SparseArrayCompat<>();
    private final SparseArrayCompat<TabContract.Presenter> mPresenterList = new SparseArrayCompat<>();

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_search_result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && TextUtils.isEmpty(mKey)) {
            mKey = bundle.getString(EXTRA_SEARCH_KEY, "");
        }
        mTabData = new Entities.TabData();
        mTabData.source = Constant.TabSource.SOURCE_SEARCH;
        mTabData.params = new String[] { mKey };
        initChildFragment();
    }

    public void search(String key) {
        mKey = key;
        if (!isAdded()) {
            return;
        }
        mTabData.params[0] = mKey;
        if (!TextUtils.isEmpty(key)) {
            mPresenterList.get(mViewPager.getCurrentItem()).refresh();
            for (int i = 0, size = mPresenterList.size(); i < size; i++) {
                TabContract.Presenter presenter = mPresenterList.valueAt(i);
                if (presenter != null) {
                    presenter.setNeedRefresh(true);
                }
            }
        } else {
            Log.e(TAG, "oldKey = " + mKey + ",newKey = " + key);
        }
    }

    public int getCurTabId() {
        return mViewPager.getCurrentItem();
    }

    private void initChildFragment() {
        List<String> tabNames = Arrays.asList(getResources().getStringArray(R.array.search_result_tabs));

        List<Fragment> fragmentList = getChildFragmentManager().getFragments();
        if (fragmentList != null) {
            for (int i = 0, size = fragmentList.size(); i < size; i++) {
                Fragment fragment = fragmentList.get(i);
                if (fragment instanceof TabFragment) {
                    TabFragment f = (TabFragment) fragment;
                    mFragments.put(f.getTabId(), f);
                    mPresenterList.put(f.getTabId(), new TabPresenter(f, mTabData, f.getTabId()));
                }
            }
        }
        for (int i = 0, size = tabNames.size(); i < size; i++) {
            TabFragment fragment = mFragments.get(i);
            if (fragment == null) {
                fragment = TabFragment.newInstance(mTabData, i);
                mFragments.put(i, fragment);
                mPresenterList.put(i, new TabPresenter(fragment, mTabData, i));
            }
        }

        initViewPage(tabNames);
    }

    private void initViewPage(List<String> tabNames) {
        if (tabNames.size() != mFragments.size()) {
            throw new IllegalStateException("mTabNames.size() must be equals mFragments.size()");
        }

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
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
        mIndicator.setTabItemTitles(tabNames);
        mIndicator.setViewPager(mViewPager, 0);
    }
}
