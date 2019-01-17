package com.xzhou.book.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;

public class SettingsActivity extends BaseActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.settings);
    }


}
