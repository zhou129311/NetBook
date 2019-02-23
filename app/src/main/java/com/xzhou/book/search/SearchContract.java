package com.xzhou.book.search;

import com.xzhou.book.common.BaseContract;
import com.xzhou.book.models.Entities;

import java.util.List;

public interface SearchContract {
    interface Presenter extends BaseContract.Presenter {
        void autoComplete(String key);
    }

    interface View extends BaseContract.View<Presenter> {
        void onAutoComplete(List<Entities.Suggest> list);
    }
}
