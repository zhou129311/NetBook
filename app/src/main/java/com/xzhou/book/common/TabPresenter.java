package com.xzhou.book.common;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant.CateType;
import com.xzhou.book.utils.Constant.TabSource;

import java.util.ArrayList;
import java.util.List;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {
    private static final int PAGE_SIZE = 20;

    private final Entities.TabData mTabData;
    private final int mPosition;
    private boolean hasStart = true;
    private int mDataNumber;

    private String mFiltrate = "";
    private String[] mTypes = new String[] { CateType.NEW, CateType.HOT, CateType.REPUTATION, CateType.OVER };

    TabPresenter(TabContract.View view, Entities.TabData data, int position) {
        super(view);
        mTabData = data;
        mPosition = position;
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
                    String type = mTypes[mPosition];
                    String major = mTabData.params[0];
                    String gender = mTabData.params[1];
                    mDataNumber = 0;
                    Entities.BooksByCats booksByCats = ZhuiShuSQApi.get().getBooksByCats(gender, type, major, mFiltrate, mDataNumber, PAGE_SIZE);
                    if (booksByCats != null) {
                        list = new ArrayList<>();
                        if (booksByCats.books != null && booksByCats.books.size() > 0) {
                            list.addAll(booksByCats.books);
                            mDataNumber = list.size();
                        }
                    }
                    break;
                case TabSource.SOURCE_RANK_SUB:
                    String rankId = mTabData.params[mPosition];
                    Entities.Rankings rankings = ZhuiShuSQApi.get().getRanking(rankId);
                    if (rankings != null) {
                        list = new ArrayList<>();
                        if (rankings.ranking != null && rankings.ranking.books != null) {
                            list.addAll(rankings.ranking.books);
                        }
                    }
                    break;
                case TabSource.SOURCE_TOPIC_LIST:


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
                case TabSource.SOURCE_TAG:
                    mDataNumber = 0;
                    Entities.BooksByTag booksByTag = ZhuiShuSQApi.get().getBooksByTag(mTabData.params[0], mDataNumber, PAGE_SIZE);
                    if (booksByTag != null) {
                        list = new ArrayList<>();
                        if (booksByTag.books != null && booksByTag.books.size() > 0) {
                            list.addAll(booksByTag.books);
                            mDataNumber = list.size();
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
                    if (mDataNumber % PAGE_SIZE != 0) {
                        list = new ArrayList<>();
                    } else {
                        String type = mTypes[mPosition];
                        String major = mTabData.params[0];
                        String gender = mTabData.params[1];
                        int limit = mDataNumber + PAGE_SIZE;
                        Entities.BooksByCats booksByCats = ZhuiShuSQApi.get().getBooksByCats(gender, type, major, mFiltrate, mDataNumber, limit);
                        if (booksByCats != null) {
                            list = new ArrayList<>();
                            if (booksByCats.books != null && booksByCats.books.size() > 0) {
                                list.addAll(booksByCats.books);
                                mDataNumber += list.size();
                            }
                        }
                    }
                    break;
                case TabSource.SOURCE_TOPIC_LIST:
                    break;
                case TabSource.SOURCE_TAG:
                    if (mDataNumber % PAGE_SIZE != 0) {
                        list = new ArrayList<>();
                    } else {
                        int limit = mDataNumber + PAGE_SIZE;
                        Entities.BooksByTag booksByTag = ZhuiShuSQApi.get().getBooksByTag(mTabData.params[0], mDataNumber, limit);
                        if (booksByTag != null) {
                            list = new ArrayList<>();
                            if (booksByTag.books != null && booksByTag.books.size() > 0) {
                                list.addAll(booksByTag.books);
                                mDataNumber += list.size();
                            }
                        }
                    }
                    break;
                }

                setDataList(list, true);
            }
        });
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
