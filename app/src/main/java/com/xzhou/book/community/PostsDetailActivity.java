package com.xzhou.book.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.widget.CommonLoadMoreView;
import com.xzhou.book.widget.RatingBar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 帖子详情，包括 书评、讨论、综合讨论、女生话题等等
 */
public class PostsDetailActivity extends BaseActivity<PostsDetailContract.Presenter> implements PostsDetailContract.View {
    public static final int TYPE_DISCUSS = 1; //综合讨论区帖子详情
    public static final int TYPE_HELP = 2; //书荒互助区详情
    public static final int TYPE_REVIEW = 3; //书评详情

    public static final String EXTRA_TYPE = "post_type";
    public static final String EXTRA_ID = "post_id";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;
    @BindView(R.id.comment_layout)
    RelativeLayout mCommentSendLayout;
    @BindView(R.id.comment_edit_view)
    EditText mEditView;
    @BindView(R.id.comment_send_view)
    ImageView mCommentSendView;

    private Adapter mAdapter;
    private int mType;

    public static void startActivity(Context context, String id, int type) {
        Intent intent = new Intent(context, PostsDetailActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_DISCUSS);
        setContentView(R.layout.activity_posts_detail);
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new LineItemDecoration(false, 0, 0));

        mAdapter.setEnableLoadMore(true);
        mAdapter.disableLoadMoreIfNotFullPage();
        mAdapter.setLoadMoreView(new CommonLoadMoreView());
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.loadMore();
            }
        }, mRecyclerView);

        mCommentSendView.setEnabled(false);
        mEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(mEditView.getText().toString())) {
                    mCommentSendView.setEnabled(false);
                } else {
                    mCommentSendView.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected PostsDetailContract.Presenter createPresenter() {
        return new PostsDetailPresenter(this, getIntent().getStringExtra(EXTRA_ID), mType);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        String title = getString(R.string.discussion_detail_title);
        switch (mType) {
        case TYPE_HELP:
            title = getString(R.string.help_detail_title);
            break;
        case TYPE_REVIEW:
            title = getString(R.string.review_detail_title);
            break;
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
    public void onLoading(boolean isLoading) {
        mLoadView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onInitReviewDetail(Entities.ReviewDetail reviewDetail) {
        if (reviewDetail == null) {
            mLoadErrorView.setVisibility(View.VISIBLE);
            mCommentSendLayout.setVisibility(View.GONE);
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.header_view_posts_detail, null);
        HeaderViewHolder header = new HeaderViewHolder(view);
        header.initReviewData(reviewDetail);
        mAdapter.addHeaderView(view);
    }

    @Override
    public void onInitDiscussionDetail(Entities.DiscussionDetail detail) {
        if (detail == null) {
            mLoadErrorView.setVisibility(View.VISIBLE);
            mCommentSendLayout.setVisibility(View.GONE);
            return;
        }

    }

    @Override
    public void onInitData(List<MultiItemEntity> list) {
        if (list == null || list.size() <= 0) {
            mLoadErrorView.setVisibility(View.VISIBLE);
            mCommentSendLayout.setVisibility(View.GONE);
            mAdapter.setNewData(null);
        } else {
            mCommentSendLayout.setVisibility(View.VISIBLE);
            mAdapter.addData(list);
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
    public void setPresenter(PostsDetailContract.Presenter presenter) {
    }

    @OnClick({R.id.load_error_view, R.id.comment_send_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.load_error_view:
            mPresenter.start();
            break;
        case R.id.comment_send_view:
            break;
        }
    }

    static class HeaderViewHolder {
        @BindView(R.id.posts_detail_avatar_view)
        ImageView mPostDetailAvatarView;
        @BindView(R.id.posts_detail_author_view)
        TextView mPostDetailAuthorView;
        @BindView(R.id.posts_detail_create_time)
        TextView mPostDetailCreateTime;
        @BindView(R.id.posts_detail_title_view)
        TextView mPostDetailTitleView;
        @BindView(R.id.posts_detail_desc)
        TextView mPostDetailContent;
        @BindView(R.id.review_detail_rating_layout)
        ConstraintLayout mPostDetailRatingLayout;
        @BindView(R.id.book_image)
        ImageView mBookImage;
        @BindView(R.id.book_title)
        TextView mBookTitle;
        @BindView(R.id.review_detail_rating_bar)
        RatingBar mRatingBar;
        @BindView(R.id.posts_agreed_view)
        TextView mPostAgreedView;
        @BindView(R.id.posts_more_view)
        ImageView mPostMoreView;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.posts_agreed_view, R.id.posts_more_view})
        public void onViewClicked(View view) {
            switch (view.getId()) {
            case R.id.posts_agreed_view:
                break;
            case R.id.posts_more_view:
                break;
            }
        }

        private void initReviewData(final Entities.ReviewDetail detail) {
            mPostDetailRatingLayout.setVisibility(View.VISIBLE);
            mPostDetailRatingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BookDetailActivity.startActivity(mPostDetailRatingLayout.getContext(), detail.review.bookId());
                }
            });
            ImageLoader.showCircleImageUrl(mPostDetailAvatarView.getContext(), mPostDetailAvatarView,
                    detail.review.avatar(), R.mipmap.avatar_default);
            ImageLoader.showRoundImageUrl(mBookImage.getContext(), mBookImage, detail.review.cover(), R.mipmap.ic_cover_default);
            mPostDetailAuthorView.setText(AppUtils.getString(R.string.book_detail_review_author, detail.review.nickname(), detail.review.lv()));
            mPostDetailCreateTime.setText(AppUtils.getDescriptionTimeFromDateString(detail.review.created));
            mPostDetailTitleView.setText(detail.review.title);
            mPostDetailContent.setText(detail.review.content);
            mBookTitle.setText(detail.review.bookTitle());
            mRatingBar.setStarCount(detail.review.rating);
            mPostAgreedView.setVisibility(View.GONE);
        }

        private void initDiscussionData() {
        }

        private void initHelpData() {
        }
    }

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        Adapter() {
            super(null);
            addItemType(Constant.ITEM_TYPE_TEXT, R.layout.item_view_post_section);
            addItemType(Constant.ITEM_TYPE_HELPFUL, R.layout.item_view_review_scoring);
            addItemType(Constant.ITEM_TYPE_COMMENT, R.layout.item_view_comment);
        }

        @Override
        protected void convert(CommonViewHolder holder, MultiItemEntity item) {
            switch (holder.getItemViewType()) {
            case Constant.ITEM_TYPE_TEXT:
                Entities.PostSection section = (Entities.PostSection) item;
                holder.setText(R.id.section_text_view, section.text);
                break;
            case Constant.ITEM_TYPE_HELPFUL:
                Entities.Helpful helpful = (Entities.Helpful) item;
                holder.setText(R.id.yes_count_text, String.valueOf(helpful.yes))
                        .setText(R.id.no_count_text, String.valueOf(helpful.no));
                break;
            case Constant.ITEM_TYPE_COMMENT:
                Entities.Comment comment = (Entities.Comment) item;
                holder.setCircleImageUrl(R.id.comment_image, comment.avatar(), R.mipmap.avatar_default)
                        .setText(R.id.comment_floor, AppUtils.getString(R.string.comment_floor, comment.floor))
                        .setText(R.id.comment_title, AppUtils.getString(R.string.book_detail_review_author, comment.nickname(), comment.lv()))
                        .setText(R.id.comment_content, comment.content);
                TextView likeView = holder.getView(R.id.comment_like_count);
                TextView timeView = holder.getView(R.id.comment_time);
                if (comment.isBest) {
                    likeView.setText(AppUtils.getString(R.string.comment_like_count, comment.likeCount));
                    likeView.setVisibility(View.VISIBLE);
                    timeView.setVisibility(View.GONE);
                } else {
                    timeView.setText(AppUtils.getDescriptionTimeFromDateString(comment.created));
                    likeView.setVisibility(View.GONE);
                    timeView.setVisibility(View.VISIBLE);
                }
                break;
            }
        }

    }
}
