package com.xzhou.book.ui.find;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ImageLoader;

public class SortListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private static final String TAG = "SortListAdapter";

    public static final int TEXT = 0;
    public static final int TEXT_IMAGE = 1;
    public static final int TEXT_IMAGE_2 = 2;
    public static final int TEXT_GRID = 3;

    private Context mContext;
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onClick(MultiItemEntity item);
    }

    public SortListAdapter(Context context) {
        super(null);
        mContext = context;
        addItemType(TEXT, R.layout.item_text_view);
        addItemType(TEXT_IMAGE, R.layout.item_click_view);
        addItemType(TEXT_IMAGE_2, R.layout.item_click_view);
        addItemType(TEXT_GRID, R.layout.item_grid_text_view);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final MultiItemEntity item) {
        switch (helper.getItemViewType()) {
        case TEXT:
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
        case TEXT_IMAGE:
            updateRankLv1(helper, (Entities.RankLv1) item);
            break;
        case TEXT_IMAGE_2:
            Entities.RankLv2 lv2 = (Entities.RankLv2) item;
            ImageLoader.changeImageViewSize((ImageView) helper.getView(R.id.click_image), 35, 35);
            helper.setText(R.id.click_name, lv2.title)
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
        case TEXT_GRID:
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

    private void updateRankLv1(final BaseViewHolder helper, final Entities.RankLv1 lv1) {
        helper.setText(R.id.click_name, lv1.title);
        ImageView imageView = helper.getView(R.id.click_image);
        ImageLoader.changeImageViewSize(imageView, 35, 35);
        if (lv1.hasSubItem()) {
            helper.setImageResource(R.id.click_image, R.mipmap.ic_rank_collapse)
                    .setImageResource(R.id.click_image_end, lv1.isExpanded() ? R.mipmap.rank_arrow_up : R.mipmap.rank_arrow_down)
                    .setVisible(R.id.click_image_end, true);
        } else {
            helper.setVisible(R.id.click_image_end, false);
            ImageLoader.showImageUrl(mContext, imageView, lv1.url(), R.mipmap.avatar_default);
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
