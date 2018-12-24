package com.xzhou.book.find;

import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;

import static com.xzhou.book.utils.Constant.ITEM_TYPE_TEXT;
import static com.xzhou.book.utils.Constant.ITEM_TYPE_TEXT_GRID;
import static com.xzhou.book.utils.Constant.ITEM_TYPE_TEXT_IMAGE;
import static com.xzhou.book.utils.Constant.ITEM_TYPE_TEXT_IMAGE_2;

public class ExpandListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onClick(MultiItemEntity item);
    }

    ExpandListAdapter() {
        super(null);
        addItemType(ITEM_TYPE_TEXT, R.layout.item_text_view);
        addItemType(ITEM_TYPE_TEXT_IMAGE, R.layout.item_img_text_view);
        addItemType(ITEM_TYPE_TEXT_IMAGE_2, R.layout.item_img_text_view);
        addItemType(ITEM_TYPE_TEXT_GRID, R.layout.item_grid_text_view);
    }

    void setOnItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    protected void convert(final CommonViewHolder helper, final MultiItemEntity item) {
        switch (helper.getItemViewType()) {
        case ITEM_TYPE_TEXT:
            if (item instanceof Entities.RankLv0) {
                Entities.RankLv0 lv0 = (Entities.RankLv0) item;
                helper.setText(R.id.title_view, lv0.name)
                        .setTextColor(R.id.title_view, AppUtils.getColor(R.color.common_h1));
            } else if (item instanceof Entities.CategoryLv0) {
                Entities.CategoryLv0 lv0 = (Entities.CategoryLv0) item;
                helper.setText(R.id.title_view, lv0.title)
                        .setTextColor(R.id.title_view, AppUtils.getColor(R.color.common_h2));
            }
            break;
        case ITEM_TYPE_TEXT_IMAGE:
            updateRankLv1(helper, (Entities.RankLv1) item);
            break;
        case ITEM_TYPE_TEXT_IMAGE_2:
            Entities.RankLv2 lv2 = (Entities.RankLv2) item;
            helper.changeImageViewSize(R.id.click_image, 35, 35)
                    .setText(R.id.click_name, lv2.title)
                    .setVisible(R.id.click_image, false);
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onClick(item);
                    }
                }
            });
            break;
        case ITEM_TYPE_TEXT_GRID:
            Entities.CategoryLv1 lv1 = (Entities.CategoryLv1) item;
            helper.setText(R.id.grid_title, lv1.title)
                    .setText(R.id.grid_sub, AppUtils.getString(R.string.category_book_count, lv1.bookCount));
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onClick(item);
                    }
                }
            });
            break;
        }
    }

    private void updateRankLv1(final CommonViewHolder helper, final Entities.RankLv1 lv1) {
        helper.changeImageViewSize(R.id.click_image, 35, 35)
                .setText(R.id.click_name, lv1.title);
        if (lv1.hasSubItem()) {
            helper.setImageResource(R.id.click_image, R.mipmap.ic_rank_collapse)
                    .setImageResource(R.id.click_image_end, lv1.isExpanded() ? R.mipmap.rank_arrow_up : R.mipmap.rank_arrow_down)
                    .setVisible(R.id.click_image_end, true);
        } else {
            helper.setImageUrl(R.id.click_image, lv1.url(), R.mipmap.avatar_default)
                    .setVisible(R.id.click_image_end, false);
        }
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = helper.getAdapterPosition();
                if (lv1.hasSubItem()) {
                    if (lv1.isExpanded()) {
                        collapse(pos);
                    } else {
                        expand(pos);
                    }
                } else {
                    if (mItemClickListener != null) {
                        mItemClickListener.onClick(lv1);
                    }
                }
            }
        });
    }
}
