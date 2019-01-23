package com.xzhou.book.read;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xzhou.book.DownloadManager;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonDialog;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.SwipeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ReadActivity extends BaseActivity<ReadContract.Presenter> implements ReadContract.View, DownloadManager.DownloadCallback {
    private static final String TAG = "ReadActivity";

    private static final String EXTRA_BOOK = "localBook";
    //    @BindView(R.id.read_rl_view)
//    RelativeLayout mMainLayout;
    @BindView(R.id.end_ll_view)
    ReadSlideView mEndSlideView;
    @BindView(R.id.read_dl_slide)
    SwipeLayout mSwipeLayout;
    @BindView(R.id.read_view_pager)
    ReadViewPager mReadViewPager;

    @BindView(R.id.read_abl_top_menu)
    AppBarLayout mReadTopBar;
    @BindView(R.id.read_bottom_bar)
    ConstraintLayout mReadBottomBar;
    @BindView(R.id.download_progress_tv)
    TextView mDownloadTv;

    @BindView(R.id.read_setting_layout)
    ConstraintLayout mReadSettingLayout;
    //    @BindView(R.id.brightness_min)
//    ImageView mBrightnessMin;
    @BindView(R.id.brightness_seek_bar)
    SeekBar mBrightnessSeekBar;
    //    @BindView(R.id.brightness_max)
//    ImageView mBrightnessMax;
    @BindView(R.id.brightness_checkbox)
    CheckBox mBrightnessCheckbox;
    //    @BindView(R.id.auto_reader_view)
//    TextView mAutoReaderView;
//    @BindView(R.id.text_size_dec)
//    ImageView mTextSizeDec;
//    @BindView(R.id.text_size_inc)
//    ImageView mTextSizeInc;
//    @BindView(R.id.more_setting_view)
//    TextView mMoreSettingView;
    @BindView(R.id.theme_white_view)
    ImageView mThemeWhiteView;
    @BindView(R.id.theme_brown_view)
    ImageView mThemeBrownView;
    @BindView(R.id.theme_green_view)
    ImageView mThemeGreenView;

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

    public static void startActivity(Context context, BookProvider.LocalBook book) {
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
        initMenuAnim();
        hideReadToolBar();
        initBrightness();
        initReadPageView();
        updateThemeView(AppSettings.getReadTheme());
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
        mPresenter.loadAllSource();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelable(EXTRA_BOOK, mBook);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryReceiver);
    }

    private void initMenuAnim() {
        mReadTopBar.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);
        mEndSlideView.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            hideReadToolBar();
        } else if (!mBook.isBookshelf()) {
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
                            finish();
                        }
                    });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        DownloadManager.get().removeCallback(mBook._id, this);
    }

    @Override
    protected ReadContract.Presenter createPresenter() {
        return new ReadPresenter(this, mBook);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_community:
            AppUtils.startDiscussionByBook(mActivity, mBook.title, mBook._id, 0);
            return true;
        case R.id.menu_change_source:
            return true;
        case R.id.menu_change_mode:
            return true;
        case R.id.menu_book_detail:
            BookDetailActivity.startActivity(mActivity, mBook._id);
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

            @Override
            public void onNext() {
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
        }
    }

    private boolean hideReadToolBar() {
        if (mReadBottomBar.getVisibility() == View.VISIBLE) {
            mReadTopBar.startAnimation(mTopOutAnim);
            mReadBottomBar.startAnimation(mBottomOutAnim);
            mReadSettingLayout.setVisibility(View.GONE);
            mReadBottomBar.setVisibility(View.GONE);
            mReadTopBar.setVisibility(View.GONE);
            hideSystemBar();
            return true;
        }
        return false;
    }

    private void showReadToolBar() {
        if (mReadBottomBar.getVisibility() != View.VISIBLE) {
            mReadBottomBar.setVisibility(View.VISIBLE);
            mReadTopBar.setVisibility(View.VISIBLE);
            mReadTopBar.startAnimation(mTopInAnim);
            mReadBottomBar.startAnimation(mBottomInAnim);
            showSystemBar();
        }
    }

    private void setReadTheme(@Constant.ReadTheme int theme) {
        updateThemeView(theme);
        AppSettings.saveReadTheme(theme);
    }

    private void updateThemeView(@Constant.ReadTheme int theme) {
        mThemeWhiteView.setActivated(theme == Constant.ReadTheme.WHITE);
        mThemeBrownView.setActivated(theme == Constant.ReadTheme.BROWN);
        mThemeGreenView.setActivated(theme == Constant.ReadTheme.GREEN);
        updatePageTheme(theme);
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

    @OnCheckedChanged({ R.id.brightness_checkbox })
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        AppSettings.saveBrightnessSystem(checked);
        mBrightnessSeekBar.setEnabled(!checked);
        if (checked) {
            AppUtils.setScreenBrightness(-1, this);
        } else {
            AppUtils.setScreenBrightness(AppSettings.getBrightness(this), this);
        }
    }

    @OnClick({ R.id.brightness_min, R.id.brightness_max, R.id.auto_reader_view, R.id.text_size_dec, R.id.text_size_inc,
            R.id.more_setting_view, R.id.theme_white_view, R.id.theme_brown_view, R.id.theme_green_view, R.id.day_night_view,
            R.id.orientation_view, R.id.setting_view, R.id.download_view, R.id.toc_view, R.id.read_view_pager, R.id.read_bottom_bar })
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
            break;
        case R.id.theme_white_view:
            if (mPageManagers[0].getReadPage().getTheme() != Constant.ReadTheme.WHITE) {
                setReadTheme(Constant.ReadTheme.WHITE);
            }
            break;
        case R.id.theme_brown_view:
            if (mPageManagers[0].getReadPage().getTheme() != Constant.ReadTheme.BROWN) {
                setReadTheme(Constant.ReadTheme.BROWN);
            }
            break;
        case R.id.theme_green_view:
            if (mPageManagers[0].getReadPage().getTheme() != Constant.ReadTheme.GREEN) {
                setReadTheme(Constant.ReadTheme.GREEN);
            }
            break;
        case R.id.day_night_view:
            break;
        case R.id.orientation_view:
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
                    DownloadManager.Download download = DownloadManager.createDownload(which, mCurChapter, mChaptersList);
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

    private void updatePageTheme(int theme) {
        for (ReadPageManager page : mPageManagers) {
            page.getReadPage().setReadTheme(theme, false);
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
}
