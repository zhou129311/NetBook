package com.xzhou.book.find;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.widget.DrawableButton;

import java.util.List;

import butterknife.BindView;

public class BookListDetailActivity extends BaseActivity<BookListDetailContract.Presenter> implements BookListDetailContract.View {
    private static final String TAG = "BookListDetailActivity";
    public static final String EXTRA_BOOK_LIST_ID = "extra_bookListId";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;
    @BindView(R.id.share_btn)
    DrawableButton mShareBtn;

    public static void startActivity(Context context, String bookListId) {
        Intent intent = new Intent(context, BookListDetailActivity.class);
        intent.putExtra(EXTRA_BOOK_LIST_ID, bookListId);
        context.startActivity(intent);
    }

    @Override
    protected BookListDetailContract.Presenter createPresenter() {
        return new BookListDetailPresenter(this, getIntent().getStringExtra(EXTRA_BOOK_LIST_ID));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list_detail);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitle(R.string.book_list_detail);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter.start()) {
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInitData(Entities.BookListDetail detail) {

    }

    @Override
    public void setPresenter(BookListDetailContract.Presenter presenter) {
    }
}
