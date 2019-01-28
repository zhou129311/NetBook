package com.xzhou.book.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.models.BaiduEntities;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_KEY;

public class BaiduResultActivity extends BaseActivity<BaiduContract.Presenter> implements BaiduContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Adapter mAdapter;
    private RelativeLayout mLoadingView;
    private View mEmptyView;
    private String mKey;

    public static void startActivity(Context context, String key) {
        Intent intent = new Intent(context, BaiduResultActivity.class);
        intent.putExtra(EXTRA_SEARCH_KEY, key);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_baidu);

        LayoutInflater inflater = LayoutInflater.from(this);
        mEmptyView = inflater.inflate(R.layout.common_empty_view, null);
        ProgressBar loading = (ProgressBar) inflater.inflate(R.layout.common_load_view, null);
        loading.setVisibility(View.VISIBLE);
        mLoadingView = new RelativeLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoadingView.addView(loading, lp);

        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration(false, 0, 0));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.setEmptyView(mLoadingView);

        mPresenter.search(mKey);
    }

    @Override
    protected BaiduContract.Presenter createPresenter() {
        return new BaiduPresenter(this);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mKey = getIntent().getStringExtra(EXTRA_SEARCH_KEY);
        if (TextUtils.isEmpty(mKey)) {
            finish();
            ToastUtils.showShortToast("请输入关键字");
            return;
        }
        mToolbar.setTitle(mKey);
    }

    @Override
    public void onSearchResult(List<BaiduEntities.BaiduBook> list) {
        if (list == null || list.size() < 1) {
            mAdapter.setEmptyView(mEmptyView);
        }
        mAdapter.setNewData(list);
    }

    @Override
    public void setPresenter(BaiduContract.Presenter presenter) {
    }

    private class Adapter extends BaseQuickAdapter<BaiduEntities.BaiduBook, CommonViewHolder> {

        public Adapter() {
            super(R.layout.item_view_search_result);
        }

        @Override
        protected void convert(CommonViewHolder holder, final BaiduEntities.BaiduBook item) {
            Log.i(TAG, "convert:BaiduBook = " + item);
            String sub = TextUtils.isEmpty(item.sourceName) ? item.sourceHost : item.sourceName + " " + item.sourceHost;
            final String mobReadUrl = item.mobReadUrl;
            holder.setRoundImageUrl(R.id.book_image, item.image, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.bookName)
                    .setText(R.id.book_h2, sub);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "mobReadUrl = " + mobReadUrl);
                    if (!TextUtils.isEmpty(mobReadUrl)) {
                        ReadWebActivity.startActivity(mActivity, mobReadUrl);
                    }
                }
            });
        }
    }
}
