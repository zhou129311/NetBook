package com.xzhou.book.common;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.xzhou.book.R;

public class ItemDialog extends Dialog {

    private ItemDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }


}
