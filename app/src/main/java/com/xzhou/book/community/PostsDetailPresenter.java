package com.xzhou.book.community;

import android.support.annotation.WorkerThread;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class PostsDetailPresenter extends BasePresenter<PostsDetailContract.View> implements PostsDetailContract.Presenter {
    private static final int PAGE_SIZE = 30;

    private int mType;
    private String mPostId;
    private boolean hasStart;
    private int mDataNumber;
    private final int[] mVoteNumberRes = new int[]{
            R.mipmap.post_detail_comment_vote_item_1,
            R.mipmap.post_detail_comment_vote_item_2,
            R.mipmap.post_detail_comment_vote_item_3,
            R.mipmap.post_detail_comment_vote_item_4,
            R.mipmap.post_detail_comment_vote_item_5,
            R.mipmap.post_detail_comment_vote_item_6,
            R.mipmap.post_detail_comment_vote_item_7,
    };

    PostsDetailPresenter(PostsDetailContract.View view, String postId, int type) {
        super(view);
        mType = type;
        mPostId = postId;
    }

    @Override
    public boolean start() {
        if (!hasStart) {
            hasStart = true;
            if (mView != null) {
                mView.onLoading(true);
            }
            ZhuiShuSQApi.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    mDataNumber = 0;
                    ArrayList<MultiItemEntity> list = null;
                    switch (mType) {
                    case PostsDetailActivity.TYPE_DISCUSS:
                        Entities.DiscussionDetail discussionDetail = ZhuiShuSQApi.getBookDiscussionDetail(mPostId);
                        if (discussionDetail != null && discussionDetail.post != null) {
                            setDiscussionDetail(discussionDetail);
                            list = new ArrayList<>();
                            if (discussionDetail.post.votes != null && discussionDetail.post.votes.size() > 0) {
                                list.add(new Entities.PostSection(AppUtils.getString(R.string.vote_count, discussionDetail.post.voteCount)));
                                int i = 0;
                                for (Entities.DiscussionDetail.PostDetail.Vote vote : discussionDetail.post.votes) {
                                    if (i > 6) {
                                        Log.e("Vote", "Entities.DiscussionDetail.PostDetail.Vote size > 7");
                                        break;
                                    }
                                    vote.itemNumberRes = mVoteNumberRes[i];
                                    i++;
                                }
                            }

                        }
                        break;
                    case PostsDetailActivity.TYPE_HELP:

                        break;
                    case PostsDetailActivity.TYPE_REVIEW:
                        Entities.ReviewDetail reviewDetail = ZhuiShuSQApi.getBookReviewDetail(mPostId);
                        if (reviewDetail != null && reviewDetail.review != null && reviewDetail.review.helpful != null) {
                            setReviewDetail(reviewDetail);
                            list = new ArrayList<>();
                            //给书评打分
                            list.add(new Entities.PostSection(AppUtils.getString(R.string.book_review_the_scoring)));
                            list.add(reviewDetail.review.helpful);

                            addBestCommentList(list);

                            list.add(new Entities.PostSection(AppUtils.getString(R.string.comment_comment_count, reviewDetail.review.commentCount)));
                            addReviewCommentList(list, false);
                        }
                        break;
                    }
                    setCommentData(list, false);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void loadMore() {
        ZhuiShuSQApi.getPool().execute(new Runnable() {
            @Override
            public void run() {
                List<MultiItemEntity> list = null;
                switch (mType) {
                case PostsDetailActivity.TYPE_DISCUSS:

                    break;
                case PostsDetailActivity.TYPE_HELP:
                case PostsDetailActivity.TYPE_REVIEW:
                    list = addReviewCommentList(null, true);
                    break;
                }

                setCommentData(list, true);
            }
        });
    }

    private List<MultiItemEntity> addReviewCommentList(List<MultiItemEntity> list, boolean isLoadMore) {
        if (isLoadMore && mDataNumber % PAGE_SIZE != 0) {
            return new ArrayList<>();
        }
        int start = 0;
        int limit = PAGE_SIZE;
        if (isLoadMore) {
            start = mDataNumber;
            limit = mDataNumber + PAGE_SIZE;
        }
        Entities.CommentList commentList = ZhuiShuSQApi.getBookReviewComments(mPostId, start, limit);
        if (commentList != null && commentList.comments != null && commentList.comments.size() > 0) {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.addAll(commentList.comments);
            if (isLoadMore) {
                mDataNumber += list.size();
            } else {
                mDataNumber = list.size();
            }
        }
        return list;
    }

    @WorkerThread
    private void addBestCommentList(List<MultiItemEntity> list) {
        Entities.CommentList bestCommentList = ZhuiShuSQApi.getBestComments(mPostId);
        if (bestCommentList != null && bestCommentList.comments != null && bestCommentList.comments.size() > 0) {
            //仰望神评论
            list.add(new Entities.PostSection(AppUtils.getString(R.string.comment_best_comment)));
            for (Entities.Comment comment : bestCommentList.comments) {
                comment.isBest = true;
            }
            list.addAll(bestCommentList.comments);
        }
    }

    private void setDiscussionDetail(final Entities.DiscussionDetail detail) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                hasStart = detail != null;
                if (mView != null) {
                    mView.onInitDiscussionDetail(detail);
                }
            }
        });
    }

    private void setReviewDetail(final Entities.ReviewDetail detail) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                hasStart = detail != null;
                if (mView != null) {
                    mView.onInitReviewDetail(detail);
                }
            }
        });
    }

    private void setCommentData(final List<MultiItemEntity> list, final boolean isLoadMore) {
        MyApp.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoading(false);
                }
                if (isLoadMore) {
                    if (mView != null) {
                        mView.onLoadMore(list);
                    }
                } else {
                    hasStart = list != null;
                    if (mView != null) {
                        mView.onInitData(list);
                    }
                }
            }
        });
    }
}
