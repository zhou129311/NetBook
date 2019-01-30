package com.xzhou.book.common;

import android.annotation.StringRes;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.xzhou.book.R;

public class CheckDialog extends Dialog {

    private CheckDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    public interface OnPositiveClickListener {
        void onClick(DialogInterface dialog, boolean isChecked);
    }

    public static class Builder {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private OnPositiveClickListener mPositiveButtonListener;
        private DialogInterface.OnClickListener mNegativeButtonListener;
        private boolean mCancelable;
        private CharSequence mTitle;
        private CharSequence mCheckedMessage;
        private CharSequence mPositiveButtonText;
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

        public Builder setTitle(@StringRes int textId) {
            mTitle = mContext.getText(textId);
            return this;
        }

        public Builder setCheckedMessage(String message) {
            mCheckedMessage = message;
            return this;
        }

        public Builder setCheckedMessage(@StringRes int textId) {
            mCheckedMessage = mContext.getText(textId);
            return this;
        }

        public Builder setPositiveButton(@StringRes int textId, final OnPositiveClickListener listener) {
            mPositiveButtonText = mContext.getText(textId);
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(final OnPositiveClickListener listener) {
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId, final OnClickListener listener) {
            mNegativeButtonText = mContext.getText(textId);
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(final OnClickListener listener) {
            mNegativeButtonListener = listener;
            return this;
        }

        private CheckDialog create() {
            final CheckDialog dialog = new CheckDialog(mContext);
            dialog.setCancelable(mCancelable);
            if (mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            View view = mInflater.inflate(R.layout.dialog_checkbox, null);
            TextView title = view.findViewById(R.id.title_tv);
            if (mTitle != null) {
                title.setText(mTitle);
            }
            final CheckBox checkBox = view.findViewById(R.id.checkbox);
            if (mCheckedMessage != null) {
                checkBox.setText(mCheckedMessage);
            }
            TextView cancel = view.findViewById(R.id.cancel_tv);
            if (mNegativeButtonText != null) {
                cancel.setText(mNegativeButtonText);
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
            }
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPositiveButtonListener != null) {
                        mPositiveButtonListener.onClick(dialog, checkBox.isChecked());
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
            final CheckDialog dialog = create();
            dialog.show();
        }
    }
}
