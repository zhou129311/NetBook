package com.xzhou.book.read;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

import java.util.List;

public class ReadPagerAdapter extends PagerAdapter {
    private List<ReadPage> mList;
    private @Constant.ReadTheme
    int mTheme = AppSettings.getReadTheme();

    public ReadPagerAdapter(List<ReadPage> list) {
        mList = list;
    }

    public int getRealCount() {
        return mList.size();
    }

    public void setReadTheme(int theme) {
        if (mTheme != theme) {
            mTheme = theme;
            for (ReadPage pager : mList) {
                pager.setReadTheme(mTheme);
            }
        }
    }

    public void setBattery(int battery) {
        for (ReadPage pager : mList) {
            pager.setBattery(battery);
        }
    }

    public ReadPage getItem(int position) {
        return mList.get(position % mList.size());
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ReadPage view = mList.get(position % mList.size());
        Log.i("instantiateItem::view = " + view + ",position=" + position);
        //view.reset();
        //view.setReadTheme(mTheme);
        //view.setLoadState(true);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        container.addView(view);
        return view;
    }
}
