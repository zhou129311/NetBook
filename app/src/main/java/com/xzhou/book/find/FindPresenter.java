package com.xzhou.book.find;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class FindPresenter extends BasePresenter<FindContract.View> implements FindContract.Presenter {
    private List<Entities.ItemClick> mList;

    public FindPresenter(FindContract.View view) {
        super(view);
    }

    @Override
    public boolean start() {
        if (mList == null) {
            mList = new ArrayList<>();
            mList.add(new Entities.ItemClick(AppUtils.getString(R.string.find_ranking), R.mipmap.home_find_rank));
            mList.add(new Entities.ItemClick(AppUtils.getString(R.string.find_topic), R.mipmap.home_find_topic));
            mList.add(new Entities.ItemClick(AppUtils.getString(R.string.find_category), R.mipmap.home_find_category));
            mList.add(new Entities.ItemClick(AppUtils.getString(R.string.find_random_read), R.mipmap.home_find_secret_unlocked));
            mView.onInitData(mList);
            return true;
        }
        return false;
    }
}
