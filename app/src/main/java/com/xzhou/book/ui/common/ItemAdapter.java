package com.xzhou.book.ui.common;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;

import java.util.List;

public class ItemAdapter extends BaseQuickAdapter<Entities.ItemClick, BaseViewHolder> {
    private boolean mIsShowEndView;

    public ItemAdapter(@Nullable List<Entities.ItemClick> data, boolean isShowEndView) {
        super(R.layout.item_click_view, data);
        mIsShowEndView = isShowEndView;
    }

    @Override
    protected void convert(BaseViewHolder helper, Entities.ItemClick item) {
        helper.setText(R.id.click_name, item.name)
                .setImageResource(R.id.click_image, item.resId)
                .setVisible(R.id.click_image_end, mIsShowEndView);
    }
}
