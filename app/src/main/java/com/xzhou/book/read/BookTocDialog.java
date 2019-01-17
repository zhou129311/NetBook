package com.xzhou.book.read;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookTocDialog extends Dialog {

    @BindView(R.id.toc_title)
    TextView mTocTitle;
    @BindView(R.id.list_view)
    ListView mListView;
    private View mView;

    private List<Entities.Chapters> mChaptersList;
    private int mCurChapter;
    private Adapter mAdapter;
    private BookProvider.LocalBook mLocalBook;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onClickItem(int chapter, Entities.Chapters chapters);
    }

    public static BookTocDialog createDialog(Context context, List<Entities.Chapters> list, BookProvider.LocalBook book) {
        return new BookTocDialog(context, list, book);
    }

    private BookTocDialog(@NonNull Context context, List<Entities.Chapters> list, BookProvider.LocalBook book) {
        super(context, R.style.DialogTheme);
        mChaptersList = list;
        mLocalBook = book;
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_toc, null);
        ButterKnife.bind(this, mView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = (int) (AppUtils.getScreenWidth() * 0.9);
        lp.height = (int) (AppUtils.getScreenHeight() * 0.9);
        mTocTitle.setText(mLocalBook.title);
        mAdapter = new Adapter(context);
        mListView.setAdapter(mAdapter);
        setContentView(mView, lp);
        setCanceledOnTouchOutside(true);
    }

    public void setCurChapter(int curChapter) {
        mCurChapter = curChapter;
        if (isShowing()) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public void show() {
        super.show();
        mListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams lp = mView.getLayoutParams();
                int otherH = mTocTitle.getHeight() + mView.getPaddingBottom() + mView.getPaddingTop() + 50;
                int height = getListViewHeightBasedOnChildren(otherH, mListView, mAdapter);
                if (height < lp.height) {
                    mListView.setFastScrollAlwaysVisible(false);
                    lp.height = height;
                    setContentView(mView, lp);
                } else {
                    mListView.setFastScrollAlwaysVisible(true);
                }
                mListView.setSelection(mCurChapter);
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mItemClickListener = null;
    }

    private class Adapter extends BaseAdapter {
        private LayoutInflater mInflater;

        Adapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mChaptersList.size();
        }

        @Override
        public Object getItem(int position) {
            return mChaptersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView textView;
            final Entities.Chapters item = mChaptersList.get(position);
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) mInflater.inflate(R.layout.item_view_toc, parent, false);
            }
            if (mCurChapter == position) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_activated, 0, 0, 0);
            } else if (item.hasLocal) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_download, 0, 0, 0);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_toc_item_normal, 0, 0, 0);
            }
            textView.setText(item.title);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onClickItem(position, item);
                    }
                }
            });
            return textView;
        }
    }

    /**
     * Dialog listView自适应高度变化
     */
    private int getListViewHeightBasedOnChildren(int otherViewHeight, ListView listView, BaseAdapter adapter) {
        int totalHeight = 0;
        View view = listView.getChildAt(0);
        if (view != null) {
            int count = adapter.getCount();
            totalHeight = view.getHeight() * count + listView.getDividerHeight() * count + otherViewHeight;
        }
        return totalHeight;
    }
}
