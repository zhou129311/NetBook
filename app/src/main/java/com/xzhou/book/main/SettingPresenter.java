package com.xzhou.book.main;

import android.text.format.Formatter;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.io.File;

public class SettingPresenter extends BasePresenter<SettingContract.View> implements SettingContract.Presenter {

    private long mCacheSize;
    private String mCachePath;

    SettingPresenter(SettingContract.View view) {
        super(view);
        mCachePath = FileUtils.getCachePath(MyApp.getContext());
    }

    @Override
    public boolean start() {
        mCacheSize = FileUtils.getFolderSize(mCachePath);
        String value = Formatter.formatFileSize(MyApp.getContext(), mCacheSize);
        updateCacheSize(value);
        return super.start();
    }

    @Override
    public void clearCache() {
        if (mCacheSize <= 0) {
            ToastUtils.showShortToast("没有缓存");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteFileOrDirectory(new File(mCachePath));
                mCacheSize = FileUtils.getFolderSize(mCachePath);
                String value = Formatter.formatFileSize(MyApp.getContext(), mCacheSize);
                updateCacheSize(value);
            }
        }).start();
    }

    private void updateCacheSize(final String value) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    Log.d("updateCacheSize::" + value);
                    mView.updateCacheSize(value);
                }
            }
        });
    }
}
