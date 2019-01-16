package com.xzhou.book.bookshelf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.main.MainActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

public class BookshelfFragment extends BaseFragment<BookshelfContract.Presenter> implements BookshelfContract.View {
    private static final String TAG = "BookshelfFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private Adapter mAdapter;
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
        mAdapter = new Adapter();
        mAdapter.setEmptyView(getEmptyView());
        mAdapter.bindToRecyclerView(mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration());

        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
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
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_bookshelf, null);
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
    public void onLoadingState(boolean loading) {
        mSwipeLayout.setRefreshing(loading);
    }

    @Override
    public void onDataChange(List<BookProvider.LocalBook> books) {
        if (books == null || books.size() < 1) {
            mSwipeLayout.setEnabled(false);
            return;
        }
        mAdapter.setNewData(books);
        mSwipeLayout.setEnabled(true);
    }

    @Override
    public void onBookshelfUpdated(boolean update) {
        String toast = getString(update ? R.string.update_success : R.string.update_none);
        ToastUtils.showShortToast(toast);
    }

    @Override
    public void onAdd(int position, BookProvider.LocalBook book) {
        if (book == null) {
            return;
        }
        mAdapter.addData(position, book);
    }

    @Override
    public void onRemove(BookProvider.LocalBook book) {
        if (book == null) {
            return;
        }
        for (int i = 0, size = mAdapter.getData().size(); i < size; i++) {
            BookProvider.LocalBook old = mAdapter.getData().get(i);
            if (TextUtils.equals(old._id, book._id)) {
                mAdapter.remove(i);
                break;
            }
        }
    }

    private static class Adapter extends BaseQuickAdapter<BookProvider.LocalBook, CommonViewHolder> {

        Adapter() {
            super(R.layout.item_view_bookshelf_book, null);
        }

        @Override
        protected void convert(CommonViewHolder helper, BookProvider.LocalBook item) {
            String sub = AppUtils.getDescriptionTimeFromTimeMills(item.updated);
            helper.setRoundImageUrl(R.id.book_image, item.cover, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.title)
                    .setText(R.id.book_subhead, sub + ":" + item.lastChapter)
                    .setGone(R.id.book_updated_iv, item.updated > item.readTime);
        }

    }
}
