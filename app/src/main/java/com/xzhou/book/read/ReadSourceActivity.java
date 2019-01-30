package com.xzhou.book.read;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.search.BaiduResultActivity;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

public class ReadSourceActivity extends BaseActivity<ReadPresenter> implements ReadContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private BookProvider.LocalBook mBook;
    private Adapter mAdapter;
    private RelativeLayout mLoadingView;
    private TextView mEmptyView;

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        Intent intent = new Intent(context, ReadSourceActivity.class);
        intent.putExtra(ReadActivity.EXTRA_BOOK, book);
        ((Activity) context).startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBook = getIntent().getParcelableExtra(ReadActivity.EXTRA_BOOK);
        if (mBook == null) {
            finish();
            ToastUtils.showShortToast("该书籍无法换源");
            return;
        }
        setContentView(R.layout.activity_read_source);
        initAdapter();
    }

    @Override
    protected ReadPresenter createPresenter() {
        return new ReadPresenter(this, mBook);
    }

    private void initAdapter() {
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        //init empty and load view
        LayoutInflater inflater = LayoutInflater.from(this);
        mEmptyView = (TextView) inflater.inflate(R.layout.common_empty_view, null);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.loadAllSource();
                mAdapter.setEmptyView(mLoadingView);
            }
        });
        ProgressBar loading = (ProgressBar) inflater.inflate(R.layout.common_load_view, null);
        loading.setVisibility(View.VISIBLE);
        mLoadingView = new RelativeLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoadingView.addView(loading, lp);
        View header = inflater.inflate(R.layout.item_view_book_source_header, null);
        header.findViewById(R.id.baidu_source_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaiduResultActivity.startActivity(mActivity, mBook.title);
            }
        });

        mAdapter.setEmptyView(mLoadingView);
        mAdapter.setHeaderAndEmpty(true);
        mAdapter.setHeaderView(header);
        mPresenter.loadAllSource();
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.source_title);
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_clear_mtrl_alpha);
    }

    @Override
    public void initChapterList(List<Entities.Chapters> list) {
    }

    @Override
    public void onUpdatePages(PageContent[] pageContent) {
    }

    @Override
    public void onUpdateSource(List<Entities.BookSource> list) {
        mAdapter.setNewData(list);
        if (list == null) {
            mEmptyView.setText(R.string.network_error_tips);
        } else if (list.size() == 0) {
            mEmptyView.setText(R.string.empty_data);
        }
        mAdapter.setEmptyView(mEmptyView);
    }

    @Override
    public void setPresenter(ReadContract.Presenter presenter) {
    }

    private class Adapter extends BaseQuickAdapter<Entities.BookSource, CommonViewHolder> {

        public Adapter() {
            super(R.layout.item_view_book_source);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.BookSource item) {
            final boolean isChecked = TextUtils.equals(item.host, mBook.curSourceHost);
            holder.setText(R.id.chapter_title, item.lastChapter)
                    .setText(R.id.source_host, item.host)
                    .setText(R.id.update_time, AppUtils.getDescriptionTimeFromDateString(item.updated))
                    .setGone(R.id.cur_check_source, isChecked)
                    .setGone(R.id.first_letter_tv, true)
                    .setText(R.id.first_letter_tv, item.host.substring(0, 1).toUpperCase());
            holder.itemView.setBackgroundResource(R.drawable.touch_bg);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isChecked) {
                        finish();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.change_source_dialog_title)
                            .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mBook.sourceId = item._id;
                                    mBook.curSourceHost = item.host;
                                    BookProvider.insertOrUpdate(mBook, false);
                                    AppSettings.deleteChapterList(mBook._id);
                                    FileUtils.deleteBookDir(mBook._id);
                                    Intent data = new Intent();
                                    data.putExtra(ReadActivity.EXTRA_BOOK, mBook);
                                    setResult(2, data);
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
        }
    }
}
