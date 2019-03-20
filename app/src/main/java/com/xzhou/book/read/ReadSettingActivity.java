package com.xzhou.book.read;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.widget.SettingItemView;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ReadSettingActivity extends BaseActivity {

    @BindView(R.id.volume_turn_page_sw)
    SwitchCompat mVolumeTurnPageSw;
    @BindView(R.id.next_page_sw)
    SwitchCompat mNextPageSw;
    @BindView(R.id.full_screen_mode_sw)
    SwitchCompat mFullScreenModeSw;
    @BindView(R.id.screen_off_time_view)
    SettingItemView mScreenOffView;

    private String[] mScreenOffItems = new String[] {
            "常亮",
            "5分钟",
            "10分钟",
            "跟随系统",
    };

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ReadSettingActivity.class);
        ((Activity) context).startActivityForResult(intent, 2);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_setting);
        mVolumeTurnPageSw.setChecked(AppSettings.HAS_VOLUME_TURN_PAGE);
        mNextPageSw.setChecked(AppSettings.HAS_CLICK_NEXT_PAGE);
        mFullScreenModeSw.setChecked(AppSettings.HAS_FULL_SCREEN_MODE);
        mScreenOffView.setValue(mScreenOffItems[AppSettings.SCREEN_OFF_MODE]);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.read_settings);
    }

    @Override
    public void finish() {
        setResult(1, null);
        super.finish();
    }

    @OnClick({ R.id.screen_off_time_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.screen_off_time_view:
            ItemDialog.Builder builder = new ItemDialog.Builder(mActivity);
            builder.setTitle(R.string.screen_off_time)
                    .setSingleChoiceItems(mScreenOffItems, AppSettings.SCREEN_OFF_MODE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AppSettings.setScreenOffMode(which);
                            mScreenOffView.setValue(mScreenOffItems[which]);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            break;
        }
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
