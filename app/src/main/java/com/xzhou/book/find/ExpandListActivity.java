package com.xzhou.book.find;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.TabActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Constant.TabSource;
import com.xzhou.book.utils.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ExpandListActivity extends BaseActivity<ExpandListContract.Presenter> implements ExpandListContract.View {
    private static final String TAG = "ExpandListActivity";

    private static final String EXTRA_SOURCE = "extra_source";

    public static void startActivity(Activity activity, int source) {
        Intent intent = new Intent(activity, ExpandListActivity.class);
        intent.putExtra(EXTRA_SOURCE, source);
        activity.startActivity(intent);
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    private ExpandListAdapter mAdapter;
    private int mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mAdapter = new ExpandListAdapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.setOnItemClickListener(new ExpandListAdapter.ItemClickListener() {
            @Override
            public void onClick(MultiItemEntity item) {
                clickItem(item);
            }
        });

        mRecyclerView.setHasFixedSize(true);
        if (mSource == ExpandListPresenter.SOURCE_CATEGORY) {
            MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(this, 3);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return mAdapter.getItemViewType(position) == Constant.ITEM_TYPE_TEXT_GRID ? 1 : 3;
                }
            });
            mRecyclerView.addItemDecoration(new LineItemDecoration(3));
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        }
    }

    @Override
    protected ExpandListContract.Presenter createPresenter() {
        return new ExpandListPresenter(this, mSource);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        String title;
        mSource = getIntent().getIntExtra(EXTRA_SOURCE, ExpandListPresenter.SOURCE_RANK);
        if (mSource == ExpandListPresenter.SOURCE_RANK) {
            title = getString(R.string.find_ranking);
        } else {
            title = getString(R.string.find_category);
        }
        mToolbar.setTitle(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter.start()) {
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initData(final List<MultiItemEntity> list) {
        mLoadView.setVisibility(View.GONE);
        if (list != null && list.size() > 0) {
            mLoadErrorView.setVisibility(View.GONE);
            mAdapter.setNewData(list);
        } else {
            mLoadErrorView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.load_error_view)
    public void onViewClicked() {
        if (mPresenter.start()) {
            mLoadErrorView.setVisibility(View.GONE);
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    private void clickItem(MultiItemEntity item) {
        Entities.TabData data = new Entities.TabData();
        Log.i(TAG, "clickItem :" + item);
        if (item instanceof Entities.RankLv1) {
            Entities.RankLv1 rankLv1 = (Entities.RankLv1) item;
            data.title = rankLv1.title;
            data.source = TabSource.SOURCE_RANK_SUB;
            data.params = new String[] { rankLv1._id, rankLv1.monthRank, rankLv1.totalRank };
        } else if (item instanceof Entities.CategoryLv1) {
            Entities.CategoryLv1 categoryLv1 = (Entities.CategoryLv1) item;
            data.title = categoryLv1.title;
            data.source = TabSource.SOURCE_CATEGORY_SUB;
            data.params = new String[] { categoryLv1.title, categoryLv1.gender };
            if (categoryLv1.minors != null && categoryLv1.minors.size() > 0) {
                List<String> filtrates = new ArrayList<>();
                filtrates.add(data.title);
                filtrates.addAll(categoryLv1.minors);
                data.filtrate = filtrates.toArray(new String[0]);
            }
        }
        if (data.title != null) {
            TabActivity.startActivity(mActivity, data);
        }
    }

    @Override
    public void setPresenter(ExpandListContract.Presenter presenter) {
    }

    private static class ExpandListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        private ItemClickListener mItemClickListener;

        public interface ItemClickListener {
            void onClick(MultiItemEntity item);
        }

        ExpandListAdapter() {
            super(null);
            addItemType(Constant.ITEM_TYPE_TEXT, R.layout.item_view_text);
            addItemType(Constant.ITEM_TYPE_TEXT_IMAGE, R.layout.item_view_img_text);
            addItemType(Constant.ITEM_TYPE_TEXT_IMAGE_2, R.layout.item_view_img_text);
            addItemType(Constant.ITEM_TYPE_TEXT_GRID, R.layout.item_view_grid_text);
        }

        void setOnItemClickListener(ItemClickListener listener) {
            mItemClickListener = listener;
        }

        @Override
        protected void convert(final CommonViewHolder helper, final MultiItemEntity item) {
            switch (helper.getItemViewType()) {
            case Constant.ITEM_TYPE_TEXT:
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
            case Constant.ITEM_TYPE_TEXT_IMAGE:
                updateRankLv1(helper, (Entities.RankLv1) item);
                break;
            case Constant.ITEM_TYPE_TEXT_IMAGE_2:
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
            case Constant.ITEM_TYPE_TEXT_GRID:
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
}
