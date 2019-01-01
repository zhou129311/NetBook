package com.xzhou.book.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppUtils;

import java.util.List;

public class FiltrateAdapter extends BaseAdapter {
    public static final int CHECK_TYPE_TAB = 1;
    public static final int CHECK_TYPE_DISCUSSION = 2;

    private List<String> mList;
    private LayoutInflater mInflater;
    private int mCheckedPos;
    private int mMarginLeft;
    private int mCheckedType = CHECK_TYPE_TAB;

    public FiltrateAdapter(Context context, List<String> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    public void setMarginLeft(int dp) {
        mMarginLeft = dp;
    }

    public void setChecked(int position) {
        mCheckedPos = position;
        notifyDataSetChanged();
    }

    public void setCheckedStyle(int type) {
        mCheckedType = type;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_view_category_selected, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv.setText(mList.get(position));
        if (mCheckedPos == position) {
            holder.tv.setTextColor(AppUtils.getColor(R.color.colorPrimary));
            int vis = View.VISIBLE;
            if (mCheckedType == CHECK_TYPE_DISCUSSION) {
                vis = View.GONE;
            }
            holder.iv.setVisibility(vis);
        } else {
            int color = AppUtils.getColor(R.color.common_h1);
            if (mCheckedType == CHECK_TYPE_DISCUSSION) {
                color = AppUtils.getColor(R.color.common_h3);
            }
            holder.tv.setTextColor(color);
            holder.iv.setVisibility(View.GONE);
        }
        int leftMargin = position == 0 ? 0 : AppUtils.dip2px(mMarginLeft);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.tv.getLayoutParams();
        lp.leftMargin = leftMargin;
        holder.tv.setLayoutParams(lp);
        return convertView;
    }

    private class ViewHolder {
        TextView tv;
        ImageView iv;

        ViewHolder(View parent) {
            tv = parent.findViewById(R.id.category_title);
            iv = parent.findViewById(R.id.category_yes);
        }
    }
}
