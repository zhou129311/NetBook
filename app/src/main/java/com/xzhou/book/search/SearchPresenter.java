package com.xzhou.book.search;

import android.text.TextUtils;

import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchPresenter extends BasePresenter<SearchContract.View> implements SearchContract.Presenter {

    private ExecutorService mSearchPool = Executors.newSingleThreadExecutor();

    SearchPresenter(SearchContract.View view) {
        super(view);
    }

    @Override
    public void autoComplete(final String key) {
        mSearchPool.execute(new Runnable() {
            @Override
            public void run() {
                List<Entities.Suggest> list = null;
                Entities.AutoSuggest autoSuggest = ZhuiShuSQApi.getAutoSuggest(key);
//                Entities.AutoComplete autoComplete = ZhuiShuSQApi.getAutoComplete(key);
                if (autoSuggest != null && autoSuggest.keywords != null) {
                    list = autoSuggest.keywords;
                    for (Entities.Suggest suggest : list) {
                        if (suggest.isCat()) {
                            Entities.CategoryListLv2 categoryListLv2 = ZhuiShuSQApi.getCategoryListLv2();
                            if (categoryListLv2 == null) {
                                list.remove(suggest);
                                break;
                            }
                            if ("male".equals(suggest.gender)) {
                                suggest.minors = getMinors(suggest.major, categoryListLv2.male);
                            } else {
                                suggest.minors = getMinors(suggest.major, categoryListLv2.female);
                            }
                            break;
                        }
                    }
                }
                updateAutoComplete(list);
            }
        });
    }

    private List<String> getMinors(String major, List<Entities.CategoryListLv2.Category> categories) {
        for (Entities.CategoryListLv2.Category category : categories) {
            if (TextUtils.equals(major, category.major)) {
                return category.mins;
            }
        }
        return null;
    }

    private void updateAutoComplete(final List<Entities.Suggest> list) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onAutoComplete(list);
                }
            }
        });
    }
}
