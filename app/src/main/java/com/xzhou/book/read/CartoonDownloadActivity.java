package com.xzhou.book.read;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.db.BookProvider;

public class CartoonDownloadActivity extends BaseActivity {

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        Intent intent = new Intent(context, CartoonDownloadActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
