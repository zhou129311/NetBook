package com.xzhou.book.read;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadSlideView extends LinearLayout {

    @BindView(R.id.source_recycler_view)
    RecyclerView mRecyclerView;

    private BookProvider.LocalBook mBook;
    private Adapter mAdapter;
    private ReadActivity mReadActivity;

    public ReadSlideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    public void setBook(BookProvider.LocalBook book, ReadActivity activity) {
        mBook = book;
        mReadActivity = activity;
    }

    public void setSource(List<Entities.BookSource> list) {
        mAdapter.setNewData(list);
    }

    @OnClick({ R.id.more_source_tv, R.id.discussion_item_tv, R.id.recommend_item_tv })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.more_source_tv:
            ReadSourceActivity.startActivity(getContext(), mBook);
            break;
        case R.id.discussion_item_tv:
            AppUtils.startDiscussionByBook(getContext(), mBook.title, mBook._id, 0);
            break;
        case R.id.recommend_item_tv:
            AppUtils.startRecommendByBook(getContext(), mBook._id);
            break;
        }
    }

    private class Adapter extends BaseQuickAdapter<Entities.BookSource, CommonViewHolder> {

        Adapter() {
            super(R.layout.item_view_book_source);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.BookSource item) {
            final boolean isChecked = TextUtils.equals(item.host, mBook.curSourceHost);
            holder.setText(R.id.chapter_title, item.lastChapter)
                    .setText(R.id.source_host, item.host)
                    .setText(R.id.update_time, AppUtils.getDescriptionTimeFromDateString(item.updated))
                    .setGone(R.id.cur_check_source, isChecked);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isChecked) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                                    mReadActivity.recreate();
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
