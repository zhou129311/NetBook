package com.xzhou.book.read;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppSettings;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookTocDialog extends AppCompatDialog {

    @BindView(R.id.toc_title)
    TextView mTocTitle;
    @BindView(R.id.toc_sort)
    ImageView mTocSortView;
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
        View decorView = ((Activity) context).getWindow().getDecorView();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = (int) (decorView.getWidth() * 0.85);
        lp.height = (int) (decorView.getHeight() * 0.85);
        mAdapter = new Adapter(context);
        mListView.setAdapter(mAdapter);
        setContentView(mView, lp);
        setCanceledOnTouchOutside(true);
    }

    @OnClick({R.id.toc_sort})
    public void onViewClicked(View view) {
        dismiss();
        final Activity activity = getOwnerActivity();
        if (activity instanceof ReadActivity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("确定倒序排列？")
                    .setMessage("倒序排列后会重置已记录的阅读章节")
                    .setPositiveButton(R.string.confirm, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Collections.reverse(mChaptersList);
                            AppSettings.saveChapterList(mLocalBook._id, mChaptersList);
                            AppSettings.saveReadProgress(mLocalBook._id, 0, 0);
                            ((ReadActivity) activity).refreshChapterList();
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
        }
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
                mTocTitle.setText(mLocalBook.getTitle());
            }
        });
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mCurChapter);
            }
        }, 200);
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
            textView.setText((position + 1) + ". " + item.title);
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
    public static int getListViewHeightBasedOnChildren(int otherViewHeight, ListView listView, BaseAdapter adapter) {
        int totalHeight = 0;
        View view = listView.getChildAt(0);
        if (view != null) {
            int count = adapter.getCount();
            totalHeight = view.getHeight() * count + listView.getDividerHeight() * count + otherViewHeight;
        }
        return totalHeight;
    }
}
