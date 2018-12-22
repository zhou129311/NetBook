package com.xzhou.book.bookrack;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;

public class BookshelfAdapter extends BaseQuickAdapter<Entities.NetBook, BaseViewHolder> {

    public BookshelfAdapter() {
        super(R.layout.item_bookshelf_view, null);
    }

    @Override
    protected void convert(BaseViewHolder helper, Entities.NetBook item) {
        helper.setText(R.id.book_title, item.title)
                .setText(R.id.book_subhead, "13小时前:灌口二郎神显圣 第1037章...");
    }

}
