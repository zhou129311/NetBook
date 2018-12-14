package com.xzhou.book.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.ui.common.BaseActivity;
import com.xzhou.book.ui.view.RVPIndicator;
import com.xzhou.book.utils.SPUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements MainContract.View {

    @BindView(R.id.indicator)
    RVPIndicator mIndicatorView;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private List<String> mTabs;
    private MainContract.Presenter mPresenter;
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewData();
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.app_name);
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ZhuiShuSQApi.get().getBookListTags();
                }
            }).start();
            break;
        case R.id.action_scan_local_book:
            break;
        case R.id.action_night_mode:
            break;
        case R.id.action_settings:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViewData() {
        mTabs = Arrays.asList(getResources().getStringArray(R.array.home_tabs));
        mIndicatorView.setTabItemTitles(mTabs);
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return PagerFactory.SIZE;
            }

            @Override
            public Fragment getItem(int position) {
                return PagerFactory.getFragment(position, getSupportFragmentManager());
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(PagerFactory.SIZE - 1);
        mIndicatorView.setViewPager(mViewPager, PagerFactory.FRAGMENT_BOOKRACK);
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
        ToastUtils.release();
        SPUtils.get().uninit();
        PagerFactory.releaseCache();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
