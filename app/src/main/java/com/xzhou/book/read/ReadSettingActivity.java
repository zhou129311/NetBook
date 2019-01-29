package com.xzhou.book.read;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.utils.AppSettings;

import butterknife.BindView;
import butterknife.OnCheckedChanged;

public class ReadSettingActivity extends BaseActivity {

    @BindView(R.id.volume_turn_page_sw)
    SwitchCompat mVolumeTurnPageSw;
    @BindView(R.id.next_page_sw)
    SwitchCompat mNextPageSw;
    @BindView(R.id.full_screen_mode_sw)
    SwitchCompat mFullScreenModeSw;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ReadSettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_setting);
        mVolumeTurnPageSw.setChecked(AppSettings.HAS_VOLUME_TURN_PAGE);
        mNextPageSw.setChecked(AppSettings.HAS_CLICK_NEXT_PAGE);
        mFullScreenModeSw.setChecked(AppSettings.HAS_FULL_SCREEN_MODE);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.read_settings);
    }

    @OnCheckedChanged({ R.id.volume_turn_page_sw, R.id.next_page_sw, R.id.full_screen_mode_sw })
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        switch (button.getId()) {
        case R.id.volume_turn_page_sw:
            AppSettings.saveVolumeTurnPage(checked);
            break;
        case R.id.next_page_sw:
            AppSettings.saveClickNextPage(checked);
            break;
        case R.id.full_screen_mode_sw:
            AppSettings.saveFullScreenMode(checked);
            break;
        }
    }
}
