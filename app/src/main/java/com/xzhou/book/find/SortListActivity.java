package com.xzhou.book.find;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.ListItemDecoration;
import com.xzhou.book.common.TabActivity;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SortListActivity extends BaseActivity implements SortListContract.View {
    private static final String TAG = "SortListActivity";

    private static final String EXTRA_SOURCE = "extra_source";

    public static void startActivity(Activity activity, int source) {
        Intent intent = new Intent(activity, SortListActivity.class);
        intent.putExtra(EXTRA_SOURCE, source);
        activity.startActivity(intent);
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    private SortListAdapter mAdapter;
    private SortListContract.Presenter mPresenter;
    private int mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_list);
        mPresenter = new SortListPresenter(this, mSource);

        mAdapter = new SortListAdapter(this);
        mAdapter.setOnItemClickListener(new SortListAdapter.ItemClickListener() {
            @Override
            public void onClick(MultiItemEntity item) {
                clickItem(item);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        if (mSource == SortListPresenter.SOURCE_CATEGORY) {
            MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(this, 3);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return mAdapter.getItemViewType(position) == SortListAdapter.TEXT_GRID ? 1 : 3;
                }
            });
            mRecyclerView.addItemDecoration(new ListItemDecoration(3));
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        }
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        String title;
        mSource = getIntent().getIntExtra(EXTRA_SOURCE, SortListPresenter.SOURCE_RANK);
        if (mSource == SortListPresenter.SOURCE_RANK) {
            title = getString(R.string.find_ranking);
        } else {
            title = getString(R.string.find_category);
        }
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setTitle(title);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter.start()) {
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void setPresenter(SortListContract.Presenter presenter) {
    }

    @Override
    public void initData(final List<MultiItemEntity> list) {
        mLoadView.setVisibility(View.GONE);
        if (list != null) {
            if (list.size() > 0) {
                mLoadErrorView.setVisibility(View.GONE);
                mAdapter.setNewData(list);
            } else {

            }
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
            data.source = Constant.TabSource.SOURCE_RANK_SUB;
            data.weekRankId = rankLv1._id;
            data.monthRankId = rankLv1.monthRank;
            data.totalRankId = rankLv1.totalRank;
        } else if (item instanceof Entities.CategoryLv1) {
            Entities.CategoryLv1 categoryLv1 = (Entities.CategoryLv1) item;
            data.title = categoryLv1.title;
            data.source = Constant.TabSource.SOURCE_CATEGORY_SUB;
            data.gender = categoryLv1.gender;
            data.major = categoryLv1.title;
            data.minor = "";
        }
        if (data.title != null) {
            TabActivity.startActivity(mActivity, data);
        }
    }
}
