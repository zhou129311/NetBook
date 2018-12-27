package com.xzhou.book.find;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.CommonViewHolder;

import java.util.List;

public class BookListDetailAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

    public BookListDetailAdapter(List<MultiItemEntity> data) {
        super(data);
    }

    @Override
    protected void convert(CommonViewHolder holder, MultiItemEntity item) {

    }
}
