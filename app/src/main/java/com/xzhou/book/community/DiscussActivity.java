package com.xzhou.book.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.widget.CommonLoadMoreView;
import com.xzhou.book.widget.MultiSortLayout;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

public class DiscussActivity extends BaseActivity<DiscussContract.Presenter> implements DiscussContract.View {
    private static final String EXTRA_TYPE = "type";

    public static final int TYPE_DISCUSS = 1;
    public static final int TYPE_REVIEWS = 2;
    public static final int TYPE_HELP = 3;
    public static final int TYPE_GIRL = 4;

    @BindView(R.id.multi_sort_view)
    MultiSortLayout mMultiSortView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    private int mType;
    private Adapter mAdapter;
    private View mEmptyView;
    private View mLoadErrorView;

    public static void startActivity(Context context, int type) {
        Intent intent = new Intent(context, DiscussActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        //init MultiSortLayout data
        mMultiSortView.setToolbar(mToolbar);
        mMultiSortView.setDataType(mType);
        mMultiSortView.setOnSelectListener(new MultiSortLayout.OnSelectListener() {
            @Override
            public void onSortSelect(HashMap<String, String> params) {
                mPresenter.setParams(params);
            }
        });
        //init error view
        LayoutInflater inflater = LayoutInflater.from(this);
        mEmptyView = inflater.inflate(R.layout.common_empty_view, null);
        mLoadErrorView = inflater.inflate(R.layout.common_load_error_view, null);
        mLoadErrorView.setVisibility(View.VISIBLE);
        mLoadErrorView.setOnClickListener(mRefreshClickListener);
        mEmptyView.setOnClickListener(mRefreshClickListener);
        //init mAdapter and mRecyclerView
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new LineItemDecoration(false, 70, 0));
        mAdapter.setLoadMoreView(new CommonLoadMoreView());
        mAdapter.setEnableLoadMore(true);
        mAdapter.disableLoadMoreIfNotFullPage();
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.loadMore();
            }
        }, mRecyclerView);
        //init mSwipeLayout
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeLayout.setEnabled(true);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_DISCUSS);
        String title = getString(R.string.community_discuss);
        if (mType == TYPE_REVIEWS) {
            title = getString(R.string.community_comment);
        } else if (mType == TYPE_HELP) {
            title = getString(R.string.community_helper);
        }
        mToolbar.setTitle(title);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            switch (mType) {
            case TYPE_DISCUSS:

                break;
            case TYPE_REVIEWS:

                break;
            case TYPE_HELP:

                break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected DiscussContract.Presenter createPresenter() {
        return new DiscussPresenter(this, mType, mMultiSortView.getDefaultParams());
    }

    @Override
    public void onRefreshStateChange(boolean isRefreshing) {
        mSwipeLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void onDataChange(List<MultiItemEntity> list) {
        if (list == null) {
            mAdapter.setEmptyView(mLoadErrorView);
            mAdapter.setNewData(null);
        } else if (list.size() <= 0) {
            mAdapter.setEmptyView(mEmptyView);
            mAdapter.setNewData(null);
        } else {
            mAdapter.replaceData(list);
        }
    }

    @Override
    public void onLoadMore(List<MultiItemEntity> list) {
        if (list == null) {
            mAdapter.loadMoreFail();
        } else if (list.size() <= 0) {
            mAdapter.loadMoreEnd();
        } else {
            mAdapter.loadMoreComplete();
            mAdapter.addData(list);
        }
    }

    @Override
    public void setPresenter(DiscussContract.Presenter presenter) {
    }

    private View.OnClickListener mRefreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPresenter.refresh();
        }
    };

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        Adapter() {
            super(null);
            addItemType(Constant.ITEM_TYPE_DISCUSSION, R.layout.item_view_posts_discussion);
            addItemType(Constant.ITEM_TYPE_POSTS_REVIEW, R.layout.item_view_posts_review);
            addItemType(Constant.ITEM_TYPE_POSTS_HELP, R.layout.item_view_posts_help);
        }

        @Override
        protected void convert(CommonViewHolder holder, MultiItemEntity item) {
            int resId = 0;
            switch (holder.getItemViewType()) {
            case Constant.ITEM_TYPE_DISCUSSION:
                final Entities.Posts posts = (Entities.Posts) item;
                holder.setCircleImageUrl(R.id.discussion_img, posts.avatar(), R.mipmap.avatar_default)
                        .setText(R.id.discussion_author, AppUtils.getString(R.string.book_detail_review_author,
                                posts.nickname(), posts.authorLv()))
                        .setText(R.id.discussion_time, AppUtils.getDescriptionTimeFromDateString(posts.created))
                        .setText(R.id.discussion_title, posts.title)
                        .setText(R.id.discussion_comment_count, String.valueOf(posts.commentCount))
                        .setText(R.id.discussion_vote_count, String.valueOf(posts.voteCount))
                        .setText(R.id.discussion_comment_like_count, String.valueOf(posts.likeCount))
                        .setGone(R.id.discussion_comment_count, !posts.isVote())
                        .setGone(R.id.discussion_vote_count, posts.isVote())
                        .setGone(R.id.discussion_time, !posts.isHot() && !posts.isDistillate())
                        .setGone(R.id.view_hot, posts.isHot())
                        .setGone(R.id.view_distillate, posts.isDistillate())
                        .setGone(R.id.author_type_view, posts.isOfficial() || posts.isDoyen());

                if (posts.isOfficial()) {
                    resId = R.mipmap.user_avatar_verify_official;
                } else if (posts.isDoyen()) {
                    resId = R.mipmap.user_avatar_verify_doyen;
                }
                if (resId != 0) {
                    holder.setImageResource(R.id.author_type_view, resId);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostsDetailActivity.startActivity(mContext, posts._id, PostsDetailActivity.TYPE_DISCUSS);
                    }
                });
                break;
            case Constant.ITEM_TYPE_POSTS_REVIEW:
                final Entities.PostsReviews postsReviews = (Entities.PostsReviews) item;
                holder.setRoundImageUrl(R.id.posts_review_img, postsReviews.cover(), R.mipmap.ic_cover_default)
                        .setText(R.id.posts_review_name_type, AppUtils.getString(R.string.post_review_name_type,
                                postsReviews.bookTitle(), Constant.typeToText.get(postsReviews.bookType())))
                        .setText(R.id.posts_review_title, postsReviews.title)
                        .setText(R.id.posts_review_time, AppUtils.getDescriptionTimeFromDateString(postsReviews.created))
                        .setText(R.id.posts_review_useful_yes, String.valueOf(postsReviews.helpfulYes()))
                        .setGone(R.id.posts_review_time, !postsReviews.isHot() && !postsReviews.isDistillate())
                        .setGone(R.id.view_hot, postsReviews.isHot())
                        .setGone(R.id.view_distillate, postsReviews.isDistillate());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostsDetailActivity.startActivity(mContext, postsReviews._id, PostsDetailActivity.TYPE_REVIEW);
                    }
                });
                break;
            case Constant.ITEM_TYPE_POSTS_HELP:
                final Entities.BookHelpList.HelpsBean helpsBean = (Entities.BookHelpList.HelpsBean) item;
                holder.setCircleImageUrl(R.id.book_help_img, helpsBean.avatar(), R.mipmap.avatar_default)
                        .setText(R.id.book_help_author, AppUtils.getString(R.string.book_detail_review_author,
                                helpsBean.nickname(), helpsBean.lv()))
                        .setText(R.id.book_help_time, AppUtils.getDescriptionTimeFromDateString(helpsBean.created))
                        .setText(R.id.book_help_title, helpsBean.title)
                        .setText(R.id.book_help_comment_count, String.valueOf(helpsBean.commentCount))
                        .setGone(R.id.book_help_time, !helpsBean.isHot() && !helpsBean.isDistillate())
                        .setGone(R.id.view_hot, helpsBean.isHot())
                        .setGone(R.id.view_distillate, helpsBean.isDistillate())
                        .setGone(R.id.author_type_view, helpsBean.isOfficial() || helpsBean.isDoyen());
                if (helpsBean.isOfficial()) {
                    resId = R.mipmap.user_avatar_verify_official;
                } else if (helpsBean.isDoyen()) {
                    resId = R.mipmap.user_avatar_verify_doyen;
                }
                if (resId != 0) {
                    holder.setImageResource(R.id.author_type_view, resId);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostsDetailActivity.startActivity(mContext, helpsBean._id, PostsDetailActivity.TYPE_HELP);
                    }
                });
                break;
            }
        }
    }
}
