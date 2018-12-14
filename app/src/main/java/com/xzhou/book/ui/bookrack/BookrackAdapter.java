package com.xzhou.book.ui.bookrack;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xzhou.book.models.Entities;

import java.util.List;

public class BookrackAdapter extends BaseQuickAdapter<Entities.Recommend.RecommendBook, BaseViewHolder> {


    public BookrackAdapter(int layoutResId, @Nullable List<Entities.Recommend.RecommendBook> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Entities.Recommend.RecommendBook item) {

    }

}
