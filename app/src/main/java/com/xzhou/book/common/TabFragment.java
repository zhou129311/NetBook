package com.xzhou.book.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.widget.CommonLoadMoreView;

import java.util.List;

import butterknife.BindView;

public class TabFragment extends BaseFragment<TabContract.Presenter> implements TabContract.View {
    private String TAG;

    public static final String TAB_DATA = "tab_data";
    public static final String TAB_POSITION = "position";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private Entities.TabData mTabData;
    private TabAdapter mAdapter;
    private View mEmptyView;
    private View mLoadErrorView;
    private int mPosition;

    public static TabFragment newInstance(Entities.TabData data, int position) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TAB_DATA, data);
        bundle.putInt(TAB_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    public int getPosition() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(TAB_POSITION, -1);
        }
        return -1;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_common_tab;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTabData = bundle.getParcelable(TAB_DATA);
            mPosition = bundle.getInt(TAB_POSITION);
            TAG = "TabFragment_" + mPosition;
        }
        mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.common_empty_view, null);
        mLoadErrorView = LayoutInflater.from(getActivity()).inflate(R.layout.common_load_error_view, null);
        mLoadErrorView.setOnClickListener(mRefreshClickListener);
        mEmptyView.setOnClickListener(mRefreshClickListener);

        mAdapter = new TabAdapter(mPosition);
        mAdapter.bindToRecyclerView(mRecyclerView);
        boolean enableLoadMore = hasEnableLoadMore();
        mAdapter.setEnableLoadMore(enableLoadMore);
        if (enableLoadMore) {
            mAdapter.disableLoadMoreIfNotFullPage();
            mAdapter.setLoadMoreView(new CommonLoadMoreView());
            mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    mPresenter.loadMore();
                }
            }, mRecyclerView);
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration(true));
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));

        mSwipeLayout.setEnabled(hasEnableRefresh());
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPosition == 0) {
            mPresenter.start();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            mPresenter.start();
        }
    }

    @Override
    public void setPresenter(TabContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onRefreshStateChange(boolean isRefreshing) {
        mSwipeLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {
        if (list == null) {
            mAdapter.setEmptyView(mLoadErrorView);
            mAdapter.notifyDataSetChanged();
        } else if (list.size() <= 0) {
            mAdapter.setEmptyView(mEmptyView);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.replaceData(list);
        }
    }

    @Override
    public void onLoadMore(List<MultiItemEntity> list) {
        if (list == null) {
            mAdapter.loadMoreFail();
        } else if (list.size() <= 0) {
            mAdapter.loadMoreEnd();
        } else {
            mAdapter.loadMoreComplete();
            mAdapter.addData(list);
        }
    }

    private View.OnClickListener mRefreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPresenter.refresh();
        }
    };

    private String getDataSource() {
        if (mTabData.source == Constant.TabSource.SOURCE_RANK_SUB) {
            return mTabData.params[mPosition];
        }
        return "";
    }

    private boolean hasEnableRefresh() {
        boolean enable = false;
        switch (mTabData.source) {
        case Constant.TabSource.SOURCE_CATEGORY_SUB:
        case Constant.TabSource.SOURCE_RANK_SUB:
        case Constant.TabSource.SOURCE_TOPIC_LIST:
            enable = true;
            break;
        }
        return enable;
    }

    private boolean hasEnableLoadMore() {
        boolean enable = false;
        switch (mTabData.source) {
        case Constant.TabSource.SOURCE_CATEGORY_SUB:
        case Constant.TabSource.SOURCE_TOPIC_LIST:
        case Constant.TabSource.SOURCE_TAG:
            enable = true;
            break;
        }
        return enable;
    }
}
