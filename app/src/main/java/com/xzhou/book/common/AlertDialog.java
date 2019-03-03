package com.xzhou.book.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.xzhou.book.R;

public class AlertDialog extends AppCompatDialog {
    private TextView mMessageTv;
    private TextView mTitleTv;

    private AlertDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    public void setMessage(String msg) {
        mMessageTv.setText(msg);
        if (mMessageTv.getVisibility() != View.VISIBLE) {
            mMessageTv.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
        if (mTitleTv.getVisibility() != View.VISIBLE) {
            mTitleTv.setVisibility(View.VISIBLE);
        }
    }

    public static class Builder {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private DialogInterface.OnClickListener mPositiveButtonListener;
        private DialogInterface.OnClickListener mNegativeButtonListener;
        private boolean mCancelable;
        private CharSequence mTitle;
        private CharSequence mMessage;
        private CharSequence mPositiveButtonText;
        private CharSequence mNegativeButtonText;

        public Builder(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public Builder setTitle(@StringRes int textId) {
            mTitle = mContext.getText(textId);
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setMessage(@StringRes int textId) {
            mMessage = mContext.getText(textId);
            return this;
        }

        public Builder setPositiveButton(@StringRes int textId, final DialogInterface.OnClickListener listener) {
            mPositiveButtonText = mContext.getText(textId);
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(String text, final DialogInterface.OnClickListener listener) {
            mPositiveButtonText = text;
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(final DialogInterface.OnClickListener listener) {
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId, final OnClickListener listener) {
            mNegativeButtonText = mContext.getText(textId);
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(String text, final OnClickListener listener) {
            mNegativeButtonText = text;
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(final OnClickListener listener) {
            mNegativeButtonListener = listener;
            return this;
        }

        public AlertDialog create() {
            final AlertDialog dialog = new AlertDialog(mContext);
            dialog.setCancelable(mCancelable);
            if (mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            View view = mInflater.inflate(R.layout.dialog_alert, null);
            TextView title = view.findViewById(R.id.title_tv);
            dialog.mTitleTv = title;
            if (mTitle != null) {
                title.setText(mTitle);
            } else {
                title.setVisibility(View.GONE);
            }
            final TextView message = view.findViewById(R.id.message_tv);
            dialog.mMessageTv = message;
            if (mMessage != null) {
                message.setText(mMessage);
            } else {
                message.setVisibility(View.GONE);
            }
            TextView cancel = view.findViewById(R.id.cancel_tv);
            if (mNegativeButtonText != null) {
                cancel.setText(mNegativeButtonText);
            } else {
                cancel.setVisibility(View.GONE);
            }
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNegativeButtonListener != null) {
                        mNegativeButtonListener.onClick(dialog, Dialog.BUTTON_NEGATIVE);
                    }
                }
            });
            TextView ok = view.findViewById(R.id.ok_tv);
            if (mPositiveButtonText != null) {
                ok.setText(mPositiveButtonText);
            } else {
                ok.setVisibility(View.GONE);
            }
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPositiveButtonListener != null) {
                        mPositiveButtonListener.onClick(dialog, Dialog.BUTTON_POSITIVE);
                    }
                }
            });
            View decorView = ((Activity) mContext).getWindow().getDecorView();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.width = (int) (decorView.getWidth() * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.setContentView(view, lp);
            return dialog;
        }

        public void show() {
            final AlertDialog dialog = create();
            dialog.show();
        }
    }
}
