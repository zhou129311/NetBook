package com.xzhou.book.read;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.WebFragment;

public class ReadWebActivity extends BaseActivity {
    private static final String TAG = "ReadWebActivity";

    private WebFragment mWebFragment;

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, ReadWebActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.web_content, getFragment(), "web");
        ft.commitAllowingStateLoss();
    }

    private Fragment getFragment() {
        mWebFragment = new WebFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", "https://m.boluoxs.com/book/271.html");
        mWebFragment.setArguments(bundle);
        return mWebFragment;
    }

    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (mWebFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
