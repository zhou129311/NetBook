package com.xzhou.book.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ListPopupWindow;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant.TabSource;
import com.xzhou.book.utils.Log;
import com.xzhou.book.widget.Indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class TabActivity extends BaseActivity {
    private static final String TAG = "TabActivity";
    private static final String EXTRA_DATA = "extra_args";
    public static final String EXTRA_TAB_ID = "extra_tabId";

    @BindView(R.id.indicator)
    Indicator mIndicator;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private final SparseArrayCompat<TabFragment> mFragments = new SparseArrayCompat<>();
    private final SparseArrayCompat<TabContract.Presenter> mPresenterList = new SparseArrayCompat<>();
    private Entities.TabData mTabData;
    private List<String> mTabNames;
    private ListPopupWindow mListPopupWindow;
    private SparseArrayCompat<FiltrateAdapter> mFiltrateAdapterList = new SparseArrayCompat<>();

    public static void startActivity(Context activity, Entities.TabData data) {
        startActivity(activity, data, 0);
    }

    public static void startActivity(Context activity, Entities.TabData data, int tabId) {
        if (data == null) {
            throw new NullPointerException("data cannot be null");
        }
        Intent intent = new Intent(activity, TabActivity.class);
        intent.putExtra(EXTRA_DATA, data);
        intent.putExtra(EXTRA_TAB_ID, tabId);
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
        if (mTabData == null) {
            mTabData = getIntent().getParcelableExtra(EXTRA_DATA);
        }
        if (mTabData == null) {
            return;
        }
        Log.i(TAG, "mTabData = " + mTabData.toString());
        mToolbar.setTitle(mTabData.title);
    }

    public int getCurTabId() {
        return getIntent().getIntExtra(EXTRA_TAB_ID, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTabData.filtrate != null && mTabData.filtrate.length > 0) {
            if (mTabData.source == TabSource.SOURCE_TOPIC_LIST) {
                getMenuInflater().inflate(R.menu.menu_topic, menu);
            } else if (mTabData.source == TabSource.SOURCE_COMMUNITY) {
                getMenuInflater().inflate(R.menu.menu_post_sort, menu);
            } else {
                getMenuInflater().inflate(R.menu.menu_filtrate, menu);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_filtrate:
        case R.id.menu_post_sort:
            showPopupWindow(item);
            return true;
        case R.id.menu_create_list:

            return true;
        case R.id.menu_user_list:

            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void initChildFragment() {
        if (mTabData == null) {
            finish();
            return;
        }
        switch (mTabData.source) {
        case TabSource.SOURCE_CATEGORY_SUB:
            mTabNames = Arrays.asList(getResources().getStringArray(R.array.category_sub_tabs));
            break;
        case TabSource.SOURCE_RANK_SUB:
            if (!AppUtils.isEmpty(mTabData.params[1]) && !AppUtils.isEmpty(mTabData.params[2])) {
                mTabNames = Arrays.asList(getResources().getStringArray(R.array.sub_rank_tabs));
            }
            break;
        case TabSource.SOURCE_TOPIC_LIST:
            mTabNames = Arrays.asList(getResources().getStringArray(R.array.topic_tabs));
            break;
        case TabSource.SOURCE_COMMUNITY:
            mTabNames = Arrays.asList(getResources().getStringArray(R.array.community_tabs));
            break;
        }
        if (mTabNames == null) {
            mTabNames = new ArrayList<>();
            mTabNames.add("");
        }

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
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
        for (int i = 0, size = mTabNames.size(); i < size; i++) {
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
            mIndicator.setViewPager(mViewPager, getCurTabId());
        }
    }

    private void showPopupWindow(final MenuItem item) {
        if (mTabData.filtrate == null || mTabData.filtrate.length < 1) {
            return;
        }
        item.setTitle(R.string.pack_up);
        final int curTabId = hasAllTabFiltrate() ? 0 : mViewPager.getCurrentItem();
        FiltrateAdapter adapter = mFiltrateAdapterList.get(curTabId);
        if (adapter == null) {
            final List<String> list = new ArrayList<>();
            if (mTabData.source == TabSource.SOURCE_TOPIC_LIST) {
                list.add(getString(R.string.all_topic));
                list.add(getString(R.string.male_topic));
                list.add(getString(R.string.female_topic));
            } else {
                list.addAll(Arrays.asList(mTabData.filtrate));
            }

            adapter = new FiltrateAdapter(mActivity, list);
            if (mTabData.source == TabSource.SOURCE_COMMUNITY) {
                adapter.setChecked(1);
            } else {
                adapter.setMarginLeft(10);
            }
            mFiltrateAdapterList.put(curTabId, adapter);
        }
        if (mListPopupWindow == null) {
            mListPopupWindow = new ListPopupWindow(mActivity);
            mListPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mListPopupWindow.setAnchorView(mToolbar);
            //mListPopupWindow.setVerticalOffset(-mToolbar.getHeight());
            mListPopupWindow.setModal(true);
            mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    item.setTitle(R.string.filtrate);
                }
            });
        }
        mListPopupWindow.setAdapter(adapter);
        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FiltrateAdapter fa = mFiltrateAdapterList.get(curTabId);
                fa.setChecked(position);
                if (mTabData.source != TabSource.SOURCE_COMMUNITY) {
                    mToolbar.setTitle(fa.getItem(position));
                }
                mListPopupWindow.dismiss();
                String filtrate = mTabData.filtrate[position];
                setFiltrate(filtrate, curTabId);
            }
        });
        mListPopupWindow.show();
    }

    private void setFiltrate(String filtrate, int curTabId) {
        if (hasAllTabFiltrate()) {
            for (int i = 0, size = mPresenterList.size(); i < size; i++) {
                TabContract.Presenter presenter = mPresenterList.valueAt(i);
                if (presenter != null) {
                    presenter.setFiltrate(filtrate);
                }
            }
        } else {
            TabContract.Presenter presenter = mPresenterList.get(curTabId);
            if (presenter != null) {
                presenter.setFiltrate(filtrate);
            }
        }
    }

    /**
     * @return true 右上角筛选对所有TabFragment生效, false 右上角筛选只对当前TabFragment生效
     */
    private boolean hasAllTabFiltrate() {
        switch (mTabData.source) {
        case TabSource.SOURCE_COMMUNITY:
            return false;
        default:
            return true;
        }
    }
}
