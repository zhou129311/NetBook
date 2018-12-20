package com.xzhou.book.ui.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;

import java.util.List;

public class TabFragment extends BaseFragment<TabContract.Presenter> implements TabContract.View {
    private static String TAG = "TabFragment";

    private int mSource;
    private Entities.TabData mTabData;

    public static TabFragment newInstance(Entities.TabData data) {
        TabFragment fragment = new TabFragment();
        fragment.setTabData(data);
        return fragment;
    }

    public void setTabData(Entities.TabData data) {
        mTabData = data;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_common_tab;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void setPresenter(TabContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {

    }

    @Override
    public void onLoadMore(List<MultiItemEntity> list) {

    }
}
