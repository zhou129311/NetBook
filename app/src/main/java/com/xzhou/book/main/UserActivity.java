package com.xzhou.book.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.xzhou.book.common.BaseActivity;

public class UserActivity extends BaseActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        super.finish();
    }
}
