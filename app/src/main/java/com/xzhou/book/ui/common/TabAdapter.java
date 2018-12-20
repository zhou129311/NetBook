package com.xzhou.book.ui.common;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

public class TabAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public TabAdapter(@Nullable List<MultiItemEntity> data) {
        super(data);
    }

    @Override
    protected void convert(BaseViewHolder holder, MultiItemEntity item) {

    }
}
