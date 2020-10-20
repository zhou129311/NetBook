package com.xzhou.book.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.xzhou.book.R;
import com.xzhou.book.common.FiltrateAdapter;
import com.xzhou.book.community.DiscussActivity;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Constant.SortType;
import com.xzhou.book.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * ToolBar下面的过滤条
 */
public class MultiSortLayout extends LinearLayout {
    private static final String TAG = "MultiSortLayout";
    private OnSelectListener mListener;
    private HashMap<String, String> mParams = new HashMap<>();
    private Toolbar mToolbar;

    public interface OnSelectListener {
        void onSortSelect(HashMap<String, String> params);
    }

    public void setOnSelectListener(OnSelectListener listener) {
        mListener = listener;
    }

    public MultiSortLayout(Context context) {
        super(context);
    }

    public MultiSortLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiSortLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HashMap<String, String> getDefaultParams() {
        mParams.put(ZhuiShuSQApi.DISTILLATE, Constant.Distillate.ALL);
        mParams.put(ZhuiShuSQApi.TYPE, Constant.BookType.ALL);
        mParams.put(ZhuiShuSQApi.SORT, SortType.DEFAULT);
        return mParams;
    }

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    public static List<ChildData> getDataForDiscussionType(int type) {
        List<ChildData> list = new ArrayList<>();
        ChildData data1 = new ChildData();
        ChildData data2 = new ChildData();
        ChildData data3 = new ChildData();
        switch (type) {
        case DiscussActivity.TYPE_DISCUSS:
        case DiscussActivity.TYPE_HELP:
        case DiscussActivity.TYPE_GIRL:
            data1.key = ZhuiShuSQApi.DISTILLATE;
            data1.data = new String[]{AppUtils.getString(R.string.distillate_false), AppUtils.getString(R.string.distillate_true)};
            data1.dataParams = new String[]{Constant.Distillate.ALL, Constant.Distillate.DISTILLATE};
            data2.key = ZhuiShuSQApi.SORT;
            data2.data = new String[]{AppUtils.getString(R.string.sort_default),
                    AppUtils.getString(R.string.sort_created),
                    AppUtils.getString(R.string.sort_comment_count),
            };
            data2.dataParams = new String[]{SortType.DEFAULT, SortType.CREATED, SortType.COMMENT_COUNT};
            list.add(data1);
            list.add(data2);
            break;
        case DiscussActivity.TYPE_REVIEWS:
            data1.key = ZhuiShuSQApi.DISTILLATE;
            data1.data = new String[]{AppUtils.getString(R.string.distillate_false), AppUtils.getString(R.string.distillate_true)};
            data1.dataParams = new String[]{Constant.Distillate.ALL, Constant.Distillate.DISTILLATE};
            data2.key = ZhuiShuSQApi.TYPE;
            data2.data = Constant.bookTypes.toArray(new String[0]);
            data2.dataParams = Constant.bookTypeParams.toArray(new String[0]);
            data3.key = ZhuiShuSQApi.SORT;
            data3.data = new String[]{AppUtils.getString(R.string.sort_default),
                    AppUtils.getString(R.string.sort_created),
                    AppUtils.getString(R.string.sort_helpful),
                    AppUtils.getString(R.string.sort_comment_count),
            };
            data3.dataParams = new String[]{SortType.DEFAULT, SortType.CREATED, SortType.HELPFUL, SortType.COMMENT_COUNT};
            list.add(data1);
            list.add(data2);
            list.add(data3);
            break;
        }
        return list;
    }

    public void setDataType(int type) {
        List<ChildData> data = getDataForDiscussionType(type);
        if (data != null && data.size() > 0) {
            for (ChildData childData : data) {
                if (childData.hasInvalid()) {
                    Log.e(TAG, childData + " is invalid!");
                    continue;
                }
                mParams.put(childData.key, childData.dataParams[0]);
                int width = AppUtils.getScreenWidth() / data.size();
                ChildView childView = new ChildView(getContext());
                childView.setData(childData);
                LayoutParams lp = new LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER_VERTICAL;
                addView(childView, lp);
            }
        }
    }

    public static class ChildData {
        String key; //distillate 、type 、sort
        String[] data;
        String[] dataParams;

        private boolean hasInvalid() {
            return data == null || data.length < 1 || dataParams == null || data.length != dataParams.length;
        }

        @Override
        public String toString() {
            return "ChildData{" +
                    "key='" + key + '\'' +
                    ", data=" + Arrays.toString(data) +
                    ", dataParams=" + Arrays.toString(dataParams) +
                    '}';
        }
    }

    private class ChildView extends DrawableButton {

        private ChildData mChildData;
        private ListPopupWindow mListPopupWindow;
        private FiltrateAdapter mFiltrateAdapter;

        public ChildView(Context context) {
            super(context);
            setBackgroundResource(R.drawable.bg_color_primary_click);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isActivated()) {
                        closePopWindow();
                        setActivated(false);
                    } else {
                        openPopupWindow();
                        setActivated(true);
                    }
                }
            });
        }

        private void setData(ChildData data) {
            mChildData = data;
            setText(data.data[0]);
            setPadding(0, AppUtils.dip2px(12), 0, AppUtils.dip2px(12));
            setTag(data.key);
            setTextColor(AppUtils.getColor(R.color.white));
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            setCompoundDrawablePadding(AppUtils.dip2px(5));
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.sel_sort_arrow, 0);
        }

        private void openPopupWindow() {
            if (mChildData.hasInvalid()) {
                Log.e(TAG, mChildData + " is invalid! don't openPopupWindow()");
                return;
            }
            mListPopupWindow = createPopupWindow();
            mListPopupWindow.show();
        }

        private ListPopupWindow createPopupWindow() {
            if (mListPopupWindow == null) {
                mListPopupWindow = new ListPopupWindow(getContext());
                mFiltrateAdapter = new FiltrateAdapter(getContext(), Arrays.asList(mChildData.data));
                mFiltrateAdapter.setCheckedStyle(FiltrateAdapter.CHECK_TYPE_DISCUSSION);
                mListPopupWindow.setAdapter(mFiltrateAdapter);
                mListPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                mListPopupWindow.setAnchorView(mToolbar != null ? mToolbar : this);
                mListPopupWindow.setVerticalOffset(mToolbar != null ? -mToolbar.getHeight() : 0);
                mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mFiltrateAdapter.setChecked(position);
                        String title = mChildData.data[position];
                        setText(title);
                        mParams.put(mChildData.key, mChildData.dataParams[position]);
                        mListPopupWindow.dismiss();
                        if (mListener != null) {
                            mListener.onSortSelect(mParams);
                        }
                    }
                });
                mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        setActivated(false);
                    }
                });
                mListPopupWindow.setModal(true);
            }
            return mListPopupWindow;
        }

        public void closePopWindow() {
            if (mListPopupWindow != null && mListPopupWindow.isShowing()) {
                mListPopupWindow.dismiss();
            }
        }
    }
}
