package com.xzhou.book.read;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.DownloadManager;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonDialog;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ThemeUtils;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.SwipeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ReadActivity extends BaseActivity<ReadContract.Presenter> implements ReadContract.View, DownloadManager.DownloadCallback {
    private static final String TAG = "ReadActivity";

    private static final long MIN_SPACE = 5 * 60 * 1000;
    private static final long MAX_READ_TIME = 60 * 60 * 1000;
    public static final String EXTRA_BOOK = "localBook";
    //    @BindView(R.id.read_rl_view)
//    RelativeLayout mMainLayout;
    @BindView(R.id.end_ll_view)
    ReadSlideView mEndSlideView;
    @BindView(R.id.read_dl_slide)
    SwipeLayout mSwipeLayout;
    @BindView(R.id.read_view_pager)
    ReadViewPager mReadViewPager;

    @BindView(R.id.day_night_view)
    TextView mDayNightView;
    @BindView(R.id.orientation_view)
    TextView mOrientationView;

    @BindView(R.id.read_abl_top_menu)
    AppBarLayout mReadTopBar;
    @BindView(R.id.read_bottom_bar)
    ConstraintLayout mReadBottomBar;
    @BindView(R.id.download_progress_tv)
    TextView mDownloadTv;

    @BindView(R.id.read_setting_layout)
    ConstraintLayout mReadSettingLayout;
    @BindView(R.id.brightness_seek_bar)
    SeekBar mBrightnessSeekBar;
    @BindView(R.id.brightness_checkbox)
    CheckBox mBrightnessCheckbox;

    @BindView(R.id.theme_recycler_view)
    RecyclerView mThemeRecyclerView;

    // tool bar show hide anim
    private Animation mTopInAnim;
    private Animation mTopOutAnim;
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;

    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private ReadPageManager[] mPageManagers = new ReadPageManager[3];
    private int mCurChapter;
    private int mPrePosition;
    private int mCurPosition;
    private int mScrollState = ViewPager.SCROLL_STATE_IDLE;
    private long mStartReadTime;
    private long mFirstStartReadTime;
    private ReadSleepDialog mSleepDialog;

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        if (book.isPicture) {
            ReadCartoonActivity.startActivity(context, book);
            return;
        }
        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra(EXTRA_BOOK, book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBook = getIntent().getParcelableExtra(EXTRA_BOOK);
        if (mBook == null && savedInstanceState != null) {
            mBook = savedInstanceState.getParcelable(EXTRA_BOOK);
        }
        if (mBook == null) {
            ToastUtils.showShortToast("出现错误，打开失败");
            finish();
            return;
        }

        setContentView(R.layout.activity_read);

        mReadTopBar.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);
        mEndSlideView.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);
        if (!AppSettings.HAS_FULL_SCREEN_MODE) {
            mReadViewPager.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);
        }
        initMenuAnim();
        hideReadToolBar();
        initBrightness();
        initThemeView(AppSettings.READ_THEME);
        initReadPageView();
        updateThemeView(AppSettings.READ_THEME, AppSettings.isNight());
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(mBatteryReceiver, filter);
        updateBattery(intent);
        mSwipeLayout.setOnStateListener(new SwipeLayout.OnStateListener() {
            @Override
            public void onOpen() {
                showReadToolBar();
                mReadViewPager.setCanTouch(false);
            }

            @Override
            public void onClose() {
                mReadViewPager.setCanTouch(true);
            }
        });
        DownloadManager.get().addCallback(mBook._id, ReadActivity.this);
        mEndSlideView.setBook(mBook, this);
        if (!mBook.isBaiduBook) {
            mPresenter.loadAllSource();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mOrientationView.setText(R.string.book_read_portrait);
        } else {
            mOrientationView.setText(R.string.book_read_landscape);
        }
        relayoutPageContent();
    }

    private void initThemeView(@Constant.ReadTheme int theme) {
        final ThemeAdapter themeAdapter = new ThemeAdapter(ThemeUtils.THEME_LIST);
        themeAdapter.bindToRecyclerView(mThemeRecyclerView);
        mThemeRecyclerView.setHasFixedSize(true);
        MyLinearLayoutManager lm = new MyLinearLayoutManager(this);
        lm.setOrientation(RecyclerView.HORIZONTAL);
        mThemeRecyclerView.setLayoutManager(lm);
        mThemeRecyclerView.addItemDecoration(new ThemeItemDecoration(AppUtils.dip2px(30)));
        themeAdapter.setTheme(theme);
        themeAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                themeAdapter.setTheme(position);
                setReadTheme(position);
            }
        });
        mThemeRecyclerView.scrollToPosition(theme);
        mReadBottomBar.setVisibility(View.INVISIBLE);
        mReadSettingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        mStartReadTime = SystemClock.elapsedRealtime();
        long lastTime = AppSettings.getLastStopReadTime();
        if (lastTime > 0 && mStartReadTime - lastTime < MIN_SPACE) {
            //距离上次阅读时间小于5分钟，按照上次阅读时间处理
            mFirstStartReadTime = lastTime;
        } else {
            mFirstStartReadTime = mStartReadTime;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AppSettings.HAS_FULL_SCREEN_MODE && mReadViewPager.getPaddingTop() == 0) {
            mReadViewPager.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);
            relayoutPageContent();
        } else if (AppSettings.HAS_FULL_SCREEN_MODE && mReadViewPager.getPaddingTop() > 0) {
            mReadViewPager.setPadding(0, 0, 0, 0);
            relayoutPageContent();
        }
        if (AppSettings.HAS_FULL_SCREEN_MODE && mReadBottomBar.getVisibility() != View.VISIBLE) {
            hideSystemBar();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        long stop = SystemClock.elapsedRealtime();
        long oldReadTime = AppSettings.getTotalReadTime();
        long newReadTime = stop - mStartReadTime;
        AppSettings.setTotalReadTime(oldReadTime + newReadTime);
        AppSettings.setLastStopReadTime(stop);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelable(EXTRA_BOOK, mBook);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!AppSettings.HAS_VOLUME_TURN_PAGE) {
            return super.onKeyUp(keyCode, event);
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            return true;
        case KeyEvent.KEYCODE_VOLUME_UP:
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!AppSettings.HAS_VOLUME_TURN_PAGE) {
            return super.onKeyUp(keyCode, event);
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            nextPage();
            return true;
        case KeyEvent.KEYCODE_VOLUME_UP:
            previousPage();
            return true;
        }
        Log.i(TAG, "onKeyUp keyCode= " + keyCode);
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
        DownloadManager.get().removeCallback(mBook._id, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryReceiver);
    }

    private void initMenuAnim() {
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(mBook.title);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.reader_menu_bg_color));
    }

    @Override
    public void onBackPressed() {
        if (mSwipeLayout.isMenuOpen()) {
            Log.i(TAG, "onBackPressed isMenuOpen");
            mSwipeLayout.smoothToCloseMenu();
            hideReadToolBar();
        } else if (!mBook.isBookshelf()) {
            Log.i(TAG, "onBackPressed Book is not bookshelf");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.book_read_add_book_title)
                    .setMessage(R.string.book_read_add_book_msg)
                    .setNegativeButton(R.string.book_read_not_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.book_read_join, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            BookProvider.insertOrUpdate(mBook, true);
                            AppSettings.saveChapterList(mBook._id, mChaptersList);
                            finish();
                        }
                    });
            builder.show();
        } else {
            Log.i(TAG, "onBackPressed");
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            mBook = data.getParcelableExtra(EXTRA_BOOK);
            getIntent().putExtra(EXTRA_BOOK, mBook);
            recreate();
        }
    }

    @Override
    protected ReadContract.Presenter createPresenter() {
        return new ReadPresenter(this, mBook);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(mBook.isBaiduBook ? R.menu.menu_read_baidu : R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_community:
            AppUtils.startDiscussionByBook(mActivity, mBook.title, mBook._id, 0);
            return true;
        case R.id.menu_change_source:
            ReadSourceActivity.startActivity(this, mBook);
            return true;
        /*case R.id.menu_change_mode:
            return true;*/
        case R.id.menu_book_detail:
            BookDetailActivity.startActivity(mActivity, mBook._id);
            return true;
        case R.id.menu_read_web:
            ReadWebActivity.startActivity(mActivity, mBook);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onUpdatePages(PageContent[] pageContent) {
        if (pageContent != null && pageContent.length == 3) {
            checkReadTime();
            mReadViewPager.setCanTouch(false);
            for (int i = 0; i < 3; i++) {
                mPageManagers[i].getReadPage().setPageContent(pageContent[i]);
                Log.d(TAG, "onUpdatePages:: pageContent[" + i + "] = " + pageContent[i]);
                if (pageContent[i] != null && pageContent[i].isShow) {
                    if (!TextUtils.isEmpty(pageContent[i].chapterTitle)) {
                        mReadViewPager.setCanTouch(true);
                    }
                    mPrePosition = i;
                    mCurChapter = pageContent[i].chapter;
                    mReadViewPager.setCurrentItem(i, false);
                }
            }
        } else {
            mPageManagers[mReadViewPager.getCurrentItem()].getReadPage().setErrorView(true);
            mReadViewPager.setCanTouch(false);
        }
    }

    @Override
    public void onUpdateSource(List<Entities.BookSource> list) {
        mEndSlideView.setSource(list);
    }

    private void initReadPageView() {
        for (int i = 0; i < mPageManagers.length; i++) {
            final ReadPage page = new ReadPage(this);
            final int position = i;
            page.setOnReloadListener(new ReadPage.OnReloadListener() {
                @Override
                public void onReload() {
                    mPresenter.reloadCurPage(position, page.getPageContent());
                }
            });
            mPageManagers[i] = new ReadPageManager();
            mPageManagers[i].setReadPage(page);
        }
        final ReadPage page = mPageManagers[0].getReadPage();
        page.setTextLayoutListener(new ReadPage.TextLayoutListener() {

            @Override
            public void onLayout(boolean isFirst) {
                Log.i(TAG, "onLayout::isFirst = " + isFirst);
                relayoutPageContent();
                if (isFirst) {
                    mPresenter.start();
                }
            }
        });
        mReadViewPager.setPageManagers(mPageManagers);
        mReadViewPager.setSwipeLayout(mSwipeLayout);
        mReadViewPager.setOffscreenPageLimit(3);
        mReadViewPager.setOnClickChangePageListener(new ReadViewPager.OnClickChangePageListener() {
            @Override
            public void onPrevious() {
                previousPage();
            }

            @Override
            public void onNext() {
                nextPage();
            }
        });
        mReadViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        mReadViewPager.setCanTouch(false);
        mReadViewPager.setAdapter(new MyPagerAdapter());
        mReadViewPager.setCurrentItem(0, false);
    }

    private void previousPage() {
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            return;
        }
        if (hideReadToolBar()) {
            return;
        }
        int curPos = mReadViewPager.getCurrentItem();
        if (curPos <= 0) {
            if (mPageManagers[0].getReadPage().isPageStart()) {
                ToastUtils.showShortToast("已经是第一页了");
            }
            return;
        }
        mReadViewPager.setCurrentItem(curPos - 1, false);
        mCurPosition = curPos - 1;
        changePage();
    }

    private void nextPage() {
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            return;
        }
        if (hideReadToolBar()) {
            return;
        }
        int curPos = mReadViewPager.getCurrentItem();
        if (curPos >= 2) {
            return;
        }
        ReadPage page = mPageManagers[curPos].getReadPage();
        if (page.isPageEnd()) {
            mSwipeLayout.smoothToOpenMenu();
            return;
        }
        mReadViewPager.setCurrentItem(curPos + 1, false);
        mCurPosition = curPos + 1;
        changePage();
    }

    private void changePage() {
        if (mCurPosition == mPrePosition) {
            return;
        }
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            ReadPage readPage = mPageManagers[mCurPosition].getReadPage();
            PageContent pageContent = readPage.getPageContent();
            readPage.checkLoading();
            Log.d(TAG, "changePage:: cur pageContent = " + pageContent);
            hideReadToolBar();
            if (mCurPosition > mPrePosition) {
                mPresenter.loadNextPage(mCurPosition, pageContent);
            } else if (mCurPosition < mPrePosition) {
                mPresenter.loadPreviousPage(mCurPosition, pageContent);
            }
            mReadViewPager.setCanTouch(false);
        }
    }

    private boolean hideReadToolBar() {
        if (mReadBottomBar.getVisibility() == View.VISIBLE) {
            mReadTopBar.startAnimation(mTopOutAnim);
            mReadBottomBar.startAnimation(mBottomOutAnim);
            mReadSettingLayout.setVisibility(View.GONE);
            mReadBottomBar.setVisibility(View.GONE);
            mReadTopBar.setVisibility(View.GONE);
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
            mReadTopBar.setVisibility(View.VISIBLE);
            mReadSettingLayout.setVisibility(View.GONE);
            mReadTopBar.startAnimation(mTopInAnim);
            mReadBottomBar.startAnimation(mBottomInAnim);
            showSystemBar();
        }
    }

    private void setReadTheme(@Constant.ReadTheme int theme) {
        AppSettings.setNight(false);
        updateThemeView(theme, false);
        AppSettings.saveReadTheme(theme);
    }

    private void updateThemeView(@Constant.ReadTheme int theme, boolean isNight) {
        mDayNightView.setActivated(isNight);
        mDayNightView.setText(isNight ? R.string.book_read_mode_day_manual_setting : R.string.book_read_mode_night_manual_setting);
        updatePageTheme(theme, isNight);
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
                setBrightnessProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setBrightnessProgress(int value) {
        mBrightnessSeekBar.setProgress(value);
        AppUtils.setScreenBrightness(value, this);
        AppSettings.saveBrightness(value);
    }

    @Override
    public void setPresenter(ReadContract.Presenter presenter) {
    }

    @OnCheckedChanged({R.id.brightness_checkbox})
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        AppSettings.saveBrightnessSystem(checked);
        mBrightnessSeekBar.setEnabled(!checked);
        if (checked) {
            AppUtils.setScreenBrightness(-1, this);
        } else {
            AppUtils.setScreenBrightness(AppSettings.getBrightness(this), this);
        }
    }

    @OnClick({R.id.brightness_min, R.id.brightness_max, R.id.auto_reader_view, R.id.text_size_dec, R.id.text_size_inc,
            R.id.more_setting_view, R.id.day_night_view, R.id.orientation_view, R.id.setting_view, R.id.download_view,
            R.id.toc_view, R.id.read_view_pager, R.id.read_bottom_bar})
    public void onViewClicked(View view) {
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            hideReadToolBar();
            return;
        }
        switch (view.getId()) {
        case R.id.read_view_pager:
            if (!hideReadToolBar() && mSwipeLayout.isMenuClosed()) {
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
        case R.id.auto_reader_view:
            break;
        case R.id.text_size_dec:
            updateFontSize(true);
            break;
        case R.id.text_size_inc:
            updateFontSize(false);
            break;
        case R.id.more_setting_view:
            ReadSettingActivity.startActivity(mActivity);
            break;
        case R.id.day_night_view:
            boolean isNight = !AppSettings.isNight();
            AppSettings.setNight(isNight);
            updateThemeView(AppSettings.READ_THEME, isNight);
            AppCompatDelegate.setDefaultNightMode(isNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            break;
        case R.id.orientation_view:
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            break;
        case R.id.setting_view:
            if (mReadSettingLayout.getVisibility() == View.VISIBLE) {
                mReadSettingLayout.setVisibility(View.GONE);
            } else {
                mReadSettingLayout.setVisibility(View.VISIBLE);
            }
            break;
        case R.id.download_view:
            if (mChaptersList == null || mChaptersList.size() < 1) {
                ToastUtils.showShortToast("未找到章节列表");
                return;
            }
            ItemDialog.Builder builder = new ItemDialog.Builder(this);
            builder.setTitle("缓存多少章？").setItems(DownloadManager.DOWNLOAD_ITEMS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!mBook.isBookshelf()) {
                        BookProvider.insertOrUpdate(mBook, true);
                    }
                    mReadSettingLayout.setVisibility(View.GONE);
                    DownloadManager.Download download = DownloadManager.createDownload(which, mCurChapter, mChaptersList
                            , mBook.isBaiduBook ? mBook.curSourceHost : null);
                    boolean rel = DownloadManager.get().startDownload(mBook._id, download);
                    if (!rel) {
                        ToastUtils.showShortToast("正在缓存中...");
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
            break;
        case R.id.toc_view:
            if (mChaptersList == null || mChaptersList.size() < 1) {
                ToastUtils.showShortToast("未找到章节列表");
                return;
            }

            final CommonDialog fragmentDialog = getDialog();
            fragmentDialog.setOnItemClickListener(new BookTocDialog.OnItemClickListener() {
                @Override
                public void onClickItem(int chapter, Entities.Chapters chapters) {
                    Log.i(TAG, "onClickItem::" + chapter);
                    mPresenter.loadChapter(mReadViewPager.getCurrentItem(), chapter);
                    fragmentDialog.dismiss();
                }
            });
            fragmentDialog.setChapter(mCurChapter);
            fragmentDialog.show(getSupportFragmentManager(), "TocDialog");
            break;
        }
    }

    private CommonDialog getDialog() {
        CommonDialog fragmentDialog = (CommonDialog) getSupportFragmentManager().findFragmentByTag("TocDialog");
        if (fragmentDialog == null) {
            final BookTocDialog dialog = BookTocDialog.createDialog(this, mChaptersList, mBook);
            fragmentDialog = CommonDialog.newInstance(new CommonDialog.OnCallDialog() {
                @Override
                public Dialog getDialog(Context context) {
                    return dialog;
                }
            }, true);
        }
        return fragmentDialog;
    }

    private void relayoutPageContent() {
        ReadPage curPage = mPageManagers[mReadViewPager.getCurrentItem()].getReadPage();
        int maxLineCount = curPage.mChapterContent.getMaxLineCount();
        int width = curPage.mChapterContent.getMeasuredWidth();
        PageLines pageLines = curPage.getPageContent() == null ? null : curPage.getPageContent().mPageLines;
        mPresenter.setTextViewParams(maxLineCount, curPage.mChapterContent.getPaint(), width, pageLines);
    }

    private void updateFontSize(boolean isDec) {
        for (ReadPageManager page : mPageManagers) {
            if (isDec) {
                page.getReadPage().decFontSize();
            } else {
                page.getReadPage().incFontSize();
            }
        }
        relayoutPageContent();
    }

    private void updatePageTheme(int theme, boolean isNight) {
        for (ReadPageManager page : mPageManagers) {
            page.getReadPage().setReadTheme(theme, isNight);
        }
    }

    private void updateBattery(Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int curBattery = (int) (((float) level / (float) scale) * 100f);
            for (ReadPageManager page : mPageManagers) {
                page.getReadPage().setBattery(curBattery);
            }
        }
    }

    private void checkReadTime() {
        if (AppSettings.READ_SLEEP_TIME <= 0) {
            return;
        }
        long startSleepTime = AppSettings.getStartSleepTime();
        long curTime = SystemClock.elapsedRealtime();
        long oldSleepTime = curTime - startSleepTime;
        if (oldSleepTime > 0 && startSleepTime > 0 && oldSleepTime < AppSettings.READ_SLEEP_TIME) {
            showSleepTimeDialog(AppSettings.READ_SLEEP_TIME - oldSleepTime);
            return;
        }
        if (curTime - mFirstStartReadTime < MAX_READ_TIME) {
            return;
        }
        AppSettings.setStartSleepTime(curTime);
        showSleepTimeDialog(AppSettings.READ_SLEEP_TIME);
    }

    public void resetFirstReadTime() {
        mFirstStartReadTime = SystemClock.elapsedRealtime();
        mSleepDialog = null;
    }

    private void showSleepTimeDialog(long countDownTime) {
        if (mSleepDialog != null && mSleepDialog.isShowing()) {
            return;
        }
        mSleepDialog = new ReadSleepDialog(this);
        mSleepDialog.updateCountTime(countDownTime);
        mSleepDialog.show();
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBattery(intent);
        }
    };

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

    @Override
    public void onStartDownload() {
        mDownloadTv.setVisibility(View.VISIBLE);
        mDownloadTv.setText(getString(R.string.book_read_download_start, mBook.title));
    }

    @Override
    public void onProgress(int progress, int max) {
        mDownloadTv.setVisibility(View.VISIBLE);
        mDownloadTv.setText(getString(R.string.book_read_download_progress, mBook.title, progress, max));
    }

    @Override
    public void onEndDownload(int failedCount, int error) {
        mDownloadTv.setVisibility(View.VISIBLE);
        if (error != DownloadManager.ERROR_NONE) {
            mDownloadTv.setText(error == DownloadManager.ERROR_NO_NETWORK ? R.string.book_read_download_error_net : R.string.book_read_download_error_topic);
        } else {
            String text = getString(R.string.book_read_download_complete, mBook.title);
            if (failedCount > 0) {
                text = getString(R.string.book_read_download_complete2, mBook.title, failedCount);
            }
            mDownloadTv.setText(text);
        }
    }

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
            View view = mPageManagers[position].getReadPage();
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }
    }

    private class ThemeAdapter extends BaseQuickAdapter<ThemeUtils.ReadTheme, CommonViewHolder> {
        private @Constant.ReadTheme
        int mCurTheme;

        ThemeAdapter(List<ThemeUtils.ReadTheme> list) {
            super(R.layout.item_theme_view, list);
        }

        void setTheme(@Constant.ReadTheme int theme) {
            mCurTheme = theme;
            notifyDataSetChanged();
        }

        @Override
        protected void convert(CommonViewHolder holder, ThemeUtils.ReadTheme item) {
            holder.setImageResource(R.id.theme_image_view, item.smBgResId);
            holder.getView(R.id.theme_image_sel_view).setActivated(item.theme == mCurTheme);
        }
    }

    public class ThemeItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpace;

        ThemeItemDecoration(int space) {
            mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int position = parent.getChildAdapterPosition(view); // item position
            if (position > 0) {
                outRect.set(mSpace, 0, 0, 0);
            }
        }
    }
}
