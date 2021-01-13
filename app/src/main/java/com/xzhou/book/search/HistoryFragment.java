package com.xzhou.book.search;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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
    private final List<String> mList = new ArrayList<>();

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
        mAdapter.setOnItemChildClickListener((adapter, view1, position) -> {
            mAdapter.remove(position);
            AppSettings.saveHistory(mList);
        });
        mClearHistoryTv.setEnabled(!mList.isEmpty());
    }

    public void addNewHistory(String history) {
        if (!mList.contains(history)) {
            mList.add(0, history);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            if (mClearHistoryTv != null) {
                mClearHistoryTv.setEnabled(true);
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
            holder.setOnClickListener(R.id.history_delete_iv, v -> {
                mAdapter.remove(mList.indexOf(item));
                mClearHistoryTv.setEnabled(!mList.isEmpty());
                AppSettings.saveHistory(mList);
            });
            holder.itemView.setOnClickListener(v -> {
                if (mHistoryListener != null) {
                    mHistoryListener.onClick(item);
                }
            });
        }
    }
}
