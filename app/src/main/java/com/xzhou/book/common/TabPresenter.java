package com.xzhou.book.common;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {
    private final Entities.TabData mTabData;
    private final int mPosition;
    private boolean hasStart = true;

    public TabPresenter(TabContract.View view, Entities.TabData data, int position) {
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
    public void refresh() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<MultiItemEntity> list = null;
                switch (mTabData.source) {
                case Constant.TabSource.SOURCE_CATEGORY_SUB:
                    list = new ArrayList<>();
                    break;
                case Constant.TabSource.SOURCE_RANK_SUB:
                    String rankId = mTabData.weekRankId;
                    if (mPosition == 1) {
                        rankId = mTabData.monthRankId;
                    } else if (mPosition == 2) {
                        rankId = mTabData.totalRankId;
                    }
                    Entities.Rankings rankings = ZhuiShuSQApi.get().getRanking(rankId);
                    if (rankings != null) {
                        list = new ArrayList<>();
                        if (rankings.ranking != null && rankings.ranking.books != null) {
                            list.addAll(rankings.ranking.books);
                        }
                    }
                    break;
                case Constant.TabSource.SOURCE_TOPIC:
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
                case Constant.TabSource.SOURCE_CATEGORY_SUB:
                    break;
                case Constant.TabSource.SOURCE_RANK_SUB:
                    break;
                case Constant.TabSource.SOURCE_TOPIC:
                    break;
                }

                setDataList(list, true);
            }
        });
    }

    private void setDataList(final List<MultiItemEntity> list, final boolean isLoadMore) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
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
