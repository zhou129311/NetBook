package com.xzhou.book.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

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
        mLoadErrorView.setOnClickListener(mRefershClickListener);
        mEmptyView.setOnClickListener(mRefershClickListener);

        mAdapter = new TabAdapter(mPosition);
        mAdapter.bindToRecyclerView(mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new ListItemDecoration());
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
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
        if (mPresenter.start()) {
            mSwipeLayout.setRefreshing(true);
        }
    }

    @Override
    public void setPresenter(TabContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {
        mSwipeLayout.setRefreshing(false);
        if (list == null) {
            Log.i(TAG, "onDataChange: null");
            mAdapter.setEmptyView(mLoadErrorView);
            mAdapter.notifyDataSetChanged();
        } else if (list.size() <= 0) {
            Log.i(TAG, "onDataChange: " + list);
            mAdapter.setEmptyView(mEmptyView);
            mAdapter.notifyDataSetChanged();
        } else {
            Log.i(TAG, "onDataChange: " + list.size() + ",source = " + getDataSource());
            mAdapter.replaceData(list);
        }

        Log.i(TAG, "mAdapter.getData = " + mAdapter.getData().size());
    }

    @Override
    public void onLoadMore(List<MultiItemEntity> list) {
        mSwipeLayout.setRefreshing(false);
        if (list == null) {

        } else if (list.size() <= 0) {

        } else {
            mAdapter.addData(list);
        }
    }

    private View.OnClickListener mRefershClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSwipeLayout.setRefreshing(true);
            mPresenter.refresh();
        }
    };

    private String getDataSource() {
        if (mTabData.source == Constant.TabSource.SOURCE_RANK_SUB) {
            if (mPosition == 0) {
                return mTabData.weekRankId;
            } else if (mPosition == 1) {
                return mTabData.monthRankId;
            } else if (mPosition == 2) {
                return mTabData.totalRankId;
            }
        }
        return "";
    }
}
