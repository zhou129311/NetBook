package com.xzhou.book.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.utils.AppSettings;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HistoryFragment extends BaseFragment {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.clear_history_tv)
    TextView mClearHistoryTv;

    public interface OnHistoryListener {
        void onClick(String history);
    }

    private OnHistoryListener mHistoryListener;
    private Adapter mAdapter;
    private List<String> mList = new ArrayList<>();

    public void setOnHistoryListener(OnHistoryListener listener) {
        mHistoryListener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_search_history;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> list = AppSettings.getSearchHistory();
        if (list != null) {
            mList.addAll(list);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new Adapter(mList);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(view.getContext()));
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                mAdapter.remove(position);
                AppSettings.saveHistory(mList);
            }
        });
        mClearHistoryTv.setEnabled(!mList.isEmpty());
    }

    public void addNewHistory(String history) {
        if (!mList.contains(history)) {
            if (mAdapter != null) {
                mAdapter.addData(history);
                mClearHistoryTv.setEnabled(!mList.isEmpty());
            } else {
                mList.add(history);
            }
            AppSettings.saveHistory(mList);
        }
    }

    @OnClick(R.id.clear_history_tv)
    public void onViewClicked() {
        mList.clear();
        mAdapter.setNewData(null);
        AppSettings.saveHistory(null);
        mClearHistoryTv.setEnabled(!mList.isEmpty());
    }

    private class Adapter extends BaseQuickAdapter<String, CommonViewHolder> {

        Adapter(@Nullable List<String> data) {
            super(R.layout.item_search_history, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, final String item) {
            holder.setText(R.id.history_content_tv, item);
            holder.setOnClickListener(R.id.history_delete_iv, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.remove(mList.indexOf(item));
                    AppSettings.saveHistory(mList);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHistoryListener != null) {
                        mHistoryListener.onClick(item);
                    }
                }
            });
        }
    }
}
