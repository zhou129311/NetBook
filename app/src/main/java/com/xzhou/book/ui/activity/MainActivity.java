package com.xzhou.book.ui.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.ui.view.RVPIndicator;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.indicator)
    RVPIndicator mIndicatorView;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private List<String> mTabs;
    private List<Fragment> mTabFragments;

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

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
