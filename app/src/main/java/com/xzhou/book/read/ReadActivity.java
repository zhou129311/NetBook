package com.xzhou.book.read;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xzhou.book.BookManager;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonDialog;
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

public class ReadActivity extends BaseActivity<ReadContract.Presenter> implements ReadContract.View {
    private static final String TAG = "ReadActivity";
    //    @BindView(R.id.read_rl_view)
//    RelativeLayout mMainLayout;
//    @BindView(R.id.end_ll_view)
//    LinearLayout mEndSlideView;
    @BindView(R.id.read_dl_slide)
    SwipeLayout mSwipeLayout;
    @BindView(R.id.read_view_pager)
    ReadViewPager mReadViewPager;

    @BindView(R.id.read_abl_top_menu)
    AppBarLayout mReadTopBar;
    @BindView(R.id.read_bottom_bar)
    LinearLayout mReadBottomBar;

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

    private BookManager.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private CommonDialog mBookTocDialog;
    private ReadPageManager[] mPageManagers = new ReadPageManager[3];
    private int mCurChapter;
    private int mPrePosition;

    public static void startActivity(Context context, BookManager.LocalBook book) {
        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra("localBook", book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            }

            @Override
            public void onClose() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryReceiver);
    }

    private void initMenuAnim() {
        if (mTopInAnim != null) return;
        mReadTopBar.setPadding(0, AppUtils.getStatusBarHeight(), 0, 0);

        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mBook = getIntent().getParcelableExtra("localBook");
        if (mBook == null) {
            mBook = new BookManager.LocalBook();
            mBook._id = "591ed23b1861e2e332db308e";
            mBook.title = "超神机械师";
//            ToastUtils.showShortToast("出现错误，打开失败");
//            finish();
//            return;
        }
        mToolbar.setTitle(mBook.title);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.reader_menu_bg_color));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        if (mBook != null && !mBook.isBookshelf) {
            AppUtils.deleteBookCache(mBook._id);
        }
        if (mBookTocDialog != null) {
            mBookTocDialog.dismiss();
            mBookTocDialog = null;
        }
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
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            hideReadToolBar();
            return true;
        }
        switch (item.getItemId()) {
        case R.id.menu_community:
            return true;
        case R.id.menu_change_source:
            return true;
        case R.id.menu_change_mode:
            return true;
        case R.id.menu_book_detail:
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

    private void initReadPageView() {
        for (int i = 0; i < mPageManagers.length; i++) {
            final ReadPage page = new ReadPage(this);
            final int position = i;
            page.setOnReloadListener(new ReadPage.OnReloadListener() {
                @Override
                public void onReload() {
                    mPresenter.reloadCurPage(position);
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
                int maxLineCount = page.mChapterContent.getMaxLineCount();
                int width = page.mChapterContent.getWidth();
                PageLines pageLines = page.getPageContent() == null ? null : page.getPageContent().mPageLines;
                mPresenter.setTextViewParams(maxLineCount, page.mChapterContent.getPaint(), width, pageLines);
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
            }

            @Override
            public void onNext() {
                if (hideReadToolBar()) {
                    return;
                }
                int curPos = mReadViewPager.getCurrentItem();
                if (curPos >= 2) {
                    return;
                }
                ReadPage nextPage = mPageManagers[curPos + 1].getReadPage();
                if (nextPage.isPageEnd()) {
                    ToastUtils.showShortToast("没有下一页了");
                    return;
                }
                mReadViewPager.setCurrentItem(curPos + 1, false);
            }
        });
        mReadViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected:mPrePosition=" + mPrePosition + " ,position=" + position);
                mPageManagers[position].getReadPage().checkLoading();
                hideReadToolBar();
                if (position > mPrePosition) {
                    mPresenter.loadNextPage(position);
                } else if (position < mPrePosition) {
                    mPresenter.loadPreviousPage(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mReadViewPager.setCanTouch(false);
        mReadViewPager.setAdapter(new MyPagerAdapter());
        mReadViewPager.setCurrentItem(0, false);
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
    public void onBackPressed() {
        if (mSwipeLayout.isMenuOpen()) {
            mSwipeLayout.smoothToCloseMenu();
            hideReadToolBar();
        } else {
            super.onBackPressed();
        }
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
        case R.id.auto_reader_view:
            break;
        case R.id.text_size_dec:
            break;
        case R.id.text_size_inc:
            break;
        case R.id.more_setting_view:
            break;
        case R.id.theme_white_view:
            setReadTheme(Constant.ReadTheme.WHITE);
            break;
        case R.id.theme_brown_view:
            setReadTheme(Constant.ReadTheme.BROWN);
            break;
        case R.id.theme_green_view:
            setReadTheme(Constant.ReadTheme.GREEN);
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
            break;
        case R.id.toc_view:
            if (mChaptersList == null) {
                ToastUtils.showShortToast("未找到章节列表");
                return;
            }
            if (mBookTocDialog == null) {
                List<Entities.Chapters> list = mChaptersList.subList(0, 3);
                final BookTocDialog dialog = BookTocDialog.createDialog(this, list, mBook);
                dialog.setCurChapter(mCurChapter);
                dialog.setOnItemClickListener(new BookTocDialog.OnItemClickListener() {
                    @Override
                    public void onClickItem(int chapter, Entities.Chapters chapters) {

                    }
                });
                mBookTocDialog = CommonDialog.newInstance(new CommonDialog.OnCallDialog() {
                    @Override
                    public Dialog getDialog(Context context) {
                        return dialog;
                    }
                }, true);
            }
            mBookTocDialog.show(getSupportFragmentManager(), "TocDialog");
            if (mBookTocDialog.getDialog() != null) {
                ((BookTocDialog) mBookTocDialog.getDialog()).setCurChapter(mCurChapter);
            }
            break;
        }
    }

    private void updatePageTheme(int theme) {
        for (ReadPageManager page : mPageManagers) {
            page.getReadPage().setReadTheme(theme);
        }
    }

    private void updateBattery(Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); //电量最大值
            int curBattery = (level / scale) * 100;
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
            View view = mPageManagers[position].getPageView();
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }
    }
}
