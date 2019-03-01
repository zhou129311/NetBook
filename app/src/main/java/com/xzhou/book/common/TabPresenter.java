package com.xzhou.book.common;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant.CateType;
import com.xzhou.book.utils.Constant.TabSource;
import com.xzhou.book.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {
    private String TAG = "TabPresenter";
    private static final int PAGE_SIZE = 20;

    private final Entities.TabData mTabData;
    private final int mTabId;
    private boolean hasStart = true;
    private int mDataNumber;

    private String mFiltrate = "";
    private final String[] CATE_TYPE = new String[] { CateType.NEW, CateType.HOT, CateType.REPUTATION, CateType.OVER };

    public TabPresenter(TabContract.View view, Entities.TabData data, int tabId) {
        super(view);
        mTabData = data;
        mTabId = tabId;
        TAG += "_" + tabId;
        if (mTabData.curFiltrate > -1) {
            mFiltrate = mTabData.filtrate[mTabData.curFiltrate];
        }
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
    public void setNeedRefresh(boolean isNeedRefresh) {
        this.hasStart = isNeedRefresh;
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
                mDataNumber = 0;
                switch (mTabData.source) {
                case TabSource.SOURCE_CATEGORY_SUB:
                    list = getCategorySubData();
                    break;
                case TabSource.SOURCE_TOPIC_LIST:
                    list = getTopicListData();
                    break;
                case TabSource.SOURCE_TAG:
                    list = getBookByTagData();
                    break;
                case TabSource.SOURCE_COMMUNITY:
                    if (mTabId == 0) {
                        list = getDiscussionList();
                    } else {
                        list = getBookReviewList();
                    }
                    break;
                case TabSource.SOURCE_RANK_SUB:
                    String rankId = mTabData.params[mTabId];
                    Entities.Rankings rankings = ZhuiShuSQApi.getRanking(rankId);
                    if (rankings != null) {
                        list = new ArrayList<>();
                        if (rankings.ranking != null && rankings.ranking.books != null) {
                            list.addAll(rankings.ranking.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_AUTHOR:
                    Entities.BooksByTag searchBooks = ZhuiShuSQApi.searchBooksByAuthor(mTabData.params[0]);
                    if (searchBooks != null) {
                        list = new ArrayList<>();
                        if (searchBooks.books != null && searchBooks.books.size() > 0) {
                            list.addAll(searchBooks.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_RECOMMEND:
                    Entities.Recommend recommend = ZhuiShuSQApi.getRecommendBook(mTabData.params[0]);
                    if (recommend != null) {
                        list = new ArrayList<>();
                        if (recommend.books != null && recommend.books.size() > 0) {
                            list.addAll(recommend.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_SEARCH:
                    // 书籍 漫画 书单 社区
                    list = getSearchResult();
                    break;
                }
                if (list != null && list.size() > 0) {
                    mDataNumber = list.size();
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
                if (mDataNumber % PAGE_SIZE != 0) {
                    list = new ArrayList<>(); //没有更多了
                } else {
                    switch (mTabData.source) {
                    case TabSource.SOURCE_RANK_SUB:
                    case TabSource.SOURCE_AUTHOR:
                    case TabSource.SOURCE_RECOMMEND:
                        // do nothing
                        break;
                    case TabSource.SOURCE_CATEGORY_SUB:
                        list = getCategorySubData();
                        break;
                    case TabSource.SOURCE_TOPIC_LIST:
                        list = getTopicListData();
                        break;
                    case TabSource.SOURCE_TAG:
                        list = getBookByTagData();
                        break;
                    case TabSource.SOURCE_COMMUNITY:
                        if (mTabId == 0) {
                            list = getDiscussionList();
                        } else {
                            list = getBookReviewList();
                        }
                        break;
                    case TabSource.SOURCE_SEARCH:
                        list = getSearchResult();
                        break;
                    }
                }
                if (list != null && list.size() > 0) {
                    mDataNumber += list.size();
                }
                setDataList(list, true);
            }
        });
    }

    private List<MultiItemEntity> getCategorySubData() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        String type = CATE_TYPE[mTabId];
        String major = mTabData.params[0];
        String gender = mTabData.params[1];
        Entities.BooksByCats booksByCats = ZhuiShuSQApi.getBooksByCats(gender, type, major, mFiltrate, start, limit);
        if (booksByCats != null) {
            list = new ArrayList<>();
            if (booksByCats.books != null && booksByCats.books.size() > 0) {
                list.addAll(booksByCats.books);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getTopicListData() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
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
        Entities.BookLists bookLists = ZhuiShuSQApi.getBookLists(duration, sort, start, limit, "", mFiltrate);
        if (bookLists != null) {
            list = new ArrayList<>();
            if (bookLists.bookLists != null && bookLists.bookLists.size() > 0) {
                list.addAll(bookLists.bookLists);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getBookByTagData() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        Entities.BooksByTag booksByTag = ZhuiShuSQApi.getBooksByTag(mTabData.params[0], start, limit);
        if (booksByTag != null) {
            list = new ArrayList<>();
            if (booksByTag.books != null && booksByTag.books.size() > 0) {
                list.addAll(booksByTag.books);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getDiscussionList() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        String sort = "created"; //最新发布
        String type = "";
        if (TextUtils.equals(AppUtils.getString(R.string.sort_default), mFiltrate)) {
            sort = "updated"; //默认
        } else if (TextUtils.equals(AppUtils.getString(R.string.sort_comment_count), mFiltrate)) {
            sort = "comment-count"; //最多评论
        }
        Entities.DiscussionList discussionList = ZhuiShuSQApi.getBookDiscussionList(mTabData.params[0], sort, type, start, limit);
        if (discussionList != null) {
            list = new ArrayList<>();
            if (discussionList.posts != null && discussionList.posts.size() > 0) {
                list.addAll(discussionList.posts);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getBookReviewList() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        String sort = "created"; //最新发布
        if (TextUtils.equals(AppUtils.getString(R.string.sort_default), mFiltrate)) {
            sort = "updated"; //默认
        } else if (TextUtils.equals(AppUtils.getString(R.string.sort_comment_count), mFiltrate)) {
            sort = "comment-count"; //最多评论
        }
        Entities.HotReview review = ZhuiShuSQApi.getBookReviewList(mTabData.params[0], sort, start, limit);
        if (review != null) {
            list = new ArrayList<>();
            if (review.reviews != null && review.reviews.size() > 0) {
                list.addAll(review.reviews);
            }
        }
        return list;
    }

    private List<MultiItemEntity> getSearchResult() {
        List<MultiItemEntity> list = null;
        int start = mDataNumber;
        int limit = start + PAGE_SIZE;
        String key = mTabData.params[0];
        Log.i(TAG, "getSearchResult::key = " + key);
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        switch (mTabId) {
        case 0://书籍
            Entities.SearchResult result = ZhuiShuSQApi.getSearchResult(key, start, limit);
            if (result != null) {
                list = new ArrayList<>();
                if (result.books != null && result.books.size() > 0) {
                    list.addAll(result.books);
                }
            }
            break;
        case 1://漫画
            Entities.SearchResult resultPicture = ZhuiShuSQApi.getPicSearchResult(key, start, limit);
            if (resultPicture != null) {
                list = new ArrayList<>();
                if (resultPicture.books != null && resultPicture.books.size() > 0) {
                    list.addAll(resultPicture.books);
                }
            }
            break;
        case 2://书单
            Entities.SearchBookList searchBookList = ZhuiShuSQApi.getBookListSearchResult(key, start, limit);
            if (searchBookList != null) {
                list = new ArrayList<>();
                if (searchBookList.ugcbooklists != null && searchBookList.ugcbooklists.size() > 0) {
                    list.addAll(searchBookList.ugcbooklists);
                }
            }
            break;
        case 3://社区
            Entities.DiscussionList discussionList = ZhuiShuSQApi.getPostSearchResult(key, start, limit);
            if (discussionList != null) {
                list = new ArrayList<>();
                if (discussionList.posts != null && discussionList.posts.size() > 0) {
                    list.addAll(discussionList.posts);
                }
            }
            break;
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
