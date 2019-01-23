package com.xzhou.book.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.widget.SettingItemView;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.book_sort_view)
    SettingItemView mBookSortView;
    @BindView(R.id.book_read_dl_view)
    SettingItemView mBookReadDlView;
    @BindView(R.id.clear_cache_view)
    SettingItemView mClearCacheView;
    @BindView(R.id.saving_traffic_cb)
    SwitchCompat mSavingTrafficCb;

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

    @OnClick({ R.id.book_sort_view, R.id.book_read_dl_view, R.id.clear_cache_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.book_sort_view:
            String[] items = new String[] {
                    getString(R.string.bookshelf_sort_add),
                    getString(R.string.bookshelf_sort_read),
                    getString(R.string.bookshelf_sort_update),
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.bookshelf_sort_dialog)
                    .setSingleChoiceItems(items, AppSettings.getBookshelfOrder(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppSettings.saveBookshelfOrder(which);
                            BookManager.get().reloadList();
                            dialog.dismiss();
                        }
                    }).show();
            break;
        case R.id.book_read_dl_view:
            break;
        case R.id.clear_cache_view:
            break;
        }
    }
}
