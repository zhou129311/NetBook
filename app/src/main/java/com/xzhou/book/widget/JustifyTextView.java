package com.xzhou.book.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

public class JustifyTextView extends android.support.v7.widget.AppCompatTextView {

    private int mLineY;
    private int mViewWidth;

    public JustifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getMaxLineCount() {
        return (getMeasuredHeight() - (int) (getTextSize() * 0.5f)) / getLineHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setSubpixelText(true);
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth();
        mLineY = 0;
        mLineY += getTextSize() * 1.5;

        Layout layout = getLayout();
        if (layout == null) {
            return;
        }
        String text = (String) getText();
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String line = text.substring(lineStart, lineEnd);

            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            if (needScale(line) && i < layout.getLineCount() - 1) {
                drawScaledText(canvas, line, width);
            } else {
                canvas.drawText(line, 0, mLineY, paint);
            }
            mLineY += getLineHeight();
        }
    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth) {
        float x = 0;
        if (isFirstLineOfParagraph(line)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(3);
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288
                && line.charAt(1) == 12288) {
            String substring = line.substring(0, 2);
            float cw = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, x, mLineY, getPaint());
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / line.length() - 1;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }
    }

    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    private boolean needScale(String line) {
        if (line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }

}