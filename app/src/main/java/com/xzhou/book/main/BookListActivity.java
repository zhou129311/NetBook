package com.xzhou.book.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;

import java.util.List;

import butterknife.BindView;

public class BookListActivity extends BaseActivity<BookListContract.Presenter> implements BookListContract.View {
    private static final String TAG = "BookListActivity";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    @Override
    protected BookListContract.Presenter createPresenter() {
        return new BookListPresenter(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter.start()) {
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {

    }

    @Override
    public void onLoadMore(List<MultiItemEntity> list) {

    }

    @Override
    public void setPresenter(BookListContract.Presenter presenter) {
    }
}
