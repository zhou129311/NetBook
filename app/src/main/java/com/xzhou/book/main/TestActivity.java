package com.xzhou.book.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.read.ReadViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestActivity extends BaseActivity {

    @BindView(R.id.read_view_pager)
    ReadViewPager mReadViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        ButterKnife.bind(this);
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

}
