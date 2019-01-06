package com.xzhou.book.read;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xzhou.book.BookManager;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ReadActivity extends BaseActivity<ReadContract.Presenter> implements ReadContract.View {
    private static final String TAG = "ReadActivity";
    @BindView(R.id.read_view_pager)
    ReadViewPager mReadViewPager;

    @BindView(R.id.read_bottom_bar)
    LinearLayout mReadBottomBar;

    @BindView(R.id.read_setting_layout)
    ConstraintLayout mReadSettingLayout;
    @BindView(R.id.brightness_min)
    ImageView mBrightnessMin;
    @BindView(R.id.brightness_seek_bar)
    SeekBar mBrightnessSeekBar;
    @BindView(R.id.brightness_max)
    ImageView mBrightnessMax;
    @BindView(R.id.brightness_checkbox)
    CheckBox mBrightnessCheckbox;
    @BindView(R.id.auto_reader_view)
    TextView mAutoReaderView;
    @BindView(R.id.text_size_dec)
    ImageView mTextSizeDec;
    @BindView(R.id.text_size_inc)
    ImageView mTextSizeInc;
    @BindView(R.id.more_setting_view)
    TextView mMoreSettingView;
    @BindView(R.id.theme_white_view)
    ImageView mThemeWhiteView;
    @BindView(R.id.theme_brown_view)
    ImageView mThemeBrownView;
    @BindView(R.id.theme_green_view)
    ImageView mThemeGreenView;

    @BindView(R.id.day_night_view)
    TextView mDayNightView;
    @BindView(R.id.orientation_view)
    TextView mOrientationView;
    @BindView(R.id.setting_view)
    TextView mSettingView;
    @BindView(R.id.download_view)
    TextView mDownloadView;
    @BindView(R.id.toc_view)
    TextView mTocView;

    private ReadPagerAdapter mPagerAdapter;
    private BookManager.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;
    private BookTocDialog mBookTocDialog;
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
        mToolbar.setVisibility(View.GONE);
        mReadBottomBar.setVisibility(View.GONE);
        initBrightness();
        initReadPageView();
        updateThemeView(AppSettings.getReadTheme());
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(mBatteryReceiver, filter);
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); //电量最大值
            int curBattery = (level / scale) * 100;
            mPagerAdapter.setBattery(curBattery);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryReceiver);
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
    protected void onStart() {
        super.onStart();
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
    public void initChapterList(List<Entities.Chapters> list, PageContent pageContent, String chapterTitle,
                                String pageNumber, @ReadPresenter.Error int error) {
        mChaptersList = list;
        if (list != null && list.size() > 0) {
            mReadViewPager.setScanTouch(true);
        }
        Log.i(TAG, "initChapterList:" + chapterTitle + "," + pageNumber + "," + error);
        ReadPager curPage = mPagerAdapter.getItem(mReadViewPager.getCurrentItem());
        curPage.setPageContent(pageContent, chapterTitle, pageNumber, error);
    }

    @Override
    public void onUpdateNextPage(PageContent pageContent, String chapterTitle, String pageNumber, @ReadPresenter.Error int error) {
        int curPos = mReadViewPager.getCurrentItem();
        if (curPos < Integer.MAX_VALUE) {
            ReadPager nextPage = mPagerAdapter.getItem(curPos + 1);
            nextPage.setPageContent(pageContent, chapterTitle, pageNumber, error);
        }
    }

    @Override
    public void onUpdatePrePage(PageContent pageContent, String chapterTitle, String pageNumber, @ReadPresenter.Error int error) {
        int curPos = mReadViewPager.getCurrentItem();
        if (curPos > 0) {
            ReadPager prePage = mPagerAdapter.getItem(curPos - 1);
            prePage.setPageContent(pageContent, chapterTitle, pageNumber, error);
        }
    }

    private void initReadPageView() {
        List<ReadPager> pagers = new ArrayList<>();
        final ReadPager pager = new ReadPager(this);
        pager.setReadPageListener(new ReadPager.ReadPageListener() {
            @Override
            public void onInit() {
                int maxLineCount = pager.mChapterContent.getMaxLineCount();
                int width = pager.mChapterContent.getWidth();
                Log.i(TAG, "onInit:" + maxLineCount + "," + width);
                mPresenter.setTextViewParams(maxLineCount, pager.mChapterContent.getPaint(), width);
                mPresenter.start();
            }

            @Override
            public void onRetryLoad() {

            }
        });
        pagers.add(pager);
        pagers.add(new ReadPager(this));
        pagers.add(new ReadPager(this));
        mPagerAdapter = new ReadPagerAdapter(pagers);
        mReadViewPager.setOffscreenPageLimit(3);
        mReadViewPager.setOnClickChangePageListener(new ReadViewPager.OnClickChangePageListener() {
            @Override
            public void onPrevious() {
                if (hideReadToolBar()) {
                    return;
                }
                Log.i("onPrevious");
                int curPos = mReadViewPager.getCurrentItem();
                if (curPos <= 0) {
                    ToastUtils.showShortToast("没有上一页了");
                    return;
                }
                mReadViewPager.setCurrentItem(curPos - 1, false);
            }

            @Override
            public void onNext() {
                if (hideReadToolBar()) {
                    return;
                }
                Log.i("onNext");
                int curPos = mReadViewPager.getCurrentItem();
                ReadPager nextPage = mPagerAdapter.getItem(curPos + 1);
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
                Log.i(TAG, "onPageSelected::position = " + position + ",mPrePosition = " + mPrePosition);
                if (position > 0) {
                    mPagerAdapter.getItem(position - 1).reset();
                }
                mPagerAdapter.getItem(position + 1).reset();
                if (mPrePosition > position) {
                    mPresenter.previous();
                } else if (mPrePosition < position) {
                    mPresenter.next();
                }
                mPrePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mReadViewPager.setScanTouch(false);
        mReadViewPager.setAdapter(mPagerAdapter);
        mReadViewPager.setCurrentItem(0);
        ReadPager curPager = mPagerAdapter.getItem(0);
        mPrePosition = 0;
        curPager.setLoadState(true);
    }

    private boolean hideReadToolBar() {
        if (mReadBottomBar.getVisibility() == View.VISIBLE) {
            AppUtils.setFullScreen(true, this);
            mReadSettingLayout.setVisibility(View.GONE);
            mReadBottomBar.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private void setReadTheme(@Constant.ReadTheme int theme) {
        updateThemeView(theme);
        AppSettings.saveReadTheme(theme);
    }

    private void updateThemeView(@Constant.ReadTheme int theme) {
        mThemeWhiteView.setActivated(theme == Constant.ReadTheme.WHITE);
        mThemeBrownView.setActivated(theme == Constant.ReadTheme.BROWN);
        mThemeGreenView.setActivated(theme == Constant.ReadTheme.GREEN);
        mPagerAdapter.setReadTheme(theme);
    }

    private void initBrightness() {
        boolean isSystem = AppSettings.isBrightnessSystem();
        mBrightnessCheckbox.setChecked(isSystem);
        mBrightnessSeekBar.setEnabled(!isSystem);
        mBrightnessSeekBar.setMax(100);
        mBrightnessSeekBar.setProgress(AppSettings.getBrightness());
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
            AppUtils.setScreenBrightness(AppSettings.getBrightness(), this);
        }
    }

    @OnClick({R.id.brightness_min, R.id.brightness_max, R.id.auto_reader_view, R.id.text_size_dec, R.id.text_size_inc,
            R.id.more_setting_view, R.id.theme_white_view, R.id.theme_brown_view, R.id.theme_green_view, R.id.day_night_view,
            R.id.orientation_view, R.id.setting_view, R.id.download_view, R.id.toc_view, R.id.read_view_pager, R.id.read_bottom_bar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.read_view_pager:
            if (!hideReadToolBar()) {
                AppUtils.setFullScreen(false, this);
                mReadBottomBar.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.VISIBLE);
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
            ReadPager curPage = mPagerAdapter.getItem(mReadViewPager.getCurrentItem());
            if (mBookTocDialog == null) {
                mBookTocDialog = BookTocDialog.createDialog(this, mChaptersList, mBook);
                mBookTocDialog.setOnItemClickListener(new BookTocDialog.OnItemClickListener() {
                    @Override
                    public void onClickItem(int chapter, Entities.Chapters chapters) {

                    }
                });
            }
            PageContent pageContent = curPage.getPageContent();
            mBookTocDialog.setCurChapter(pageContent == null ? 0 : pageContent.chapter);
            mBookTocDialog.show();
            break;
        }
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100); //电量最大值
                int curBattery = (level / scale) * 100;
                mPagerAdapter.setBattery(curBattery);
            }
        }
    };
}
