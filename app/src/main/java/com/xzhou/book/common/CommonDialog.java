package com.xzhou.book.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;

import com.xzhou.book.read.BookTocDialog;

public class CommonDialog extends DialogFragment {

    private OnDialogCancelListener mCancelListener;

    private OnCallDialog mOnCallDialog;

    private BookTocDialog.OnItemClickListener mItemClickListener;

    public interface OnDialogCancelListener {
        void onCancel();
    }

    public interface OnCallDialog {
        Dialog getDialog(Context context);
    }

    public void setChapter(int chapter) {
        Bundle data = new Bundle();
        data.putInt("chapter", chapter);
        setArguments(data);
    }

    public void setOnItemClickListener(BookTocDialog.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public static CommonDialog newInstance(OnCallDialog call, boolean cancelable) {
        return newInstance(call, cancelable, null);
    }

    public static CommonDialog newInstance(OnCallDialog call, boolean cancelable, OnDialogCancelListener cancelListener) {
        CommonDialog instance = new CommonDialog();
        instance.setCancelable(cancelable);
        instance.mCancelListener = cancelListener;
        instance.mOnCallDialog = call;
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null == mOnCallDialog) {
            super.onCreateDialog(savedInstanceState);
        }
        return mOnCallDialog.getDialog(getActivity());
    }

    @Override
    public Dialog getDialog() {
        return super.getDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle data = getArguments();
        Dialog dialog = getDialog();
        if (data != null && dialog instanceof BookTocDialog) {
            BookTocDialog tocDialog = (BookTocDialog) dialog;
            int chapter = data.getInt("chapter", -1);
            if (chapter > -1) {
                tocDialog.setCurChapter(chapter);
            }
            tocDialog.setOnItemClickListener(mItemClickListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        Window window;
        if (dialog != null && (window = getDialog().getWindow()) != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0.6f;//弹窗背景透明度
            window.setAttributes(windowParams);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mItemClickListener = null;
        if (mCancelListener != null) {
            mCancelListener.onCancel();
        }
    }
}
