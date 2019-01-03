package com.xzhou.book.read;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xzhou.book.common.BaseActivity;

public class ReadActivity extends BaseActivity<ReadContract.Presenter> implements ReadContract.View {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected ReadContract.Presenter createPresenter() {
        return new ReadPresenter(this);
    }

    @Override
    public void setPresenter(ReadContract.Presenter presenter) {
    }
}
