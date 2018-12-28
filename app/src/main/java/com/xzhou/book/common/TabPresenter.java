package com.xzhou.book.common;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant.CateType;
import com.xzhou.book.utils.Constant.TabSource;

import java.util.ArrayList;
import java.util.List;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {
    private static final int PAGE_SIZE = 20;

    private final Entities.TabData mTabData;
    private final int mTabId;
    private boolean hasStart = true;
    private int mDataNumber;

    private String mFiltrate = "";
    private final String[] CATE_TYPE = new String[] { CateType.NEW, CateType.HOT, CateType.REPUTATION, CateType.OVER };

    TabPresenter(TabContract.View view, Entities.TabData data, int tabId) {
        super(view);
        mTabData = data;
        mTabId = tabId;
    }

    @Override
    public boolean start() {
        if (hasStart) {
            hasStart = false;
            refresh();
            return true;
        }
        return false;
    }

    @Override
    public void setFiltrate(String filtrate) {
        if (mTabData.title.equals(filtrate)) {
            filtrate = "";
        }
        if (!TextUtils.equals(mFiltrate, filtrate)) {
            mFiltrate = filtrate;
            refresh(); // 设置筛选之后需要重新加载
        }
    }

    @Override
    public void refresh() {
        if (mView != null) {
            mView.onRefreshStateChange(true);
        }
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<MultiItemEntity> list = null;
                switch (mTabData.source) {
                case TabSource.SOURCE_CATEGORY_SUB:
                    list = getCategorySubData(false);
                    break;
                case TabSource.SOURCE_TOPIC_LIST:
                    list = getTopicListData(false);
                    break;
                case TabSource.SOURCE_TAG:
                    list = getBookByTagData(false);
                    break;
                case TabSource.SOURCE_COMMUNITY:
                    if (mTabId == 0) {
                        list = getDiscussionList(false);
                    } else {
                        list = getBookReviewList(false);
                    }
                    break;
                case TabSource.SOURCE_RANK_SUB:
                    String rankId = mTabData.params[mTabId];
                    Entities.Rankings rankings = ZhuiShuSQApi.get().getRanking(rankId);
                    if (rankings != null) {
                        list = new ArrayList<>();
                        if (rankings.ranking != null && rankings.ranking.books != null) {
                            list.addAll(rankings.ranking.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_AUTHOR:
                    Entities.BooksByTag searchBooks = ZhuiShuSQApi.get().searchBooksByAuthor(mTabData.params[0]);
                    if (searchBooks != null) {
                        list = new ArrayList<>();
                        if (searchBooks.books != null && searchBooks.books.size() > 0) {
                            list.addAll(searchBooks.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_RECOMMEND:
                    Entities.Recommend recommend = ZhuiShuSQApi.get().getRecommendBook(mTabData.params[0]);
                    if (recommend != null) {
                        list = new ArrayList<>();
                        if (recommend.books != null && recommend.books.size() > 0) {
                            list.addAll(recommend.books);
                        }
                    }
                    break;
                }

                setDataList(list, false);
            }
        });
    }

    @Override
    public void loadMore() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<MultiItemEntity> list = null;
                switch (mTabData.source) {
                case TabSource.SOURCE_RANK_SUB:
                case TabSource.SOURCE_AUTHOR:
                case TabSource.SOURCE_RECOMMEND:
                    // do nothing
                    break;
                case TabSource.SOURCE_CATEGORY_SUB:
                    list = getCategorySubData(true);
                    break;
                case TabSource.SOURCE_TOPIC_LIST:
                    list = getTopicListData(true);
                    break;
                case TabSource.SOURCE_TAG:
                    list = getBookByTagData(true);
                    break;
                case TabSource.SOURCE_COMMUNITY:
                    if (mTabId == 0) {
                        list = getDiscussionList(true);
                    } else {
                        list = getBookReviewList(true);
                    }
                    break;
                }

                setDataList(list, true);
            }
        });
    }

    private List<MultiItemEntity> getCategorySubData(boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>(); //没有更多了
        }

        List<MultiItemEntity> list = null;
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        } else {
            mDataNumber = 0;
        }
        String type = CATE_TYPE[mTabId];
        String major = mTabData.params[0];
        String gender = mTabData.params[1];
        Entities.BooksByCats booksByCats = ZhuiShuSQApi.get().getBooksByCats(gender, type, major, mFiltrate, start, limit);
        if (booksByCats != null) {
            list = new ArrayList<>();
            if (booksByCats.books != null && booksByCats.books.size() > 0) {
                list.addAll(booksByCats.books);
                if (isLoadMore) {
                    mDataNumber += list.size();
                } else {
                    mDataNumber = list.size();
                }
            }
        }
        return list;
    }

    private List<MultiItemEntity> getTopicListData(boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>(); //没有更多了
        }

        List<MultiItemEntity> list = null;
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        } else {
            mDataNumber = 0;
        }
        String duration, sort;
        if (mTabId == 0) { //本周最热
            duration = "last-seven-days";
            sort = "collectorCount";
        } else if (mTabId == 1) { //最新发布
            duration = "all";
            sort = "created";
        } else { //最多收藏
            duration = "all";
            sort = "collectorCount";
        }
        Entities.BookLists bookLists = ZhuiShuSQApi.get().getBookLists(duration, sort, start, limit, "", mFiltrate);
        if (bookLists != null) {
            list = new ArrayList<>();
            if (bookLists.bookLists != null && bookLists.bookLists.size() > 0) {
                list.addAll(bookLists.bookLists);
                if (isLoadMore) {
                    mDataNumber += list.size();
                } else {
                    mDataNumber = list.size();
                }
            }
        }
        return list;
    }

    private List<MultiItemEntity> getBookByTagData(boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>(); //没有更多了
        }

        List<MultiItemEntity> list = null;
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        } else {
            mDataNumber = 0;
        }
        Entities.BooksByTag booksByTag = ZhuiShuSQApi.get().getBooksByTag(mTabData.params[0], start, limit);
        if (booksByTag != null) {
            list = new ArrayList<>();
            if (booksByTag.books != null && booksByTag.books.size() > 0) {
                list.addAll(booksByTag.books);
                if (isLoadMore) {
                    mDataNumber += list.size();
                } else {
                    mDataNumber = list.size();
                }
            }
        }
        return list;
    }

    private List<MultiItemEntity> getDiscussionList(boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>(); //没有更多了
        }
        List<MultiItemEntity> list = null;
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        } else {
            mDataNumber = 0;
        }
        String sort = "created"; //最新发布
        String type = "";
        if (TextUtils.equals(AppUtils.getString(R.string.community_sort_default), mFiltrate)) {
            sort = "updated"; //默认
        } else if (TextUtils.equals(AppUtils.getString(R.string.community_sort_maximum), mFiltrate)) {
            sort = "comment-count"; //最多评论
        }
        Entities.DiscussionList discussionList = ZhuiShuSQApi.get().getBookDiscussionList(mTabData.params[0], sort, type, start, limit);
        if (discussionList != null) {
            list = new ArrayList<>();
            if (discussionList.posts != null && discussionList.posts.size() > 0) {
                list.addAll(discussionList.posts);
                if (isLoadMore) {
                    mDataNumber += list.size();
                } else {
                    mDataNumber = list.size();
                }
            }
        }
        return list;
    }

    private List<MultiItemEntity> getBookReviewList(boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>(); //没有更多了
        }
        List<MultiItemEntity> list = null;
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        } else {
            mDataNumber = 0;
        }
        String sort = "created"; //最新发布
        if (TextUtils.equals(AppUtils.getString(R.string.community_sort_default), mFiltrate)) {
            sort = "updated"; //默认
        } else if (TextUtils.equals(AppUtils.getString(R.string.community_sort_maximum), mFiltrate)) {
            sort = "comment-count"; //最多评论
        }
        Entities.HotReview review = ZhuiShuSQApi.get().getBookReviewList(mTabData.params[0], sort, start, limit);
        if (review != null) {
            list = new ArrayList<>();
            if (review.reviews != null && review.reviews.size() > 0) {
                list.addAll(review.reviews);
                if (isLoadMore) {
                    mDataNumber += list.size();
                } else {
                    mDataNumber = list.size();
                }
            }
        }
        return list;
    }

    private void setDataList(final List<MultiItemEntity> list, final boolean isLoadMore) {
        int delayMills = list == null ? 100 : 0;
        MyApp.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    if (isLoadMore) {
                        mView.onLoadMore(list);
                    } else {
                        mView.onRefreshStateChange(false);
                        mView.onDataChange(list);
                    }
                }
            }
        }, delayMills);
    }

}
