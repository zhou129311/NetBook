package com.xzhou.book.common;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ImageLoader;

public class TabAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int NET_BOOK_ITEM = 1;

    private int mPosition;

    public TabAdapter(int position) {
        super(null);
        mPosition = position;
        addItemType(NET_BOOK_ITEM, R.layout.item_net_book_view);
    }

    @Override
    protected void convert(BaseViewHolder holder, MultiItemEntity item) {
        switch (holder.getItemViewType()) {
        case NET_BOOK_ITEM:
            final Entities.NetBook netBook = (Entities.NetBook) item;
            if (AppUtils.isEmpty(netBook.cat)) {
                netBook.cat = "";
            }
            holder.setText(R.id.book_title, netBook.title)
                    .setText(R.id.book_author, AppUtils.getString(R.string.net_book_author, netBook.author, netBook.cat))
                    .setText(R.id.book_describe, netBook.shortIntro)
                    .setText(R.id.book_save_count, AppUtils.getString(R.string.net_book_save_count, netBook.latelyFollower, String.valueOf(netBook.retentionRatio)));
            ImageLoader.showImageUrl(getRecyclerView().getContext(), (ImageView) holder.getView(R.id.book_image)
                    , netBook.cover(), R.mipmap.ic_cover_default);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getRecyclerView().getContext(), netBook._id);
                }
            });
            break;

        }
    }
}
