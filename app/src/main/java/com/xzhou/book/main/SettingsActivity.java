package com.xzhou.book.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.widget.SettingItemView;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity<SettingContract.Presenter> implements SettingContract.View {

    @BindView(R.id.book_sort_view)
    SettingItemView mBookSortView;
    @BindView(R.id.book_read_dl_view)
    SettingItemView mBookReadDlView;
    @BindView(R.id.clear_cache_view)
    SettingItemView mClearCacheView;
    @BindView(R.id.saving_traffic_cb)
    SwitchCompat mSavingTrafficCb;

    private String[] mSortItems = new String[] {
            AppUtils.getString(R.string.bookshelf_sort_add),
            AppUtils.getString(R.string.bookshelf_sort_read),
            AppUtils.getString(R.string.bookshelf_sort_update),
    };

    private String[] mCacheItems = new String[] {
            AppUtils.getString(R.string.download_read_book_none),
            AppUtils.getString(R.string.download_read_book_1),
            AppUtils.getString(R.string.download_read_book_5),
            AppUtils.getString(R.string.download_read_book_10),
    };

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mBookSortView.setValue(mSortItems[AppSettings.BOOK_ORDER]);
        mBookReadDlView.setValue(mCacheItems[AppSettings.READ_CACHE_MODE]);
        mSavingTrafficCb.setChecked(AppSettings.HAS_SAVING_TRAFFIC);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    protected SettingContract.Presenter createPresenter() {
        return new SettingPresenter(this);
    }

    @OnCheckedChanged({ R.id.saving_traffic_cb })
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        AppSettings.setSavingTraffic(checked);
    }

    @OnClick({ R.id.book_sort_view, R.id.book_read_dl_view, R.id.clear_cache_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.book_sort_view: {
            ItemDialog.Builder builder = new ItemDialog.Builder(mActivity);
            builder.setTitle(R.string.bookshelf_sort_dialog)
                    .setSingleChoiceItems(mSortItems, AppSettings.getBookshelfOrder(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppSettings.saveBookshelfOrder(which);
                            mBookSortView.setValue(mSortItems[which]);
                            BookManager.get().reloadList();
                            dialog.dismiss();
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
        case R.id.book_read_dl_view: {
            ItemDialog.Builder builder = new ItemDialog.Builder(mActivity);
            builder.setTitle(R.string.download_read_book)
                    .setSingleChoiceItems(mCacheItems, AppSettings.getReadCacheMode(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppSettings.saveReadCacheMode(which);
                            mBookReadDlView.setValue(mCacheItems[which]);
                            dialog.dismiss();
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
        case R.id.clear_cache_view:
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.clear_cache_dialog_title)
                    .setMessage(R.string.clear_cache_dialog_message)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mPresenter.clearCache();
                            mClearCacheView.setEnabled(false);
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

    @Override
    public void updateCacheSize(String value) {
        mClearCacheView.setValue(value);
        mClearCacheView.setEnabled(true);
    }

    @Override
    public void onCacheLoading() {
        mClearCacheView.setValue("加载中...");
        mClearCacheView.setEnabled(false);
    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
    }
}
