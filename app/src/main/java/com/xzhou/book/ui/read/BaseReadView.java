package com.xzhou.book.ui.read;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

public class BaseReadView extends View {
    private static final String TAG = "BaseReadView";

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected PointF mTouch = new PointF();

    public BaseReadView(Context context) {
        super(context);
    }


}
