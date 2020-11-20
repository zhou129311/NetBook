package com.xzhou.book.find;

import com.xzhou.book.R;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class FindPresenter extends BasePresenter<FindContract.View> implements FindContract.Presenter {
    private List<Entities.ImageText> mList;

    public FindPresenter(FindContract.View view) {
        super(view);
    }

    @Override
    public boolean start() {
        if (mList == null) {
            mList = new ArrayList<>();
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.find_ranking), R.mipmap.home_find_rank));
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.find_topic), R.mipmap.home_find_topic));
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.find_category), R.mipmap.home_find_category));
            mList.add(new Entities.ImageText("起点中文网", R.mipmap.icon_qidian_icon));
            mList.add(new Entities.ImageText("言情小说吧", R.mipmap.icon_yqxsb));
            mList.add(new Entities.ImageText("红袖添香", R.mipmap.icon_hxtx));
            mList.add(new Entities.ImageText("潇湘书院", R.mipmap.icon_xxsy));
            mList.add(new Entities.ImageText("小说阅读网", R.mipmap.icon_xsydw));
            mView.onInitData(mList);
            return true;
        }
        return false;
    }
}
