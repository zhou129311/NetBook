package com.xzhou.book.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.read.ReadPagerAdapter;
import com.xzhou.book.read.ReadViewPager;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant.ReadTheme;
import com.xzhou.book.utils.Log;

import butterknife.BindView;
import butterknife.OnClick;

public class TestActivity extends BaseActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        @ReadTheme int theme = AppSettings.getReadTheme();
        updateThemeView(theme);
        mReadViewPager.setBackgroundColor(AppUtils.getThemeColor(theme));
        mReadViewPager.setOnClickChangePageListener(new ReadViewPager.OnClickChangePageListener() {
            @Override
            public void onPrevious() {
                if (hideReadToolBar()) {
                    return;
                }
                Log.i("onPrevious");
            }

            @Override
            public void onNext() {
                if (hideReadToolBar()) {
                    return;
                }
                Log.i("onNext");
            }
        });
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle("我是标题");
        mToolbar.setBackgroundColor(getResources().getColor(R.color.reader_menu_bg_color));
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

    private void setReadTheme(@ReadTheme int theme) {
        updateThemeView(theme);
        AppSettings.saveReadTheme(theme);
    }

    private void updateThemeView(@ReadTheme int theme) {
        mThemeWhiteView.setActivated(theme == ReadTheme.WHITE);
        mThemeBrownView.setActivated(theme == ReadTheme.BROWN);
        mThemeGreenView.setActivated(theme == ReadTheme.GREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({ R.id.brightness_min, R.id.brightness_max, R.id.auto_reader_view, R.id.text_size_dec, R.id.text_size_inc,
            R.id.more_setting_view, R.id.theme_white_view, R.id.theme_brown_view, R.id.theme_green_view, R.id.day_night_view,
            R.id.orientation_view, R.id.setting_view, R.id.download_view, R.id.toc_view, R.id.read_view_pager })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.read_view_pager:
            if (!hideReadToolBar()) {
                mReadBottomBar.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.VISIBLE);
            }
            break;
        case R.id.brightness_min:
            break;
        case R.id.brightness_max:
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
            setReadTheme(ReadTheme.WHITE);
            break;
        case R.id.theme_brown_view:
            setReadTheme(ReadTheme.BROWN);
            break;
        case R.id.theme_green_view:
            setReadTheme(ReadTheme.GREEN);
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
            break;
        }
    }

    private boolean hideReadToolBar() {
        if (mReadBottomBar.getVisibility() == View.VISIBLE) {
            mReadBottomBar.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}
