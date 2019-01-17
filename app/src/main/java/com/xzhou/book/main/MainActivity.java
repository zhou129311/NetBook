package com.xzhou.book.main;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.bookshelf.BookshelfContract;
import com.xzhou.book.bookshelf.BookshelfFragment;
import com.xzhou.book.bookshelf.BookshelfPresenter;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.community.CommunityContract;
import com.xzhou.book.community.CommunityFragment;
import com.xzhou.book.community.CommunityPresenter;
import com.xzhou.book.find.FindContract;
import com.xzhou.book.find.FindFragment;
import com.xzhou.book.find.FindPresenter;
import com.xzhou.book.utils.SnackBarUtils;
import com.xzhou.book.widget.Indicator;
import com.xzhou.book.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    public static final int FRAGMENT_BOOKSHELF = 0;
    public static final int FRAGMENT_COMMUNITY = FRAGMENT_BOOKSHELF + 1;
    public static final int FRAGMENT_FIND = FRAGMENT_COMMUNITY + 1;
    public static final int SIZE = FRAGMENT_FIND + 1;

    @BindView(R.id.indicator)
    Indicator mIndicatorView;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private SparseArrayCompat<BaseFragment> mFragment;
    private BookshelfContract.Presenter mBookPresenter;
    private CommunityContract.Presenter mCommPresenter;
    private FindContract.Presenter mFindPresenter;
    private long mLastBackPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);
        initViewData();
    }

    @Override
    protected void initToolBar() {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextAppearance(this, R.style.MainTitleTextStyle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_search:
            ToastUtils.showShortToast("该功能正在开发中...");
            break;
        case R.id.action_scan_local_book:
            break;
        case R.id.action_night_mode:
            break;
        case R.id.action_settings:
            SettingsActivity.startActivity(this);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() != FRAGMENT_BOOKSHELF) {
            setCurFragment(FRAGMENT_BOOKSHELF);
        } else {
            if (SystemClock.elapsedRealtime() - mLastBackPressedTime < 1500) {
                super.onBackPressed();
            } else {
                mLastBackPressedTime = SystemClock.elapsedRealtime();
                SnackBarUtils.makeShort(getContentView(), getString(R.string.exit_tips)).show(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    private void initViewData() {
        mFragment = new SparseArrayCompat<>();
        restoreFragment();
        List<String> tabs = Arrays.asList(getResources().getStringArray(R.array.home_tabs));
        mIndicatorView.setTabItemTitles(tabs);
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return SIZE;
            }

            @Override
            public BaseFragment getItem(int position) {
                return getFragment(position);
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(SIZE - 1);
        mIndicatorView.setViewPager(mViewPager, FRAGMENT_BOOKSHELF);
    }

    public void setCurFragment(int tab) {
        mViewPager.setCurrentItem(tab, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void restoreFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof BookshelfFragment) {
                    mFragment.put(FRAGMENT_BOOKSHELF, (BookshelfFragment) f);
                    mBookPresenter = new BookshelfPresenter((BookshelfContract.View) f);
                } else if (f instanceof CommunityFragment) {
                    mFragment.put(FRAGMENT_COMMUNITY, (CommunityFragment) f);
                    mCommPresenter = new CommunityPresenter((CommunityContract.View) f);
                } else if (f instanceof FindFragment) {
                    mFragment.put(FRAGMENT_FIND, (FindFragment) f);
                    mFindPresenter = new FindPresenter((FindContract.View) f);
                }
            }
        }
    }

    private BaseFragment getFragment(int position) {
        BaseFragment fragment = mFragment.get(position);
        if (fragment != null) {
            return fragment;
        }
        switch (position) {
        case FRAGMENT_BOOKSHELF:
            fragment = new BookshelfFragment();
            mBookPresenter = new BookshelfPresenter((BookshelfContract.View) fragment);
            break;
        case FRAGMENT_COMMUNITY:
            fragment = new CommunityFragment();
            mCommPresenter = new CommunityPresenter((CommunityContract.View) fragment);
            break;
        case FRAGMENT_FIND:
            fragment = new FindFragment();
            mFindPresenter = new FindPresenter((FindContract.View) fragment);
            break;
        default:
            throw new IllegalArgumentException("getFragment position " + position + " undefined");
        }
        mFragment.put(position, fragment);
        return fragment;
    }
}
