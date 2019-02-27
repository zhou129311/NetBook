package com.xzhou.book.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
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
import com.xzhou.book.models.Entities;
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.SnackBarUtils;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.Indicator;

import org.json.JSONException;
import org.json.JSONObject;

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
    private MenuItem mUserItem;
    private long mLastBackPressedTime;
    private Tencent mTencent;
    private Entities.Login mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);
        initViewData();

        mTencent = Tencent.createInstance("100497199", this.getApplicationContext());
    }

    @Override
    protected void initToolBar() {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextAppearance(this, R.style.MainTitleTextStyle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mUserItem = menu.findItem(R.id.action_login);
        updateLogin(AppSettings.getLogin());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_search:
            SearchActivity.startActivity(this);
            break;
        case R.id.action_login:
            if (mLogin == null) {
                doLogin();
            } else {
                UserActivity.startActivity(this);
            }
            break;
        case R.id.action_night_mode:
            if (AppSettings.isNight()) {
                AppSettings.setNight(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppSettings.setNight(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
            break;
        case R.id.action_settings:
            SettingsActivity.startActivity(this);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateLogin(Entities.Login login) {
        mLogin = login;
        if (mLogin != null) {
            updateLoginIcon(login.user.avatar());
            mUserItem.setTitle(login.user.nickname);
        }
    }

    private void updateLoginIcon(String url) {
        if (TextUtils.isEmpty(url)) {
            mUserItem.setIcon(R.mipmap.home_menu_0);
        } else {
            Glide.with(this).load(url).apply(ImageLoader.getCircleOptions(R.mipmap.avatar_default)).into(mSimpleTarget);
        }
    }

    private SimpleTarget<Drawable> mSimpleTarget = new SimpleTarget<Drawable>() {
        @Override
        public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
            resource.setBounds(0, 0, 50, 50);
            mUserItem.setIcon(resource);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, listener);
    }

    private void doLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", listener);
        }
    }

    private IUiListener listener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            Log.i("doComplete:" + values.toString());
//            updateLoginButton();
            try {
                String openId = values.getString("openid");
                String token = values.getString("access_token");
                String type = "QQ";
                mBookPresenter.login(openId, token, type);
            } catch (JSONException e) {
                e.printStackTrace();
                ToastUtils.showShortToast("Login Error:" + e.getMessage());
            }
        }
    };

    @Override
    public void onBackPressed() {
        BookshelfFragment f = (BookshelfFragment) mFragment.get(FRAGMENT_BOOKSHELF);
        if (mViewPager.getCurrentItem() != FRAGMENT_BOOKSHELF) {
            setCurFragment(FRAGMENT_BOOKSHELF);
        } else if (f.hasEdit()) {
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
