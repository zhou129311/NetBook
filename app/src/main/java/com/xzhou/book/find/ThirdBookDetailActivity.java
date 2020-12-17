package com.xzhou.book.find;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.MyGridLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.widget.DrawableButton;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-20
 * Change List:
 */
public class ThirdBookDetailActivity extends BaseActivity {

    //占位
    @BindView(R.id.place_view)
    FrameLayout mPlaceView;
    @BindView(R.id.load_error_view)
    View mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    @BindView(R.id.detail_book_img)
    ImageView detailBookImg;
    @BindView(R.id.detail_book_title)
    TextView detailBookTitle;
    @BindView(R.id.detail_tag)
    TextView detailBookTag;
    @BindView(R.id.detail_book_author)
    TextView detailBookAuthor;
    @BindView(R.id.detail_last_updated)
    TextView detailLastUpdated;
    @BindView(R.id.detail_join)
    DrawableButton mJoinBtn;
    @BindView(R.id.detail_read)
    DrawableButton mReadBtn;
    @BindView(R.id.detail_last_chapter)
    TextView detailLastChapter;
    @BindView(R.id.detail_intro)
    TextView detailIntro;
    @BindView(R.id.detail_group_count)
    RecyclerView mRecyclerView;

    private SearchModel.SearchBook mSearchBook;
    private ThirdBookViewModel mViewModel;

    public static void startActivity(Context context, SearchModel.SearchBook book) {
        Intent intent = new Intent(context, ThirdBookDetailActivity.class);
        intent.setExtrasClassLoader(SearchModel.SearchBook.class.getClassLoader());
        intent.putExtra("search_book", book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website_book_detail);
        mSearchBook = (SearchModel.SearchBook) getIntent().getSerializableExtra("search_book");
        if (savedInstanceState != null) {
            mSearchBook = (SearchModel.SearchBook) savedInstanceState.getSerializable("search_book");
        }
        if (mSearchBook == null) {
            finish();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider
                .AndroidViewModelFactory(getApplication())).get(ThirdBookViewModel.class);

        mViewModel.mData.observe(this, thirdBookDetail -> {
            if (thirdBookDetail != null) {
                ViewGroup parent = (ViewGroup) mPlaceView.getParent();
                if (parent != null) {
                    parent.removeView(mPlaceView);
                } else {
                    mPlaceView.setVisibility(View.GONE);
                }
                initViewData(thirdBookDetail);
            } else {
                mLoadView.setVisibility(View.GONE);
                mLoadErrorView.setVisibility(View.VISIBLE);
            }
        });
        mToolbar.setTitle(mSearchBook.bookName);
        startData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_book, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_download).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search_baidu) {
            SearchActivity.startActivity(this, mSearchBook.bookName, SearchActivity.SEARCH_TYPE_BAIDU);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("search_book", mSearchBook);
    }

    private void startData() {
        mViewModel.load(mSearchBook);
        mPlaceView.setVisibility(View.VISIBLE);
        mLoadView.setVisibility(View.VISIBLE);
        mLoadErrorView.setVisibility(View.GONE);
    }

    private void initViewData(Entities.ThirdBookDetail detail) {
        ImageLoader.showRoundImageUrl(this, detailBookImg, detail.image, R.mipmap.ic_cover_default);
        detailBookTitle.setText(detail.title);
        detailBookAuthor.setText(detail.author);
        Adapter adapter = new Adapter(detail.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyGridLayoutManager(this, 3, true));
        adapter.bindToRecyclerView(mRecyclerView);
        detailBookTag.setText(detail.tags);
        detailIntro.setText(detail.intro);
        detailLastUpdated.setText(detail.lastUpdate);
        detailLastChapter.setText(detail.lastChapter);
        updateJoinBtn(BookProvider.hasCacheData(mSearchBook.id));
    }

    private void updateJoinBtn(boolean activated) {
        mJoinBtn.setActivated(activated);
        if (!mJoinBtn.isActivated()) {
            mJoinBtn.setText(R.string.book_detail_join_collection);
        } else {
            mJoinBtn.setText(R.string.book_detail_remove_collection);
        }
    }

    @OnClick({R.id.load_error_view, R.id.detail_join, R.id.detail_read})
    public void onViewClicked(View view) {
        if (doubleClick()) {
            return;
        }
        int id = view.getId();
        if (id == R.id.load_error_view) {
            startData();
        } else if (id == R.id.detail_join) {
            if (BookProvider.hasCacheData(mSearchBook.id)) {
                BookProvider.delete(mSearchBook.id, mSearchBook.bookName, true);
                updateJoinBtn(false);
            } else {
                BookProvider.insertOrUpdate(new BookProvider.LocalBook(mSearchBook), false);
                updateJoinBtn(true);
            }
        } else if (id == R.id.detail_read) {
            BookProvider.LocalBook localBook = new BookProvider.LocalBook(mSearchBook);
            ReadActivity.startActivity(this, localBook);
        }
    }

    private static class Adapter extends BaseQuickAdapter<Pair<String, String>, CommonViewHolder> {

        public Adapter(@Nullable List<Pair<String, String>> data) {
            super(R.layout.item_view_pair_count, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, Pair<String, String> item) {
            holder.setText(R.id.detail_pair_key, item.first)
                    .setText(R.id.detail_pair_count, item.second);
        }
    }
}
