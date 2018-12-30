package com.xzhou.book.find;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.widget.DrawableButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 主题书单详情
 */
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_collect) {
            //收藏
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        mLoadView.setVisibility(View.GONE);
        if (detail == null || detail.bookList == null) {
            mLoadErrorView.setVisibility(View.VISIBLE);
        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.header_view_book_list_detail, null);
            HeaderViewHolder viewHolder = new HeaderViewHolder(view);
            viewHolder.initHeaderViewData(detail);
            Adapter adapter = new Adapter(detail.bookList.books);
            adapter.addHeaderView(view);
            adapter.bindToRecyclerView(mRecyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new MyItemDecoration());
        }
    }

    @Override
    public void setPresenter(BookListDetailContract.Presenter presenter) {
    }

    @OnClick({R.id.share_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.share_btn:
            break;
        }
    }

    static class HeaderViewHolder {
        @BindView(R.id.book_list_detail_title)
        TextView mBookListDetailTitle;
        @BindView(R.id.book_list_detail_desc)
        TextView mBookListDetailDesc;
        @BindView(R.id.book_list_detail_desc_more)
        ImageView mBookListDetailDescMore;
        @BindView(R.id.book_list_detail_image)
        ImageView mBookListDetailImage;
        @BindView(R.id.book_detail_author)
        TextView mBookDetailAuthor;
        @BindView(R.id.book_list_detail_share_btn)
        TextView mBookListDetailShareBtn;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        private void initHeaderViewData(Entities.BookListDetail detail) {
            ImageLoader.showCircleImageUrl(mBookListDetailTitle.getContext(), mBookListDetailImage, detail.bookList.avatar(), R.mipmap.avatar_default);
            mBookListDetailTitle.setText(detail.bookList.title);
            mBookListDetailDesc.setText(detail.bookList.desc);
            mBookListDetailDesc.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mBookListDetailDesc.getLineCount() < mBookListDetailDesc.getMaxLines()) {
                        mBookListDetailDescMore.setVisibility(View.GONE);
                    }
                    mBookListDetailDesc.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            mBookDetailAuthor.setText(detail.bookList.nickname());
        }

        @OnClick({R.id.book_list_detail_desc, R.id.book_list_detail_share_btn, R.id.book_list_detail_desc_more})
        public void onViewClicked(View view) {
            switch (view.getId()) {
            case R.id.book_list_detail_desc:
            case R.id.book_list_detail_desc_more:
                if (mBookListDetailDesc.getLineCount() == mBookListDetailDesc.getMaxLines()) {
                    mBookListDetailDesc.setMaxLines(Integer.MAX_VALUE);
                    mBookListDetailDescMore.setVisibility(View.GONE);
                }
                break;
            case R.id.book_list_detail_share_btn:
                break;
            }
        }
    }

    private static class MyItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int space = AppUtils.dip2px(8);
            int pos = parent.getChildAdapterPosition(view);
            if (pos == 0) {
                outRect.set(0, 0, 0, 0);
            } else if (pos + 1 >= parent.getAdapter().getItemCount()) {
                outRect.set(space, space, space, space * 2);
            } else {
                outRect.set(space, space, space, 0);
            }
        }
    }

    private static class Adapter extends BaseQuickAdapter<Entities.BookListDetail.BooksBean, CommonViewHolder> {

        Adapter(@Nullable List<Entities.BookListDetail.BooksBean> data) {
            super(R.layout.item_view_book_list_detail, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.BookListDetail.BooksBean item) {
            String wordCount = AppUtils.getString(R.string.book_list_word_count, item.book.latelyFollower, item.book.wordCount / 10000);
            holder.setRoundImageUrl(R.id.book_image, item.book.cover(), R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.book.title)
                    .setText(R.id.book_author, item.book.author)
                    .setText(R.id.book_word_count, Html.fromHtml(wordCount))
                    .setText(R.id.book_list_desc, item.comment);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookDetailActivity.startActivity(getRecyclerView().getContext(), item.book._id);
                }
            });
        }
    }
}
