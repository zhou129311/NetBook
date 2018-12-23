package com.xzhou.book.bookrack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.main.MainActivity;

import java.util.List;

import butterknife.BindView;

public class BookshelfFragment extends BaseFragment<BookshelfContract.Presenter> implements BookshelfContract.View {
    private static final String TAG = "BookshelfFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private BookshelfAdapter mAdapter;
    private View mEmptyView;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_bookrack;
    }

    @Override
    public void setPresenter(BookshelfContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new BookshelfAdapter();
        mAdapter.setEmptyView(getEmptyView());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration());

        mSwipeLayout.setOnRefreshListener(mRefreshListener);
        mSwipeLayout.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mPresenter.refresh();
        }
    };

    private View getEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.view_empty_bookshelf, null);
            mEmptyView.findViewById(R.id.add_book_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.setCurFragment(MainActivity.FRAGMENT_FIND);
                    }
                }
            });
        }
        return mEmptyView;
    }

    @Override
    public void showLoading() {
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onDataChange(List<Entities.NetBook> books) {
        mAdapter.setNewData(books);
        if (books == null || books.isEmpty()) {
            mSwipeLayout.setEnabled(false);
        } else {
            mSwipeLayout.setEnabled(true);
        }
    }

    @Override
    public void onAdd(Entities.NetBook book) {
        mAdapter.addData(book);
    }

    @Override
    public void onRemove(Entities.NetBook book) {
        mAdapter.remove(mAdapter.getData().indexOf(book));
    }
}
