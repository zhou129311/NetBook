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
import com.xzhou.book.read.ReadViewPager;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.brightness_min, R.id.brightness_max, R.id.auto_reader_view, R.id.text_size_dec, R.id.text_size_inc, R.id.more_setting_view, R.id.theme_white_view, R.id.theme_brown_view, R.id.theme_green_view, R.id.day_night_view, R.id.orientation_view, R.id.setting_view, R.id.download_view, R.id.toc_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
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
            mThemeWhiteView.setActivated(true);
            mThemeBrownView.setActivated(false);
            mThemeGreenView.setActivated(false);
            break;
        case R.id.theme_brown_view:
            mThemeWhiteView.setActivated(false);
            mThemeBrownView.setActivated(true);
            mThemeGreenView.setActivated(false);
            break;
        case R.id.theme_green_view:
            mThemeWhiteView.setActivated(false);
            mThemeBrownView.setActivated(false);
            mThemeGreenView.setActivated(true);
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
}
