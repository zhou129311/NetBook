package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.xzhou.book.R;

import java.util.ArrayList;
import java.util.List;

public class JustifyTextView extends View {
    private static final String TAG = "JustifyTextView";

    /**
     * The text to be display.
     * Note: the text should not be null
     */
    private String mText;
    /**
     * The {@link TextPaint} to draw the text.
     */
    private TextPaint mTextPaint;
    /**
     * The space between the lines.
     */
    private float mLineSpace;
    /**
     * wordwrap feature
     */
    private boolean isWordWrap;
    /**
     * the alignment for the text object displayed in the view.
     */
    private Align mAlign;

    private Layout mLayout;

    public JustifyTextView(Context context) {
        this(context, null);
    }

    public JustifyTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JustifyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public JustifyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.density = getResources().getDisplayMetrics().density;
        mLineSpace = 0;
        isWordWrap = true;
        mAlign = Align.LEFT;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.JustifyTextView, defStyleAttr, defStyleRes);

        CharSequence cs = a.getText(R.styleable.JustifyTextView_android_text);
        if (cs != null) mText = cs.toString();

        int textSize = a.getDimensionPixelSize(R.styleable.JustifyTextView_android_textSize, 20);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        int color = a.getColor(R.styleable.JustifyTextView_android_textColor, 0x8A000000);
        setTextColor(color);

        a.recycle();
    }

    /**
     * Set the text displayed in the {@link JustifyTextView}
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }

        mText = text;

        if (mLayout != null) {
            checkForRelayout();
        }
    }

    /**
     * Return the text the AlignTextView is displaying.
     */
    public String getText() {
        return mText;
    }

    /**
     * Returns the length of the text managed by this AlignTextView
     */
    public int length() {
        return mText.length();
    }

    /**
     * @return the base paint used for the text.  Please use this only to
     * consult the Paint's properties and not to change them.
     */
    public TextPaint getPaint() {
        return mTextPaint;
    }

    /**
     * Return the line space in the text which displayed in this view
     */
    public float getLineSpace() {
        return mLineSpace;
    }

    /**
     * Set the line space in the text which displayed in the view
     */
    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
    }

    /**
     * @return the extent by which text is currently being letter-spaced.
     * This will normally be 0.
     * @see #setLetterSpacing(float)
     * @see Paint#setLetterSpacing
     */
    public float getLetterSpacing() {
        return mTextPaint.getLetterSpacing();
    }

    /**
     * Sets text letter-spacing.  The value is in 'EM' units.  Typical values
     * for slight expansion will be around 0.05.  Negative values tighten text.
     *
     * @see #getLetterSpacing()
     * @see Paint#getLetterSpacing
     */
    public void setLetterSpacing(float letterSpacing) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (letterSpacing != mTextPaint.getLetterSpacing()) {
                mTextPaint.setLetterSpacing(letterSpacing);

                if (mLayout != null) {
                    nullLayout();
                    requestLayout();
                    invalidate();
                }
            }
        }
    }

    /**
     * Turn on/off the wordwrap feature for the text object
     */
    public void setWordWrap(boolean wordWrap) {
        if (wordWrap != isWordWrap) {
            isWordWrap = wordWrap;

            if (mLayout != null) {
                nullLayout();
                requestLayout();
                invalidate();
            }
        }
    }

    /**
     * @return whether the wordwrap feature is turned on/off for the text object
     */
    public boolean isWordWrap() {
        return isWordWrap;
    }

    /**
     * Set the alignment which the text should be displayed
     *
     * @param align {@link Align}
     */
    public void setAlign(Align align) {
        if (align != mAlign) {
            mAlign = align;

            if (mLayout != null) {
                nullLayout();
                requestLayout();
                invalidate();
            }
        }
    }

    /**
     * Return the align
     */
    public Align getAlign() {
        return mAlign;
    }

    /**
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants, so you may need to use
     * {@link #setTypeface(Typeface, int)} to get the appearance
     * that you actually want.
     *
     * @see #getTypeface()
     */
    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);

            relayout();
        }
    }

    /**
     * @return the current typeface and style in which the text is being
     * displayed.
     * @see #setTypeface(Typeface)
     */
    public Typeface getTypeface() {
        return mTextPaint.getTypeface();
    }

    /**
     * Sets the typeface and style in which the text should be displayed,
     * and turns on the fake bold and italic bits in the Paint if the
     * Typeface that you provided does not have all the bits in the
     * style that you specified.
     */
    public void setTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setTypeface(tf);
            //
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setTypeface(tf);
        }
    }

    /**
     * Set the text displayed in the screen is bold or not
     *
     * @param bold true if the text displayed in the screen need to be bold, otherwise false.
     */
    public void setTextBold(boolean bold) {
        mTextPaint.setFakeBoldText(bold);
    }

    /**
     * Whether the text displayed in the screen is bold.
     */
    public boolean getTextBold() {
        return mTextPaint.isFakeBoldText();
    }

    /**
     * Set to underline the text, or to clear the underlines.
     *
     * @param underline true to underline the text, false to clear the underlines.
     */
    public void setUnderlineText(boolean underline) {
        if (mTextPaint.isUnderlineText() != underline) {
            mTextPaint.setUnderlineText(underline);

            if (mLayout != null) {
                nullLayout();
                requestLayout();
                invalidate();
            }
        }
    }

    /**
     * @return whether the text is underlined.
     */
    public boolean isUnderlineText() {
        return mTextPaint.isUnderlineText();
    }

    /**
     * Set the default text size to the given value, interpreted as "scaled
     * pixel" units.  This size is adjusted based on the current density and
     * user font size preference.
     *
     * @param size The scaled pixel size.
     */
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    /**
     * Set the default text size to a given unit and value.  See {@link
     * TypedValue} for the possible dimension units.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     */
    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        setRawTextSize(TypedValue.applyDimension(
                unit, size, r.getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (size != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(size);
            relayout();
        }
    }

    /**
     * Sets the text color to be this color.
     *
     * @param color A color value in the form 0xAARRGGBB.
     *              Do not pass a resource ID. To get a color value from a resource ID, call
     *              {@link android.support.v4.content.ContextCompat#getColor(Context, int) getColor}.
     * @attr ref android.R.styleable#JustifyTextView_textColor
     * @see #getTextColor()
     */
    public void setTextColor(int color) {
        if (color != mTextPaint.getColor()) {
            mTextPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * Return the current color selected for normal text.
     *
     * @return Returns the current text color.
     */
    public int getTextColor() {
        return mTextPaint.getColor();
    }

    /**
     * Set the {@link #mLayout} to null.
     */
    private void nullLayout() {
        mLayout = null;
    }

    /**
     * Relayout. If the layout is not null, null it. Then, schedule a layout pass of the view tree.
     */
    private void relayout() {
        if (mLayout != null) {
            nullLayout();
            requestLayout();
            invalidate();
        }
    }

    /**
     * Make a new {@link Layout} for this view
     *
     * @param width the width for a layout
     */
    private void makeNewLayout(int width) {
        mLayout = Layout.make(mText, mTextPaint, width, mLineSpace, mAlign, isWordWrap);
    }

    /**
     * Return the desired height from the layout
     *
     * @param layout {@link Layout}
     * @return the desired height
     */
    private int getDesiredHeight(Layout layout) {
        if (layout == null) {
            return 0;
        }

        int pad = getPaddingTop() + getPaddingBottom();
        int desired = layout.getHeight();

        desired += pad;

        // Check against our minimum height
        desired = Math.max(desired, getSuggestedMinimumHeight());

        return desired;
    }

    /**
     * Make a new layout based on the already-measured size of the view, on the assumption
     * that it was measured correctly as some point.
     */
    private void assumeLayout() {
        int width = getRight() - getLeft() - getPaddingLeft() - getPaddingRight();

        if (width < 1) {
            width = 0;
        }

        makeNewLayout(width);
    }

    /**
     * Check whether entirely new text requires a new view layout or merely a new text layout.
     */
    private void checkForRelayout() {
        // If we have a fixed width, we can just swap in a new text layout
        // if the text height stays the same or if the view height is fixed.

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Static width, so try making a new text layout

            int oldht = mLayout.getHeight();
            int want = mLayout.getWidth();

            makeNewLayout(want);

            if (mLayout.getHeight() == oldht) {
                invalidate();
                return;
            }

            // We lose: the height has changed and we have a dynamic height.
            // Request a new view layout using our new text layout
            requestLayout();
            invalidate();
        } else {
            // Dynamic width, so we have no choice but to request a new
            // view layout width a new text layout
            relayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            float desired = Layout.getDesiredWidth(mText, mTextPaint);
            width = (int) Math.ceil(desired) + getPaddingLeft() + getPaddingRight();
            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        if (mLayout == null) {
            makeNewLayout(width - getPaddingLeft() - getPaddingRight());
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be, So be it.
            height = heightSize;
        } else {
            int desired = getDesiredHeight(mLayout);
            height = desired;

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desired, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLayout == null) {
            assumeLayout();
        }

        canvas.save();
        // translate canvas for padding left and padding top
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.drawColor(Color.TRANSPARENT);
        mLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (left != getPaddingLeft() ||
                right != getPaddingRight() ||
                top != getPaddingTop() ||
                bottom != getPaddingBottom()) {
            nullLayout();
        }

        // the super call will requestLayout()
        super.setPadding(left, top, right, bottom);
        invalidate();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if (start != getPaddingStart() ||
                end != getPaddingEnd() ||
                top != getPaddingTop() ||
                bottom != getPaddingBottom()) {
            nullLayout();
        }

        // the super call will requestLayout()
        super.setPaddingRelative(start, top, end, bottom);
        invalidate();
    }

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * A class that manages text layout in visual elements on the screen.
     */
    private static class Layout {

        /**
         * The constructor for {@link Layout} class
         *
         * @param text      the text to render
         * @param paint     the default paint for this layout
         * @param width     the wrapping width for the text
         * @param lineSpace the y-spacing between lines for this layout
         */
        private Layout(String text, TextPaint paint, int width, float lineSpace, Align align, boolean wordwrap) {
            mText = text;
            mPaint = paint;
            mWidth = width;
            mLineSpace = lineSpace;
            mAlign = align;
            isWordwrap = wordwrap;
        }

        /**
         * Make a new {@link Layout}
         *
         * @param text      the text to render
         * @param paint     the paint for this layout to measure the text
         * @param width     the wrapping width for the text
         * @param lineSpace the y-spacing between lines for this layout
         * @param align     alignment for the text
         * @return a new Layout instance
         */
        public static Layout make(String text, TextPaint paint, int width, float lineSpace, Align align, boolean wordwrap) {
            Layout layout = new Layout(text, paint, width, lineSpace, align, wordwrap);
            layout.measure();
            return layout;
        }

        private void measure() {
            if (isWordwrap) {
                mSplitedLines = splitText(mPaint, mText, mWidth);
            } else {
                mSplitedLines = new ArrayList<>();
                mSplitedLines.add(mText);
            }

            mLineCount = mSplitedLines.size();
        }

        /**
         * Return how wide a layout must be in order to display the
         * specified text slice with one line per paragraph.
         *
         * @param source the string
         * @param paint  the character paint
         * @return the desired width
         */
        public static float getDesiredWidth(String source, TextPaint paint) {
            float need = 0;

            int next;
            int end = source.length();
            for (int i = 0; i <= end; i = next) {
                next = TextUtils.indexOf(source, '\n', i, end);

                if (next < 0)
                    next = end;

                float w = measureWidth(source.substring(i, next), paint);

                if (w > need)
                    need = w;

                next++;
            }

            return need;
        }

        /**
         * Measure the width of the string
         *
         * @param source the string to be measured
         * @param paint  the character paint
         * @return the width of the string
         */
        public static float measureWidth(String source, TextPaint paint) {
            if (source == null) {
                throw new IllegalArgumentException("the text to be measured is null.");
            }

            return paint.measureText(source);
        }

        /**
         * Draw this layout on the specified canvas
         *
         * @param canvas the canvas to display the text
         */
        public void draw(Canvas canvas) {

            float lineHeight = getLineHeight() + getLineSpace();
            float baseline = Math.abs(mPaint.getFontMetrics().ascent);

            int x = 0;
            for (int i = 0; i < mLineCount; i++) {
                if (mAlign == Align.LEFT) {
                    x = 0;
                } else if (mAlign == Align.CENTER) {
                    x = (getWidth() - (int) mPaint.measureText(mSplitedLines.get(i))) / 2;
                } else if (mAlign == Align.RIGHT) {
                    x = getWidth() - (int) mPaint.measureText(mSplitedLines.get(i));
                }

                canvas.drawText(mSplitedLines.get(i), x, lineHeight * i + baseline, mPaint);
            }
        }

        /**
         * Iterate a original string to parse it and split it.
         *
         * @param paint  the text paint used to measure text
         * @param source the string to be iterated
         * @return a list of string
         */
        private List<String> splitText(TextPaint paint, String source, int width) {
            List<String> origin = new ArrayList<>();
            if (source.contains("\n")) {
                int index;
                do {
                    if (source.startsWith("\n")) {
                        origin.add("");
                        source = source.substring(1);
                        continue;
                    }
                    index = source.indexOf("\n");
                    origin.add(source.substring(0, index));
                    source = source.substring(index + 1);
                } while (source.contains("\n") && source.length() > 0);
                if (source.length() > 0) {
                    origin.add(source);
                }
            } else {
                origin.add(source);
            }

            List<String> strList = new ArrayList<>();

            for (String s : origin) {
                // "\r\n"
                if (s.length() > 0 && s.charAt(s.length() - 1) == '\r') {
                    s = s.substring(0, s.length() - 1);
                }

                while (true) {
                    int position = paint.breakText(s, 0, s.length(), true, width, null);
                    if (position == 0 || position == s.length()) {
                        strList.add(s);
                        break;
                    } else if (position < s.length()) {
                        strList.add(s.substring(0, position));
                        s = s.substring(position);
                    }
                }
            }

            return strList;
        }

        /**
         * Return the width for this layout
         */
        public final int getWidth() {
            return mWidth;
        }

        /**
         * Return the height for this layout
         */
        public int getHeight() {
            return (int) (mLineCount * (getLineHeight() + getLineSpace()));
        }

        /**
         * Return the line space for this layout
         */
        public float getLineSpace() {
            return mLineSpace == 0 ? getDefaultLineSpace() : mLineSpace;
        }

        /**
         * Return the default line space for this layout
         */
        public float getDefaultLineSpace() {
            return 0;
        }

        /**
         * Return the line height for this layout
         */
        public int getLineHeight() {
            return mPaint.getFontMetricsInt(null);
        }

        /**
         * Return the number of lines of text in this layout
         */
        public int getLineCount() {
            return mLineCount;
        }

        /**
         * The text to be displayed on the screen.
         */
        private String mText;
        /**
         * Paint used to draw text, should not be null.
         */
        private TextPaint mPaint;
        /**
         * The line Spacing between lines.
         */
        private float mLineSpace;
        /**
         * The wrapping width for the text.
         */
        private int mWidth;
        /**
         * The line count of the text displaying on the screen.
         */
        private int mLineCount;
        /**
         * alignment for the text
         */
        private Align mAlign;
        /**
         * wordwrap feature
         */
        private boolean isWordwrap;

        private List<String> mSplitedLines;
    }
}