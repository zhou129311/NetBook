package com.xzhou.book.common;

import android.annotation.StringRes;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.read.BookTocDialog;
import com.xzhou.book.utils.AppUtils;

import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

public class ItemDialog extends Dialog {
    private ListView mListView;
    private View mView;
    private TextView mTitleView;
    private TextView mCancelTv;
    private Adapter mAdapter;
    private boolean mIsSingleChoice;

    private ItemDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    @Override
    public void show() {
        super.show();
        mListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams lp = mView.getLayoutParams();
                int marginH = getContext().getResources().getDimensionPixelSize(R.dimen.dialog_margin) * 2;
                int divH = mIsSingleChoice ? (AppUtils.dip2px(40) + mCancelTv.getHeight()) : AppUtils.dip2px(21);
                int otherH = mTitleView.getHeight() + divH + marginH; //除去listview之外的其他部分高度
                int height = BookTocDialog.getListViewHeightBasedOnChildren(otherH, mListView, mAdapter);
                if (height < lp.height) {
                    lp.height = height;
                    setContentView(mView, lp);
                }
            }
        });
    }

    public static class Builder {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private boolean mCancelable;
        private CharSequence mTitle;
        private DialogInterface.OnClickListener mOnClickListener;
        private CharSequence[] mItems;
        private boolean mIsSingleChoice;
        private int mCheckedItem;
        private DialogInterface.OnClickListener mNegativeButtonListener;
        private CharSequence mNegativeButtonText;

        public Builder(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setItems(CharSequence[] items, final OnClickListener listener) {
            mItems = items;
            mOnClickListener = listener;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, final OnClickListener listener) {
            mItems = items;
            mOnClickListener = listener;
            mCheckedItem = checkedItem;
            mIsSingleChoice = true;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId, final OnClickListener listener) {
            mNegativeButtonText = mContext.getText(textId);
            mNegativeButtonListener = listener;
            return this;
        }

        private ItemDialog createSingleChoice() {
            final ItemDialog dialog = new ItemDialog(mContext);
            dialog.mIsSingleChoice = true;
            dialog.setCancelable(mCancelable);
            if (mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.mView = mInflater.inflate(R.layout.dialog_choice_item, null);
            dialog.mTitleView = dialog.mView.findViewById(R.id.title_tv);
            if (mTitle != null) {
                dialog.mTitleView.setText(mTitle);
            }
            dialog.mCancelTv = dialog.mView.findViewById(R.id.cancel_tv);
            if (mNegativeButtonText != null) {
                dialog.mCancelTv.setText(mNegativeButtonText);
            } else {
                dialog.mCancelTv.setVisibility(View.GONE);
            }
            dialog.mListView = dialog.mView.findViewById(R.id.list_view);
            dialog.mListView.setChoiceMode(CHOICE_MODE_SINGLE);

            return dialog;
        }

        private ItemDialog create() {
            final ItemDialog dialog = new ItemDialog(mContext);
            dialog.setCancelable(mCancelable);
            if (mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.mView = mInflater.inflate(R.layout.dialog_item, null);
            dialog.mTitleView = dialog.mView.findViewById(R.id.title_tv);
            if (mTitle != null) {
                dialog.mTitleView.setText(mTitle);
            }
            dialog.mAdapter = new Adapter(mItems, mInflater);
            dialog.mListView = dialog.mView.findViewById(R.id.list_view);
            dialog.mListView.setAdapter(dialog.mAdapter);
            dialog.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(dialog, position);
                    }
                }
            });
            View decorView = ((Activity) mContext).getWindow().getDecorView();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.width = (int) (decorView.getWidth() * 0.85);
            lp.height = (int) (decorView.getHeight() * 0.9);
            dialog.setContentView(dialog.mView, lp);
            return dialog;
        }

        public void show() {
            final ItemDialog dialog = create();
            dialog.show();
        }
    }

    private static class Adapter extends BaseAdapter {
        private CharSequence[] mItems;
        private final LayoutInflater mInflater;

        Adapter(CharSequence[] items, LayoutInflater inflater) {
            mInflater = inflater;
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) mInflater.inflate(R.layout.item_view_dialog_text, parent, false);
            }
            textView.setText(mItems[position]);
            return textView;
        }
    }
}
