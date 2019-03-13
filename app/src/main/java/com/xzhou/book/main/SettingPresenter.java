package com.xzhou.book.main;

import android.text.format.Formatter;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingPresenter extends BasePresenter<SettingContract.View> implements SettingContract.Presenter {

    private long mCacheSize;
    private String mCachePath;
    private String mFilePath;
    private ExecutorService mSinglePool = Executors.newSingleThreadExecutor();

    SettingPresenter(SettingContract.View view) {
        super(view);
        mCachePath = FileUtils.getCachePath(MyApp.getContext());
        mFilePath = FileUtils.getFilePath(MyApp.getContext());
        Log.i("SettingPresenter", "mCachePath = " + mCachePath + ",mFilePath = " + mFilePath);
    }

    @Override
    public boolean start() {
        onCacheLoading();
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                mCacheSize = FileUtils.getFolderSize(mCachePath) + FileUtils.getFolderSize(mFilePath);
                String value = Formatter.formatFileSize(MyApp.getContext(), mCacheSize);
                updateCacheSize(value);
            }
        });
        return super.start();
    }

    @Override
    public void clearCache() {
        if (mCacheSize <= 0) {
            ToastUtils.showShortToast("没有缓存");
            return;
        }
        onCacheLoading();
        mSinglePool.execute(new Runnable() {
            @Override
            public void run() {
                File bookDir = new File(mFilePath);
                File[] books = bookDir.listFiles();
                if (books != null) {
                    for (File file : books) {
                        if(file != null){
                            String bookId = file.getName();
                            AppSettings.deleteChapterList(bookId);
                        }
                    }
                }
                FileUtils.deleteFileOrDirectory(new File(mCachePath));
                FileUtils.deleteFileOrDirectory(bookDir);
                mCacheSize = FileUtils.getFolderSize(mCachePath) + FileUtils.getFolderSize(mFilePath);
                String value = Formatter.formatFileSize(MyApp.getContext(), mCacheSize);
                updateCacheSize(value);
            }
        });
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

    private void onCacheLoading() {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onCacheLoading();
                }
            }
        });
    }
}
