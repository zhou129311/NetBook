package com.xzhou.book.community;

import com.xzhou.book.R;
import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class CommunityPresenter extends BasePresenter<CommunityContract.View> implements CommunityContract.Presenter {

    private List<Entities.ImageText> mList;

    public CommunityPresenter(CommunityContract.View view) {
        super(view);
    }

    @Override
    public boolean start() {
        if (mList == null) {
            mList = new ArrayList<>();
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.community_discuss), R.mipmap.discuss_section));
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.community_comment), R.mipmap.comment_section));
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.community_helper), R.mipmap.helper_section));
            mList.add(new Entities.ImageText(AppUtils.getString(R.string.community_girl), R.mipmap.girl_section));
            mView.onInitData(mList);
            return true;
        }
        return false;
    }
}
