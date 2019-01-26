package com.xzhou.book.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.CommonLoadMoreView;

import java.util.List;

import butterknife.BindView;

import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_KEY;

public class ResultFragment extends BaseFragment<SearchContract.Presenter> implements SearchContract.View {
    private static final String TAG = "ResultFragment";
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    private String mKey;
    private OnAutoCompleteListener mAutoCompleteListener;
    private Adapter mAdapter;
    private TextView mEmptyView;

    public interface OnAutoCompleteListener {
        void onUpdate(List<String> list);
    }

    public void setOnAutoCompleteListener(OnAutoCompleteListener listener) {
        mAutoCompleteListener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_search_result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new Adapter();
        mAdapter.setHeaderAndEmpty(true);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration(true, 0, 0));
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(view.getContext()));
        mAdapter.setEnableLoadMore(true);
        mAdapter.disableLoadMoreIfNotFullPage();
        mAdapter.setLoadMoreView(new CommonLoadMoreView());
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.loadMore();
            }
        }, mRecyclerView);

        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        mEmptyView = (TextView) inflater.inflate(R.layout.common_empty_view, null);
        mAdapter.setEmptyView(mEmptyView);
        TextView headerView = (TextView) inflater.inflate(R.layout.header_view_search_result, null);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mKey)) {
                    BaiduResultActivity.startActivity(getContext(), mKey);
                } else {
                    ToastUtils.showShortToast("请输入关键字进行搜索");
                }
            }
        });
        mAdapter.addHeaderView(headerView);

        Bundle bundle = getArguments();
        if (bundle != null && TextUtils.isEmpty(mKey)) {
            mKey = bundle.getString(EXTRA_SEARCH_KEY);
        }
        if (!TextUtils.isEmpty(mKey)) {
            mPresenter.search(mKey);
        }
    }

    public void search(String key) {
        if (!isAdded()) {
            mKey = key;
            return;
        }
        if (!TextUtils.isEmpty(key) && !key.equals(mKey)) {
            mPresenter.search(key);
        } else {
            Log.e(TAG, "oldKey = " + mKey + ",newKey = " + key);
        }
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onSearchResult(List<Entities.SearchBook> list) {
        if (!isAdded()) {
            Log.e(TAG, "onSearchResult error ,fragment don't add activity");
            return;
        }
        if (list == null) {
            if (AppUtils.isNetworkAvailable()) {
                mEmptyView.setText(R.string.network_error_tips);
            } else {
                mEmptyView.setText(R.string.network_unconnected);
            }
            mAdapter.setNewData(null);
        } else if (list.size() <= 0) {
            mEmptyView.setText(R.string.empty_data);
            mAdapter.setNewData(null);
        } else {
            mAdapter.replaceData(list);
        }
    }

    @Override
    public void onLoadMore(List<Entities.SearchBook> list) {
        if (list == null) {
            mAdapter.loadMoreFail();
        } else if (list.size() <= 0) {
            mAdapter.loadMoreEnd();
        } else {
            mAdapter.loadMoreComplete();
            mAdapter.addData(list);
        }
    }

    @Override
    public void onAutoComplete(List<String> list) {
        if (mAutoCompleteListener != null) {
            mAutoCompleteListener.onUpdate(list);
        }
    }

    @Override
    public void onLoadState(boolean loading) {
        mLoadView.setVisibility(loading ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private class Adapter extends BaseQuickAdapter<Entities.SearchBook, CommonViewHolder> {

        Adapter() {
            super(R.layout.item_view_search_result);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.SearchBook item) {
            String subText = AppUtils.getString(R.string.search_result_h2, item.latelyFollower, String.valueOf(item.retentionRatio), item.author);
            holder.setRoundImageUrl(R.id.book_image, item.cover(), R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.title)
                    .setText(R.id.book_h2, subText);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getContext(), item._id);
                }
            });
        }
    }
}
