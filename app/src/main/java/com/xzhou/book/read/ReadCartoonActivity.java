package com.xzhou.book.read;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonDialog;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.xzhou.book.read.ReadActivity.EXTRA_BOOK;

public class ReadCartoonActivity extends BaseActivity<CartoonContract.Presenter> implements CartoonContract.View {
    public static final String TAG = "ReadCartoonActivity";
    @BindView(R.id.cartoon_view_pager)
    ReadViewPager mCartoonViewPager;
    @BindView(R.id.cartoon_abl_top_menu)
    AppBarLayout mCartoonAblTopMenu;
    @BindView(R.id.brightness_seek_bar)
    SeekBar mBrightnessSeekBar;
    @BindView(R.id.brightness_checkbox)
    CheckBox mBrightnessCheckbox;
    @BindView(R.id.read_setting_bottom_ll_layout)
    LinearLayout mReadSettingBtmLlLayout;
    @BindView(R.id.read_bottom_bar)
    ConstraintLayout mReadBottomBar;
    @BindView(R.id.read_setting_layout)
    ConstraintLayout mReadLightBar;

    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private ReadPageManager[] mPageManagers = new ReadPageManager[3];
    private int mCurChapter;
    private int mPrePosition;
    private int mCurPosition;
    private int mScrollState = ViewPager.SCROLL_STATE_IDLE;

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        Intent intent = new Intent(context, ReadCartoonActivity.class);
        intent.putExtra(EXTRA_BOOK, book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBook = getIntent().getParcelableExtra(EXTRA_BOOK);
        if (mBook == null && savedInstanceState != null) {
            mBook = savedInstanceState.getParcelable(EXTRA_BOOK);
        }
        if (mBook == null) {
            ToastUtils.showShortToast("出现错误，打开失败");
            finish();
            return;
        }
        setContentView(R.layout.activity_read_cartoon);
        hideReadToolBar();
        initViewPager();
        initBrightness();
        registerReceiver(mWifiStateReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    private void initBrightness() {
        boolean isSystem = AppSettings.isBrightnessSystem();
        mBrightnessCheckbox.setChecked(isSystem);
        mBrightnessSeekBar.setEnabled(!isSystem);
        mBrightnessSeekBar.setMax(100);
        mBrightnessSeekBar.setProgress(AppSettings.getBrightness(this));
        mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBrightnessSeekBar.setProgress(progress);
                AppUtils.setScreenBrightness(progress, ReadCartoonActivity.this);
                AppSettings.saveBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if (!isSystem) {
            AppUtils.setScreenBrightness(AppSettings.getBrightness(this), this);
        }
    }

    private void initViewPager() {
        for (int i = 0; i < mPageManagers.length; i++) {
            final ReadCartoonPage page = new ReadCartoonPage(this);
            final int position = i;
            page.setOnReloadListener(new ReadCartoonPage.OnReloadListener() {
                @Override
                public void onReload() {
                    mPresenter.reloadCurPage(position, page.getContent());
                }
            });
            mPageManagers[i] = new ReadPageManager();
            mPageManagers[i].setReadCartoonPage(page);
        }
//        final ReadCartoonPage page = mPageManagers[0].getReadCartoonPage();
//        page.setTextLayoutListener(new ReadPage.TextLayoutListener() {
//
//            @Override
//            public void onLayout(boolean isFirst) {
//                if (isFinishing() || isDestroyed()) {
//                    return;
//                }
//                Log.i(TAG, "onLayout::isFirst = " + isFirst);
////                relayoutPageContent();
//                if (isFirst) {
//                    mPresenter.start();
//                }
//            }
//        });
        mCartoonViewPager.setPageManagers(mPageManagers);
//        mCartoonViewPager.setSwipeLayout(mSwipeLayout);
        mCartoonViewPager.setOffscreenPageLimit(3);
        mCartoonViewPager.setOnClickChangePageListener(new ReadViewPager.OnClickChangePageListener() {
            @Override
            public void onPrevious() {
                previousPage();
            }

            @Override
            public void onNext() {
                nextPage();
            }
        });
        mCartoonViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurPosition = position;
                Log.i(TAG, "onPageSelected:mPrePosition=" + mPrePosition + " ,position=" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mScrollState = state;
                changePage();
            }
        });
        mCartoonViewPager.setCanTouch(false);
        mCartoonViewPager.setAdapter(new MyPagerAdapter());
        mCartoonViewPager.setCurrentItem(0, false);
        mPresenter.start();
    }

    private void changePage() {

    }

    private void nextPage() {

    }

    private void previousPage() {

    }

    @Override
    protected CartoonContract.Presenter createPresenter() {
        return new CartoonPresenter(this, mBook);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void finish() {
        super.finish();
        unregisterReceiver(mWifiStateReceiver);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(mBook.title);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.reader_menu_bg_color));
    }

    @OnClick({ R.id.brightness_min, R.id.brightness_max, R.id.previous_chapter_tv, R.id.next_chapter_tv, R.id.cartoon_view_pager,
            R.id.previous_page_tv, R.id.next_page_tv, R.id.toc_view, R.id.light_view, R.id.download_view, R.id.more_setting_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.cartoon_view_pager:
            if (!hideReadToolBar()) {
                showReadToolBar();
            }
            break;
        case R.id.brightness_min: {
            int curProgress = mBrightnessSeekBar.getProgress();
            if (mBrightnessSeekBar.isEnabled() && curProgress > 0) {
                mBrightnessSeekBar.setProgress(curProgress - 1);
            }
            break;
        }
        case R.id.brightness_max:
            int curProgress = mBrightnessSeekBar.getProgress();
            if (mBrightnessSeekBar.isEnabled() && curProgress < mBrightnessSeekBar.getMax()) {
                mBrightnessSeekBar.setProgress(curProgress + 1);
            }
            break;
        case R.id.previous_chapter_tv:

            break;
        case R.id.next_chapter_tv:

            break;
        case R.id.previous_page_tv:

            break;
        case R.id.next_page_tv:

            break;
        case R.id.toc_view:
            if (mChaptersList == null || mChaptersList.size() < 1) {
                ToastUtils.showShortToast("未找到章节列表");
                return;
            }

            final CommonDialog fragmentDialog = getDialog(mChaptersList);
            fragmentDialog.setOnItemClickListener(new BookTocDialog.OnItemClickListener() {
                @Override
                public void onClickItem(int chapter, Entities.Chapters chapters) {
                    Log.i(TAG, "onClickItem::" + chapter);
                    mPresenter.loadChapter(mCartoonViewPager.getCurrentItem(), chapter);
                    fragmentDialog.dismiss();
                }
            });
            fragmentDialog.setChapter(mCurChapter);
            fragmentDialog.show(getSupportFragmentManager(), "TocDialog");
            break;
        case R.id.light_view:

            break;
        case R.id.download_view:
            CartoonDownloadActivity.startActivity(this, mBook);
            break;
        case R.id.more_setting_view:
            ReadSettingActivity.startActivity(this);
            break;
        }
    }

    @Override
    public void initChapterList(List<Entities.Chapters> list) {
        if (list == null || list.size() <= 0) {
            ToastUtils.showShortToast("未找到本书内容，请检查网络后重试");
            return;
        }
        mChaptersList = list;
    }

    @Override
    public void onUpdatePages(CartoonContent[] pageContent) {
        if (pageContent != null && pageContent.length == 3) {
            mCartoonViewPager.setCanTouch(false);
            for (int i = 0; i < 3; i++) {
                mPageManagers[i].getReadCartoonPage().setPageContent(pageContent[i]);
                Log.d(TAG, "onUpdatePages:: pageContent[" + i + "] = " + pageContent[i]);
                if (pageContent[i] != null && pageContent[i].isShow) {
                    if (!TextUtils.isEmpty(pageContent[i].title)) {
                        mCartoonViewPager.setCanTouch(true);
                    }
                    mPrePosition = i;
                    mCurChapter = pageContent[i].chapter;
                    mCartoonViewPager.setCurrentItem(i, false);
                }
            }
        } else {
//            mPageManagers[mCartoonViewPager.getCurrentItem()].getReadPage().setErrorView(true);
            mCartoonViewPager.setCanTouch(false);
        }
    }

    @Override
    public void onUpdateSource(List<Entities.BookSource> list) {

    }

    @Override
    public void setPresenter(CartoonContract.Presenter presenter) {
    }

    private boolean hideReadToolBar() {
        if (mReadBottomBar.getVisibility() == View.VISIBLE) {
            mReadLightBar.setVisibility(View.GONE);
            mReadBottomBar.setVisibility(View.GONE);
            mCartoonAblTopMenu.setVisibility(View.GONE);
            if (AppSettings.HAS_FULL_SCREEN_MODE) {
                hideSystemBar();
            }
            return true;
        } else {
            if (AppSettings.HAS_FULL_SCREEN_MODE) {
                hideSystemBar();
            }
        }
        return false;
    }

    private void showReadToolBar() {
        if (mReadBottomBar.getVisibility() != View.VISIBLE) {
            mReadBottomBar.setVisibility(View.VISIBLE);
            mCartoonAblTopMenu.setVisibility(View.VISIBLE);
            mReadLightBar.setVisibility(View.GONE);
            showSystemBar();
        }
    }

    private void showSystemBar() {
        //显示
        AppUtils.showUnStableStatusBar(this);
//        if (isFullScreen) {
//            AppUtils.showUnStableNavBar(this);
//        }
    }

    private void hideSystemBar() {
        //隐藏
        AppUtils.hideStableStatusBar(this);
//        if (isFullScreen) {
//            AppUtils.hideStableNavBar(this);
//        }
    }

    private void updateWiFiState() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        boolean state = wifiInfo != null;
        for (ReadPageManager manager : mPageManagers) {
            if (manager != null && manager.getReadCartoonPage() != null) {
                manager.getReadCartoonPage().updateWiFiState(state);
            }
        }
    }

    private CommonDialog getDialog(List<Entities.Chapters> list) {
        CommonDialog fragmentDialog = (CommonDialog) getSupportFragmentManager().findFragmentByTag("TocDialog");
        if (fragmentDialog == null) {
            final BookTocDialog dialog = BookTocDialog.createDialog(this, list, mBook);
            fragmentDialog = CommonDialog.newInstance(new CommonDialog.OnCallDialog() {
                @Override
                public Dialog getDialog(Context context) {
                    return dialog;
                }
            }, true);
        }
        return fragmentDialog;
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) { //断开连接
                updateWiFiState();
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                updateWiFiState();
            }
        }
    };

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = mPageManagers[position].getReadCartoonPage();
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }
    }
}
