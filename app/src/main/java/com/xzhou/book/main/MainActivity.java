package com.xzhou.book.main;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

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
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.SnackBarUtils;
import com.xzhou.book.widget.Indicator;

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
    private boolean mMainNight;
    private MenuAdapter mMenuAdapter;
    private ListPopupWindow mListPopupWindow;
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);
        mLayoutInflater = LayoutInflater.from(this);
        initViewData();
        mMainNight = AppSettings.isNight();
    }

    @Override
    protected void initToolBar() {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextAppearance(this, R.style.MainTitleTextStyle);
    }

    private void showPopupMenu() {
        if (mListPopupWindow == null) {
            mMenuAdapter = new MenuAdapter();
            mListPopupWindow = new ListPopupWindow(this);
            mListPopupWindow.setWidth((int) (AppUtils.getScreenWidth() / 2.6f));
            mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mListPopupWindow.setAnchorView(mToolbar);
            mListPopupWindow.setHorizontalOffset(AppUtils.dip2px(-16));//相对锚点偏移值，正值表示向右偏移
            mListPopupWindow.setVerticalOffset(AppUtils.dip2px(-5));//相对锚点偏移值，正值表示向下偏移
            mListPopupWindow.setDropDownGravity(Gravity.END);
            mListPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dialog_common));
            mListPopupWindow.setModal(true);//模态框，设置为true响应物理键
            mListPopupWindow.setAdapter(mMenuAdapter);
            mListPopupWindow.setAnimationStyle(R.style.popup_anim_style);
            mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mListPopupWindow.dismiss();
                    switch (position) {
                    case 0:
                        if (AppSettings.isNight()) {
                            AppSettings.setNight(false);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            mMainNight = false;
                        } else {
                            AppSettings.setNight(true);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            mMainNight = true;
                        }
                        recreate();
                        break;
                    case 1:
                        SettingsActivity.startActivity(mActivity);
                        break;
                    }
                }
            });
        }
        mListPopupWindow.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isNight = AppSettings.isNight();
        if (mMainNight != isNight) {
            recreate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_search:
            SearchActivity.startActivity(this);
            break;
        case R.id.action_more:
            showPopupMenu();
//            SearchActivity.startActivity(this);
            break;
//        case R.id.action_night_mode:
//            if (AppSettings.isNight()) {
//                AppSettings.setNight(false);
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                mMainNight = false;
//            } else {
//                AppSettings.setNight(true);
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                mMainNight = true;
//            }
//            recreate();
//            break;
//        case R.id.action_settings:
//            SettingsActivity.startActivity(mActivity);
//            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        BookshelfFragment f = (BookshelfFragment) mFragment.get(FRAGMENT_BOOKSHELF);
        if (mViewPager.getCurrentItem() != FRAGMENT_BOOKSHELF) {
            setCurFragment(FRAGMENT_BOOKSHELF);
        } else if (f != null && f.hasEdit()) {
            f.cancelEdit();
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

    private class MenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.menu_item_icon, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.mImageView.setImageResource(R.mipmap.theme_night);
                viewHolder.mTextView.setText(R.string.menu_main_night_mode);
            } else if (position == 1) {
                viewHolder.mImageView.setImageResource(R.mipmap.home_menu_6);
                viewHolder.mTextView.setText(R.string.settings);
            }
            return convertView;
        }

        class ViewHolder {
            private ImageView mImageView;
            private TextView mTextView;

            ViewHolder(View view) {
                mImageView = view.findViewById(R.id.menu_icon);
                mTextView = view.findViewById(R.id.menu_text);
            }
        }
    }
}
