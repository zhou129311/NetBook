package com.xzhou.book.community;

import com.xzhou.book.models.Entities;
import com.xzhou.book.common.BaseContract;

import java.util.List;

public interface CommunityContract {

    interface Presenter extends BaseContract.Presenter {



    }

    interface View extends BaseContract.View<Presenter> {
        void onInitData(List<Entities.ImageText> list);
    }

}
