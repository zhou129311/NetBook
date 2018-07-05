package com.xzhou.book.ui;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xzhou.book.R;
import com.xzhou.book.models.SearchInfo;

import java.util.List;

public class SearchAdapter extends BaseQuickAdapter<SearchInfo, BaseViewHolder> {

    public SearchAdapter(@Nullable List<SearchInfo> data) {
        super(R.layout.item_search_result_view, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SearchInfo item) {

    }

}
