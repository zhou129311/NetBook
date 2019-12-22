package com.xzhou.book.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.search.OtherResultActivity;
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.Constant.TabSource;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.CommonLoadMoreView;

import java.util.List;

import butterknife.BindView;

public class TabFragment extends BaseFragment<TabContract.Presenter> implements TabContract.View {
    private String TAG;

    public static final String TAB_DATA = "tab_data";
    public static final String TAB_ID = "tab_id";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private Entities.TabData mTabData;
    private TabAdapter mAdapter;
    private View mEmptyView;
    private View mLoadErrorView;
    private int mTabId;

    public static TabFragment newInstance(Entities.TabData data, int tabId) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TAB_DATA, data);
        bundle.putInt(TAB_ID, tabId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public int getTabId() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(TAB_ID, -1);
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
            mTabId = bundle.getInt(TAB_ID, 0);
            TAG = "TabFragment_" + mTabId;
        }
        mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.common_empty_view, null);
        mLoadErrorView = LayoutInflater.from(getActivity()).inflate(R.layout.common_load_error_view, null);
        mLoadErrorView.setVisibility(View.VISIBLE);
        mLoadErrorView.setOnClickListener(mRefreshClickListener);
        mEmptyView.setOnClickListener(mRefreshClickListener);

        mAdapter = new TabAdapter(mTabId);
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
        mRecyclerView.addItemDecoration(new LineItemDecoration());
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));

        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
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
        int tabId = 0;
        Activity activity = getActivity();
        if (activity instanceof TabActivity) {
            tabId = ((TabActivity) activity).getCurTabId();
        } else if (activity instanceof SearchActivity) {
            tabId = ((SearchActivity) activity).getCurTabId();
        }
        if (mTabId == tabId) {
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
        Activity activity = getActivity();
        if (activity instanceof SearchActivity) {
            ((SearchActivity) activity).onAutoComplete(null);
        }
        if (isRefreshing) {
            mEmptyView.setVisibility(View.INVISIBLE);
            mLoadErrorView.setVisibility(View.INVISIBLE);
        }
        mSwipeLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {
        if (list == null) {
            mLoadErrorView.setVisibility(View.VISIBLE);
            mAdapter.setEmptyView(mLoadErrorView);
            mAdapter.setNewData(null);
        } else if (list.size() <= 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mAdapter.setEmptyView(mEmptyView);
            mAdapter.setNewData(null);
        } else {
            mAdapter.setNewData(list);
//            checkAddHeaderView();
        }
    }

    private void checkAddHeaderView() {
        if (mTabData != null && mTabData.source == TabSource.SOURCE_SEARCH
                && mTabId == 0 && mAdapter.getHeaderLayoutCount() == 0) {
            TextView headerView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.header_view_search_result, null);
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mTabData.params[0])) {
                        SearchActivity.startActivity(getContext(), mTabData.params[0], SearchActivity.SEARCH_TYPE_BAIDU);
                    } else {
                        ToastUtils.showShortToast("请输入关键字进行搜索");
                    }
                }
            });
            mAdapter.addHeaderView(headerView);
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
        if (mTabData.source == TabSource.SOURCE_RANK_SUB) {
            return mTabData.params[mTabId];
        }
        return "";
    }

    private boolean hasEnableRefresh() {
        boolean enable = false;
        switch (mTabData.source) {
        case TabSource.SOURCE_CATEGORY_SUB:
        case TabSource.SOURCE_RANK_SUB:
        case TabSource.SOURCE_TOPIC_LIST:
        case TabSource.SOURCE_COMMUNITY:
        case TabSource.SOURCE_SEARCH:
            enable = true;
            break;
        }
        return enable;
    }

    private boolean hasEnableLoadMore() {
        boolean enable = false;
        switch (mTabData.source) {
        case TabSource.SOURCE_CATEGORY_SUB:
        case TabSource.SOURCE_TOPIC_LIST:
        case TabSource.SOURCE_TAG:
        case TabSource.SOURCE_COMMUNITY:
        case TabSource.SOURCE_SEARCH:
            enable = true;
            break;
        }
        return enable;
    }
}
