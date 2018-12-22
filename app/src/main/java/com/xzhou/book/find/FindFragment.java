package com.xzhou.book.find;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.ItemAdapter;
import com.xzhou.book.common.ListItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.utils.AppUtils;

import java.util.List;

import butterknife.BindView;

public class FindFragment extends BaseFragment<FindContract.Presenter> implements FindContract.View {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private ItemAdapter mAdapter;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_find;
    }

    @Override
    public void setPresenter(FindContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onInitData(List<Entities.ItemClick> list) {
        mAdapter = new ItemAdapter(list, true);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Object item = adapter.getItem(position);
                if (item instanceof Entities.ItemClick) {
                    String name = ((Entities.ItemClick) item).name;
                    if (AppUtils.getString(R.string.find_topic).equals(name)) {

                    } else if (AppUtils.getString(R.string.find_random_read).equals(name)) {

                    } else if (AppUtils.getString(R.string.find_category).equals(name)) {
                        SortListActivity.startActivity(getActivity(), SortListPresenter.SOURCE_CATEGORY);
                    } else if (AppUtils.getString(R.string.find_ranking).equals(name)) {
                        SortListActivity.startActivity(getActivity(), SortListPresenter.SOURCE_RANK);
                    }
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new ListItemDecoration(true));
    }
}
