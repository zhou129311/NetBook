package com.xzhou.book.community;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.ItemAdapter;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

public class CommunityFragment extends BaseFragment<CommunityContract.Presenter> implements CommunityContract.View {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_community;
    }

    @Override
    public void setPresenter(CommunityContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onInitData(List<Entities.ImageText> list) {
        ItemAdapter adapter = new ItemAdapter(list, false);
        adapter.bindToRecyclerView(mRecyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Object item = adapter.getItem(position);
                if (item instanceof Entities.ImageText) {
                    String name = ((Entities.ImageText) item).name;
                    if (AppUtils.getString(R.string.community_discuss).equals(name)) {

                    } else if (AppUtils.getString(R.string.community_comment).equals(name)) {

                    } else if (AppUtils.getString(R.string.community_helper).equals(name)) {

                    } else if (AppUtils.getString(R.string.community_girl).equals(name)) {

                    }
                    ToastUtils.showShortToast(name);
                }
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(true));
    }
}
