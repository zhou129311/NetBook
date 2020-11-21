package com.xzhou.book.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;

import com.xzhou.book.R;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-6-3
 * Change List:
 */
public class SingleCheckGroup extends ViewGroup {
    private int horizontalSpacing;
    private int verticalSpacing;
    private int horizontalPadding;
    private int verticalPadding;
    private int checkIndex;

    public interface OnTagCheckedListener {
        void onTagChecked(int index, String value);
    }

    private InternalTagClickListener mInternalTagClickListener = new InternalTagClickListener();

    private OnTagCheckedListener mTagCheckedListener;

    public SingleCheckGroup(Context context) {
        this(context, null);
    }

    public SingleCheckGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tagGroupStyle);
    }

    public SingleCheckGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float default_horizontal_spacing = dp2px(8.0f);
        float default_vertical_spacing = dp2px(4.0f);
        float default_horizontal_padding = dp2px(15.0f);
        float default_vertical_padding = dp2px(3.0f);

        // Load styled attributes.
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagGroup, defStyleAttr, R.style.TagGroup);
        try {
            horizontalSpacing = (int) a.getDimension(R.styleable.TagGroup_atg_horizontalSpacing, default_horizontal_spacing);
            verticalSpacing = (int) a.getDimension(R.styleable.TagGroup_atg_verticalSpacing, default_vertical_spacing);
            horizontalPadding = (int) a.getDimension(R.styleable.TagGroup_atg_horizontalPadding, default_horizontal_padding);
            verticalPadding = (int) a.getDimension(R.styleable.TagGroup_atg_verticalPadding, default_vertical_padding);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        int row = 0; // The row counter.
        int rowWidth = 0; // Calc the current row width.
        int rowMaxHeight = 0; // Calc the max tag height, in current row.

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            if (child.getVisibility() != GONE) {
                rowWidth += childWidth;
                if (rowWidth > widthSize) { // Next line.
                    rowWidth = childWidth; // The loadNextPage row width.
                    height += rowMaxHeight + verticalSpacing;
                    rowMaxHeight = childHeight; // The loadNextPage row max height.
                    row++;
                } else { // This line.
                    rowMaxHeight = Math.max(rowMaxHeight, childHeight);
                }
                rowWidth += horizontalSpacing;
            }
        }
        // Account for the last row height.
        height += rowMaxHeight;

        // Account for the padding too.
        height += getPaddingTop() + getPaddingBottom();

        // If the tags grouped in one row, set the width to wrap the tags.
        if (row == 0) {
            width = rowWidth;
            width += getPaddingLeft() + getPaddingRight();
        } else {// If the tags grouped exceed one line, set the width to match the parent.
            width = widthSize;
        }

        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        int childLeft = parentLeft;
        int childTop = parentTop;

        int rowMaxHeight = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            if (child.getVisibility() != GONE) {
                if (childLeft + width > parentRight) { // Next line
                    childLeft = parentLeft;
                    childTop += rowMaxHeight + verticalSpacing;
                    rowMaxHeight = height;
                } else {
                    rowMaxHeight = Math.max(rowMaxHeight, height);
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);

                childLeft += width + horizontalSpacing;
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.tags = getTags();
        ss.input = getCheckedTagText();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setTags(0, ss.tags);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final TagView tagView = getTagAt(i);
            tagView.setChecked(ss.input.equals(tagView.getText().toString()));
        }
    }

    public void setTagCheckedListener(OnTagCheckedListener listener) {
        mTagCheckedListener = listener;
    }

    public String[] getTags() {
        final int count = getChildCount();
        final List<String> tagList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final TagView tagView = getTagAt(i);
            tagList.add(tagView.text);
        }

        return tagList.toArray(new String[0]);
    }

    public void setTags(int checkIndex, List<String> tagList) {
        setTags(checkIndex, tagList.toArray(new String[0]));
    }

    public void setTags(int checkIndex, String... tags) {
        removeAllViews();
        for (int i = 0; i < tags.length; i++) {
            final String tag = tags[i];
            appendTag(tag, i);
        }
        getTagAt(checkIndex).setChecked(true);
    }

    protected void appendTag(String tag, int index) {
        final TagView newTag = new TagView(getContext(), tag, index);
        newTag.setOnClickListener(mInternalTagClickListener);
        addView(newTag);
    }

    protected TagView getTagAt(int index) {
        return (TagView) getChildAt(index);
    }

    public String getCheckedTagText() {
        return getTagAt(checkIndex).text;
    }

    public String getCheckedTag() {
        return (String) getTagAt(checkIndex).getTag();
    }


    public float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public float sp2px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new TagGroup.LayoutParams(getContext(), attrs);
    }

    /**
     * Per-child layout information for layouts.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    /**
     * For {@link TagGroup} save and restore state.
     */
    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int tagCount;
        String[] tags;
        String input;

        public SavedState(Parcel source) {
            super(source);
            tagCount = source.readInt();
            tags = new String[tagCount];
            source.readStringArray(tags);
            input = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            tagCount = tags.length;
            dest.writeInt(tagCount);
            dest.writeStringArray(tags);
            dest.writeString(input);
        }
    }

    class InternalTagClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            final TagView curTag = (TagView) v;
            final TagView preCheckTag = getTagAt(checkIndex);
            if (preCheckTag == curTag) {
                return;
            }
            checkIndex = curTag.index;
            preCheckTag.setChecked(false);
            curTag.setChecked(!curTag.isChecked);
            if (mTagCheckedListener != null) {
                mTagCheckedListener.onTagChecked(checkIndex, (String) curTag.getTag());
            }
        }
    }

    class TagView extends AppCompatTextView {
        private boolean isChecked = false;
        private String text;
        private int index;

        public TagView(Context context, String text, int index) {
            super(context);
            this.text = text;
            this.index = index;
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            setLayoutParams(new TagGroup.LayoutParams(
                    TagGroup.LayoutParams.WRAP_CONTENT,
                    TagGroup.LayoutParams.WRAP_CONTENT));

            setGravity(Gravity.CENTER);
            String[] em = text.split(",");
            setText(em[0]);
            if (em.length == 1) {
                setTag("");
            } else {
                setTag(em[1]);
            }
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            setBackgroundResource(R.drawable.tag_text_bg);
            ColorStateList colorStateList = getResources().getColorStateList(R.color.tag_text_color);
            setTextColor(colorStateList);
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
            setActivated(checked);
        }
    }
}