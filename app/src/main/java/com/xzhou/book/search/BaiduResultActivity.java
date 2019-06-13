package com.xzhou.book.search;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.BaiduModel;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_KEY;

public class BaiduResultActivity extends BaseActivity<BaiduContract.Presenter> implements BaiduContract.View {
    private static final String TAG = "BaiduResultActivity";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Adapter mAdapter;
    private String mKey;
    private View mEmptyView;
    private View mLoadView;
    private android.app.AlertDialog mLoadingDialog;

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
        mLoadView = inflater.inflate(R.layout.baidu_search_loading_view, null);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.search(mKey);
            }
        });
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void finish() {
        super.finish();
        mPresenter.cancel();
    }

    @Override
    protected BaiduContract.Presenter createPresenter() {
        return new BaiduPresenter(this, mKey);
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
    public void onLoadingState(boolean loading) {
        Log.i(TAG, "onLoadingState:" + loading);
        if (loading) {
            showLoadingDialog();
        } else {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
            }
        }
    }

    @Override
    public void onSearchProgress(int bookSize, int parseSize, String curHost) {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            Spanned str = Html.fromHtml("已搜索到<b><font color=#ff0000>" + bookSize + "本</font></b>相关书籍<br />已解析<b><font color=#ff0000>"
                    + parseSize + "本</font></b>书籍<br />正在解析网站：" + curHost);
            mLoadingDialog.setMessage(str);
        }
    }

    @Override
    public void onSearchResult(List<BaiduModel.BaiduBook> list) {
        if (list == null || list.size() < 1) {
            mAdapter.setEmptyView(mEmptyView);
        }
        mAdapter.setNewData(list);
    }

    private void showLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);
        mLoadingDialog = builder.setTitle("全网搜索耗时较长，请耐心等待...")
                .setMessage("正在全网搜索中")
                .setPositiveButton("结束搜索", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.cancel();
                        mLoadingDialog.setMessage("正在结束搜索...");
                        mAdapter.setEmptyView(mLoadView);
                    }
                }).create();
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    @Override
    public void setPresenter(BaiduContract.Presenter presenter) {
    }

    private class Adapter extends BaseQuickAdapter<BaiduModel.BaiduBook, CommonViewHolder> {

        private Adapter() {
            super(R.layout.item_view_search_result);
        }

        @Override
        protected void convert(CommonViewHolder holder, final BaiduModel.BaiduBook item) {
            String sub = (TextUtils.isEmpty(item.sourceName) ? item.sourceHost : item.sourceName + " | " + item.sourceHost);
            if (!TextUtils.isEmpty(item.author)) {
                sub = item.author + " | " + sub;
            }
            holder.setRoundImageUrl(R.id.book_image, item.image, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.bookName)
                    .setText(R.id.book_h2, sub)
                    .setGone(R.id.local_read_tv, BaiduModel.hasSupportLocalRead(item.sourceHost));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookProvider.LocalBook localBook = new BookProvider.LocalBook(item);
                    if (BaiduModel.hasSupportLocalRead(item.sourceHost)) {
                        ReadActivity.startActivity(mActivity, localBook);
                    } else {
                        ReadWebActivity.startActivity(mActivity, localBook);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (BookProvider.hasCacheData(item.id)) {
                        ToastUtils.showShortToast("已经加入书架了");
                        return true;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("是否将本书加入书架？")
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    BookProvider.insertOrUpdate(new BookProvider.LocalBook(item), false);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return true;
                }
            });
        }
    }
}
