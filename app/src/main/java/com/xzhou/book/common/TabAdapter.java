package com.xzhou.book.common;

import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;

import java.util.List;

public class TabAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

    private int mPosition;

    TabAdapter(int position) {
        super(null);
        mPosition = position;
        addItemType(Constant.ITEM_TYPE_NET_BOOK, R.layout.item_net_book_view);
        addItemType(Constant.ITEM_TYPE_BOOK_BY_AUTHOR, R.layout.item_search_result_view);
        addItemType(Constant.ITEM_TYPE_BOOK_BY_TAG, R.layout.item_tag_book_view);
    }

    @Override
    protected void convert(CommonViewHolder holder, MultiItemEntity item) {
        switch (holder.getItemViewType()) {
        case Constant.ITEM_TYPE_NET_BOOK:
            final Entities.NetBook netBook = (Entities.NetBook) item;
            if (AppUtils.isEmpty(netBook.cat)) {
                netBook.cat = "";
                if (netBook instanceof Entities.BooksByCats.CatBook) {
                    netBook.cat = ((Entities.BooksByCats.CatBook) netBook).majorCate;
                }
            }
            holder.setRoundImageUrl(R.id.book_image, netBook.cover(), R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, netBook.title)
                    .setText(R.id.book_author, AppUtils.getString(R.string.net_book_author, netBook.author, netBook.cat))
                    .setText(R.id.book_describe, netBook.shortIntro)
                    .setText(R.id.book_save_count, AppUtils.getString(R.string.net_book_save_count,
                            netBook.latelyFollower, String.valueOf(netBook.retentionRatio)));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getRecyclerView().getContext(), netBook._id);
                }
            });
            break;
        case Constant.ITEM_TYPE_BOOK_BY_AUTHOR: {
            final Entities.BooksByTag.TagBook tagBook = (Entities.BooksByTag.TagBook) item;
            holder.setRoundImageUrl(R.id.book_image, tagBook.cover(), R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, tagBook.title)
                    .setText(R.id.book_h2, AppUtils.getString(R.string.search_result_h2, tagBook.latelyFollower,
                            String.valueOf(tagBook.retentionRatio), tagBook.author));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getRecyclerView().getContext(), tagBook._id);
                }
            });
            break;
        }
        case Constant.ITEM_TYPE_BOOK_BY_TAG: {
            final Entities.BooksByTag.TagBook tagBook = (Entities.BooksByTag.TagBook) item;
            holder.setRoundImageUrl(R.id.book_image, tagBook.cover(), R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, tagBook.title)
                    .setText(R.id.book_h2, AppUtils.isEmpty(tagBook.shortIntro) ? AppUtils.getString(R.string.detail_no_intro) : tagBook.shortIntro)
                    .setText(R.id.book_h3, formatTags(tagBook.tags));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getRecyclerView().getContext(), tagBook._id);
                }
            });
            break;
        }
        }
    }

    private static String formatTags(List<String> tags) {
        StringBuilder sb = new StringBuilder();
        int size = tags.size();
        for (int i = 0; i < size; i++) {
            String tag = tags.get(i);
            if (i == 0) {
                sb.append(tag);
            } else {
                sb.append(" | ").append(tag);
            }
        }
        return sb.toString();
    }
}
