package com.xzhou.book.community;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscussPresenter extends BasePresenter<DiscussContract.View> implements DiscussContract.Presenter {
    private static final String TAG = "DiscussPresenter";
    private static final int PAGE_SIZE = 20;

    private int mType;
    private int mDataNumber;
    private HashMap<String, String> mParams;
    private boolean hasStart;

    DiscussPresenter(DiscussContract.View view, int type, HashMap<String, String> params) {
        super(view);
        mType = type;
        mParams = params;
    }

    @Override
    public void setParams(HashMap<String, String> params) {
        if (mParams != null) {
            mParams = params;
            refresh();
        }
    }

    @Override
    public boolean start() {
        if (!hasStart) {
            hasStart = true;
            refresh();
            return true;
        }
        return false;
    }

    @Override
    public void refresh() {
        if (mView != null) {
            mView.onRefreshStateChange(true);
        }
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                mDataNumber = 0;
                List<MultiItemEntity> list = null;
                switch (mType) {
                case DiscussActivity.TYPE_DISCUSS:
                    list = getDiscussionList();
                    break;
                case DiscussActivity.TYPE_REVIEWS:
                    list = getReviewsList();
                    break;
                case DiscussActivity.TYPE_HELP:
                    list = getBookHelpList();
                    break;
                case DiscussActivity.TYPE_GIRL:

                    break;
                }

                if (list != null && list.size() > 0) {
                    mDataNumber = list.size();
                }
                setList(list, false);
            }
        });
    }

    @Override
    public void loadMore() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<MultiItemEntity> list = null;
                if (mDataNumber % PAGE_SIZE != 0) {
                    list = new ArrayList<>();
                } else {
                    switch (mType) {
                    case DiscussActivity.TYPE_DISCUSS:
                        list = getDiscussionList();
                        break;
                    case DiscussActivity.TYPE_REVIEWS:
                        list = getReviewsList();
                        break;
                    case DiscussActivity.TYPE_HELP:
                        list = getBookHelpList();
                        break;
                    case DiscussActivity.TYPE_GIRL:

                        break;
                    }
                }

                if (list != null && list.size() > 0) {
                    mDataNumber += list.size();
                }
                setList(list, true);
            }
        });
    }

    private List<MultiItemEntity> getDiscussionList() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        boolean distillate = Boolean.valueOf(mParams.get(ZhuiShuSQApi.DISTILLATE));
        String sort = mParams.get(ZhuiShuSQApi.SORT);
        Entities.DiscussionList discussionList = ZhuiShuSQApi.getBookDiscussionList("ramble", "all", sort, "all",
                start, limit, distillate);
        if (discussionList != null) {
            list = new ArrayList<>();
            if (discussionList.posts != null && discussionList.posts.size() > 0) {
                list.addAll(discussionList.posts);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getReviewsList() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        boolean distillate = Boolean.valueOf(mParams.get(ZhuiShuSQApi.DISTILLATE));
        String type = mParams.get(ZhuiShuSQApi.TYPE);
        String sort = mParams.get(ZhuiShuSQApi.SORT);
        Entities.PostReviewList postReviewList = ZhuiShuSQApi.getBookReviewList("all", sort, type, start, limit, distillate);
        if (postReviewList != null) {
            list = new ArrayList<>();
            if (postReviewList.reviews != null && postReviewList.reviews.size() > 0) {
                list.addAll(postReviewList.reviews);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getBookHelpList() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        boolean distillate = Boolean.valueOf(mParams.get(ZhuiShuSQApi.DISTILLATE));
        String sort = mParams.get(ZhuiShuSQApi.SORT);
        Entities.BookHelpList bookHelpList = ZhuiShuSQApi.getBookHelpList("all", sort, start, limit, distillate);
        if (bookHelpList != null) {
            list = new ArrayList<>();
            if (bookHelpList.helps != null && bookHelpList.helps.size() > 0) {
                list.addAll(bookHelpList.helps);
            }
        }
        return list;
    }

    private void setList(final List<MultiItemEntity> list, final boolean isLoadMore) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onRefreshStateChange(false);
                    if (isLoadMore) {
                        mView.onLoadMore(list);
                    } else {
                        mView.onDataChange(list);
                    }
                }
            }
        });
    }
}
