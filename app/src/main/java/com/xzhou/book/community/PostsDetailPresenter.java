package com.xzhou.book.community;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.common.BasePresenter;
import com.xzhou.book.net.ZhuiShuSQApi;
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
    private final int[] mVoteNumberRes = new int[] {
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
                    case PostsDetailActivity.TYPE_GIRL:
                    case PostsDetailActivity.TYPE_DISCUSS:
                        Entities.DiscussionDetail discussionDetail = ZhuiShuSQApi.getBookDiscussionDetail(mPostId);
                        if (discussionDetail != null && discussionDetail.post != null) {
                            setPostDetail(discussionDetail);
                            list = new ArrayList<>();
                            if (discussionDetail.post.votes != null && discussionDetail.post.votes.size() > 0) {
                                list.add(new Entities.PostSection(AppUtils.getString(R.string.vote_count, discussionDetail.post.voteCount)));
                                int i = 0;
                                for (Entities.DiscussionDetail.PostDetail.Vote vote : discussionDetail.post.votes) {
                                    if (i > 6) {
                                        Log.e("Vote", "Entities.DiscussionDetail.PostDetail.Vote size > 7");
                                        break;
                                    }
                                    list.add(vote);
                                    vote.itemNumberRes = mVoteNumberRes[i];
                                    i++;
                                }
                            }
                            addBestCommentList(list);
                            list.add(new Entities.PostSection(AppUtils.getString(R.string.comment_comment_count, discussionDetail.post.commentCount)));
                            addDiscussionCommentList(list, false);
                        }
                        break;
                    case PostsDetailActivity.TYPE_HELP:
                        Entities.BookHelp bookHelp = ZhuiShuSQApi.getBookHelpDetail(mPostId);
                        if (bookHelp != null && bookHelp.help != null) {
                            setPostDetail(bookHelp);
                            list = new ArrayList<>();
                            //神评论
                            addBestCommentList(list);
                            //共XX条评论
                            list.add(new Entities.PostSection(AppUtils.getString(R.string.comment_comment_count, bookHelp.help.commentCount)));
                            addReviewOrHelpCommentList(list, false);
                        }
                        break;
                    case PostsDetailActivity.TYPE_REVIEW:
                        Entities.ReviewDetail reviewDetail = ZhuiShuSQApi.getBookReviewDetail(mPostId);
                        if (reviewDetail != null && reviewDetail.review != null && reviewDetail.review.helpful != null) {
                            setPostDetail(reviewDetail);
                            list = new ArrayList<>();
                            //给书评打分
                            list.add(new Entities.PostSection(AppUtils.getString(R.string.book_review_the_scoring)));
                            list.add(reviewDetail.review.helpful);
                            //神评论
                            addBestCommentList(list);
                            //共XX条评论
                            list.add(new Entities.PostSection(AppUtils.getString(R.string.comment_comment_count, reviewDetail.review.commentCount)));
                            addReviewOrHelpCommentList(list, false);
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
                if (mDataNumber % PAGE_SIZE != 0) {
                    list = new ArrayList<>();
                } else {
                    switch (mType) {
                    case PostsDetailActivity.TYPE_DISCUSS:
                    case PostsDetailActivity.TYPE_GIRL:
                        list = addDiscussionCommentList(null, true);
                        break;
                    case PostsDetailActivity.TYPE_HELP:
                    case PostsDetailActivity.TYPE_REVIEW:
                        list = addReviewOrHelpCommentList(null, true);
                        break;
                    }
                }
                if (list != null && list.size() > 0) {
                    mDataNumber += list.size();
                }
                setCommentData(list, true);
            }
        });
    }

    private List<MultiItemEntity> addReviewOrHelpCommentList(List<MultiItemEntity> list, boolean isLoadMore) {
        int start = mDataNumber;
        int limit = mDataNumber + PAGE_SIZE;
        Entities.CommentList commentList = ZhuiShuSQApi.getBookReviewComments(mPostId, start, limit);
        if (commentList != null && commentList.comments != null) {
            if (list == null) {
                list = new ArrayList<>();
            }
            if (commentList.comments.size() > 0) {
                list.addAll(commentList.comments);
                if (!isLoadMore) {
                    mDataNumber = commentList.comments.size();
                }
            }
        }
        return list;
    }

    private List<MultiItemEntity> addDiscussionCommentList(List<MultiItemEntity> list, boolean isLoadMore) {
        int start = mDataNumber;
        int limit = mDataNumber + PAGE_SIZE;
        Entities.CommentList commentList = ZhuiShuSQApi.getBookDiscussionComments(mPostId, start, limit);
        if (commentList != null && commentList.comments != null) {
            if (list == null) {
                list = new ArrayList<>();
            }
            if (commentList.comments.size() > 0) {
                list.addAll(commentList.comments);
                if (!isLoadMore) {
                    mDataNumber = commentList.comments.size();
                }
            }
        }
        return list;
    }

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

    private void setPostDetail(final Object detail) {
        MyApp.runUI(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    mView.onLoading(false);
                }
                hasStart = detail != null;
                if (mView != null) {
                    mView.onInitPostDetail(detail);
                }
            }
        });
    }

    private void setCommentData(final List<MultiItemEntity> list, final boolean isLoadMore) {
        MyApp.runUI(new Runnable() {
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
