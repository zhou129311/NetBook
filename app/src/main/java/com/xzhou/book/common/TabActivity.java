package com.xzhou.book.common;

import android.app.Activity;
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
import com.xzhou.book.widget.RVPIndicator;

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

    private final SparseArrayCompat<TabFragment> mFragments = new SparseArrayCompat<>();
    private final SparseArrayCompat<TabContract.Presenter> mPresenterList = new SparseArrayCompat<>();
    private Entities.TabData mTabData;
    private List<String> mTabNames;
    private ListPopupWindow mListPopupWindow;
    private FiltrateAdapter mFiltrateAdapter;

    public static void startActivity(Activity activity, Entities.TabData data) {
        if (data == null) {
            throw new NullPointerException("data cannot be null");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTabData.filtrate != null && mTabData.filtrate.length > 0) {
            if (mTabData.source == TabSource.SOURCE_TOPIC_LIST) {
                getMenuInflater().inflate(R.menu.menu_topic, menu);
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
                    mFragments.put(f.getPosition(), f);
                    mPresenterList.put(f.getPosition(), new TabPresenter(f, mTabData, f.getPosition()));
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
            mIndicator.setViewPager(mViewPager, 0);
        }
    }

    private void showPopupWindow(final MenuItem item) {
        if (mTabData.filtrate != null && mTabData.filtrate.length > 0) {
            item.setTitle(R.string.pack_up);
            if (mListPopupWindow == null) {
                List<String> list = new ArrayList<>();
                if (mTabData.source == TabSource.SOURCE_TOPIC_LIST) {
                    list.add(getString(R.string.all_topic));
                    list.add(getString(R.string.male_topic));
                    list.add(getString(R.string.female_topic));
                } else {
                    list.addAll(Arrays.asList(mTabData.filtrate));
                }
                Log.i(TAG, "showPopupWindow::" + Arrays.toString(mTabData.filtrate));
                mFiltrateAdapter = new FiltrateAdapter(mActivity, list);
                mListPopupWindow = new ListPopupWindow(mActivity);
                mListPopupWindow.setAdapter(mFiltrateAdapter);
                mListPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                mListPopupWindow.setAnchorView(mToolbar);
                //mListPopupWindow.setVerticalOffset(-mToolbar.getHeight());
                mListPopupWindow.setModal(true);
                mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mFiltrateAdapter.setChecked(position);
                        mToolbar.setTitle(mTabData.filtrate[position]);
                        mListPopupWindow.dismiss();
                        for (int i = 0, size = mPresenterList.size(); i < size; i++) {
                            TabContract.Presenter presenter = mPresenterList.valueAt(i);
                            if (presenter != null) {
                                presenter.setFiltrate(mTabData.filtrate[position]);
                            }
                        }
                    }
                });
                mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        item.setTitle(R.string.filtrate);
                    }
                });
            }
            mListPopupWindow.show();
        }
    }
}
