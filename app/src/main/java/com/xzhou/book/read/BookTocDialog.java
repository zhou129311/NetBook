package com.xzhou.book.read;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.BookManager;
import com.xzhou.book.R;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookTocDialog extends Dialog {

    @BindView(R.id.toc_title)
    TextView mTocTitle;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private List<Entities.Chapters> mChaptersList;
    private int mCurChapter;
    private Adapter mAdapter;
    private BookManager.LocalBook mLocalBook;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onClickItem(int chapter, Entities.Chapters chapters);
    }

    public static BookTocDialog createDialog(Context context, List<Entities.Chapters> list, BookManager.LocalBook book) {
        return new BookTocDialog(context, list, book);
    }

    private BookTocDialog(@NonNull Context context, List<Entities.Chapters> list, BookManager.LocalBook book) {
        super(context, R.style.TocDialogTheme);
        mChaptersList = list;
        mLocalBook = book;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_toc, null);
        ButterKnife.bind(this, view);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = (int) (AppUtils.getScreenWidth() * 0.85);
        lp.height = (int) (AppUtils.getScreenHeight() * 0.9);
        mTocTitle.setText(mLocalBook.title);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration(true, 0, 0));
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(context));
        mAdapter = new Adapter(mChaptersList);
        mAdapter.bindToRecyclerView(mRecyclerView);
        addContentView(view, lp);
        setCanceledOnTouchOutside(true);
    }

    public void setCurChapter(int curChapter) {
        mCurChapter = curChapter;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mItemClickListener = null;
    }

    private class Adapter extends BaseQuickAdapter<Entities.Chapters, CommonViewHolder> {

        Adapter(@Nullable List<Entities.Chapters> data) {
            super(R.layout.item_view_toc, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.Chapters item) {
            TextView textView = holder.getView(R.id.toc_tv);
            final int pos = holder.getAdapterPosition();
            if (mCurChapter == pos) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_activated, 0, 0, 0);
            } else if (FileUtils.hasCacheChapter(mLocalBook._id, pos)) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_download, 0, 0, 0);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_normal, 0, 0, 0);
            }
            textView.setText(item.title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onClickItem(pos, item);
                    }
                }
            });
        }
    }

}
