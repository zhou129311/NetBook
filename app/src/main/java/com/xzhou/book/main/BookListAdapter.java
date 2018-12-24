package com.xzhou.book.main;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.common.CommonViewHolder;

import java.util.List;

public class BookListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

    public BookListAdapter(List<MultiItemEntity> data) {
        super(data);
    }

    @Override
    protected void convert(CommonViewHolder holder, MultiItemEntity item) {

    }
}
