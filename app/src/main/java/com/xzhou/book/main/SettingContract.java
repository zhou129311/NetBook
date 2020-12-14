package com.xzhou.book.main;

import com.xzhou.book.common.BaseContract;

import java.io.File;
import java.util.List;

public interface SettingContract {

    interface Presenter extends BaseContract.Presenter {
        void clearCache();
    }

    interface View extends BaseContract.View<Presenter> {

        void updateCacheSize(String value);

        void onCacheLoading();

        void updateCrashFiles(List<File> list);
    }
}
