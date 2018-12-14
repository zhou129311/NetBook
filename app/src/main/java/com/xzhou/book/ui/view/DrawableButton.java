package com.xzhou.book.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

public class DrawableButton extends AppCompatTextView {

    public DrawableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawable = getCompoundDrawables();
        Drawable leftDrawable = drawable[0];

        if (leftDrawable != null) {
            int drawableWidth = leftDrawable.getIntrinsicWidth();
            String text = getText().toString().trim();
            int bodyWidth = drawableWidth;
            if (!TextUtils.isEmpty(text)) {
                bodyWidth += getCompoundDrawablePadding() + (int) getPaint().getTextSize();
            }
            canvas.save();
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        }

        super.onDraw(canvas);
    }

}
