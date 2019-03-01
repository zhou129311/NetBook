package com.xzhou.book.community;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
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
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.RichTextUtils;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.CommonLoadMoreView;
import com.xzhou.book.widget.LinkTouchMovementMethod;
import com.xzhou.book.widget.RatingBar;
import com.xzhou.book.widget.SwipeItemLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 帖子详情，包括 书评、讨论、综合讨论、女生话题等等
 */
public class PostsDetailActivity extends BaseActivity<PostsDetailContract.Presenter> implements PostsDetailContract.View {
    private static final String TAG = "PostsDetailActivity";
    public static final int TYPE_DISCUSS = 1; //综合讨论区帖子详情
    public static final int TYPE_HELP = 2; //书荒互助区详情
    public static final int TYPE_REVIEW = 3; //书评详情
    public static final int TYPE_GIRL = 4; //女生区话题详情

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
        mRecyclerView.addItemDecoration(new LineItemDecoration(false, false));
        mRecyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(this));

        mAdapter.setEnableLoadMore(true);
        mAdapter.disableLoadMoreIfNotFullPage();
        mAdapter.setLoadMoreView(new CommonLoadMoreView());
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.loadMore();
            }
        }, mRecyclerView);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                case R.id.yes_ll_view:

                    break;
                case R.id.no_ll_view:

                    break;
                }
            }
        });

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
        case TYPE_GIRL:
            title = getString(R.string.girl_detail_title);
            break;
        }
        mToolbar.setTitle(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onBackPressed() {
        if (mRecyclerView.getChildCount() <= 1) {
            super.onBackPressed();
            return;
        }
        LinearLayoutManager lm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstItem = lm.findFirstVisibleItemPosition();
        if (firstItem == 0) {
            super.onBackPressed();
        } else {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onLoading(boolean isLoading) {
        mLoadView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onInitPostDetail(Object detail) {
        if (detail == null) {
            mLoadErrorView.setVisibility(View.VISIBLE);
            mCommentSendLayout.setVisibility(View.GONE);
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.header_view_posts_detail, null);
        HeaderViewHolder header = new HeaderViewHolder(view);
        mAdapter.addHeaderView(view);
        if (detail instanceof Entities.ReviewDetail) {
            Entities.ReviewDetail reviewDetail = (Entities.ReviewDetail) detail;
            header.initReviewData(reviewDetail);
        } else if (detail instanceof Entities.DiscussionDetail) {
            Entities.DiscussionDetail discussionDetail = (Entities.DiscussionDetail) detail;
            header.initDiscussionData(discussionDetail);
        } else if (detail instanceof Entities.BookHelp) {
            Entities.BookHelp bookHelp = (Entities.BookHelp) detail;
            header.initHelpData(bookHelp);
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

    @OnClick({ R.id.load_error_view, R.id.comment_send_view })
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
        @BindView(R.id.author_type_view)
        ImageView mAuthorTypeView;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick({ R.id.posts_agreed_view, R.id.posts_more_view })
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
            mBookTitle.setText(detail.review.bookTitle());
            mRatingBar.setStarCount(detail.review.rating);
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
            formatPostDetailContent(detail.review.content);
            mPostAgreedView.setVisibility(View.GONE);
            setAuthorTypeVisible(detail.review.isOfficial(), detail.review.isDoyen());
        }

        private void initDiscussionData(Entities.DiscussionDetail detail) {
            mPostDetailRatingLayout.setVisibility(View.GONE);
            ImageLoader.showCircleImageUrl(mPostDetailAvatarView.getContext(), mPostDetailAvatarView,
                    detail.post.avatar(), R.mipmap.avatar_default);
            mPostDetailAuthorView.setText(AppUtils.getString(R.string.book_detail_review_author, detail.post.nickname(), detail.post.lv()));
            mPostDetailCreateTime.setText(AppUtils.getDescriptionTimeFromDateString(detail.post.created));
            mPostDetailTitleView.setText(detail.post.title);
            formatPostDetailContent(detail.post.content);
            mPostAgreedView.setVisibility(View.VISIBLE);
            setAuthorTypeVisible(detail.post.isOfficial(), detail.post.isDoyen());
        }

        private void initHelpData(Entities.BookHelp bookHelp) {
            mPostDetailRatingLayout.setVisibility(View.GONE);
            ImageLoader.showCircleImageUrl(mPostDetailAvatarView.getContext(), mPostDetailAvatarView,
                    bookHelp.help.avatar(), R.mipmap.avatar_default);
            mPostDetailAuthorView.setText(AppUtils.getString(R.string.book_detail_review_author, bookHelp.help.nickname(), bookHelp.help.lv()));
            mPostDetailCreateTime.setText(AppUtils.getDescriptionTimeFromDateString(bookHelp.help.created));
            mPostDetailTitleView.setText(bookHelp.help.title);
            formatPostDetailContent(bookHelp.help.content);
            mPostAgreedView.setVisibility(View.VISIBLE);
            setAuthorTypeVisible(bookHelp.help.isOfficial(), bookHelp.help.isDoyen());
        }

        private void setAuthorTypeVisible(boolean isOfficial, boolean isDoyen) {
            int resId = 0;
            if (isOfficial) {
                resId = R.mipmap.user_avatar_verify_official;
            } else if (isDoyen) {
                resId = R.mipmap.user_avatar_verify_doyen;
            }
            if (resId != 0) {
                mAuthorTypeView.setVisibility(View.VISIBLE);
                mAuthorTypeView.setImageResource(resId);
            }
        }

        private void formatPostDetailContent(String content) {
            CharSequence colorString = getColorStringForContent(content);
            mPostDetailContent.setText(colorString);
            mPostDetailContent.setHighlightColor(AppUtils.getColor(R.color.text_high_light_color));
            //TextView设置超链接可点击
            mPostDetailContent.setMovementMethod(new LinkTouchMovementMethod());
        }

        private CharSequence getColorStringForContent(String content) {
            List<String> groups = new ArrayList<>();
            List<String> groups1 = new ArrayList<>();
            List<String> replaceGroups = new ArrayList<>();
            Map<String, View.OnClickListener> listenerMap = new HashMap<>();
            String regex1 = "《(.*?)》";
            Pattern p1 = Pattern.compile(regex1);
            Matcher m1 = p1.matcher(content);
            while (m1.find()) {
                String group = m1.group();
                Log.i(TAG, "m1.group=" + group);
                groups1.add(group);
                String key = group.substring(1, group.length() - 1);
                listenerMap.put(group, new OnSearchBookClickListener(mPostDetailContent.getContext(), key));
            }
            String regex2 = "\\[\\[(.*?)\\]\\]";
            Pattern p2 = Pattern.compile(regex2);
            Matcher m2 = p2.matcher(content);
            while (m2.find()) {
                String group = m2.group();
//                Log.i(TAG, "group=" + group);
                String replaceGroup = group.substring(group.indexOf(" ") + 1, group.indexOf("]]"));
                String idType = group.substring(group.indexOf("[[") + 2, group.indexOf(":"));
                String id = group.substring(group.indexOf(":") + 1, group.indexOf(" "));
//                Log.i(TAG, "idType = " + idType + ",id = " + id);
                groups.add(group);
                replaceGroups.add(replaceGroup);
                Entities.RichPost richPost = new Entities.RichPost();
                richPost.idType = idType;
                richPost.id = id;
                listenerMap.put(replaceGroup, new OnPostClickListener(mPostDetailContent.getContext(), richPost));
            }
            for (int j = 0, k = groups.size(); j < k; j++) {
                content = content.replace(groups.get(j), replaceGroups.get(j));
            }
            String regex3 = "\\{\\{(.*?)\\}\\}";
            Pattern p3 = Pattern.compile(regex2);
            Matcher m3 = p2.matcher(content);

            replaceGroups.addAll(0, groups1);
            return RichTextUtils.getColorString(content, replaceGroups, AppUtils.getColor(R.color.orange), listenerMap);
        }
    }

    private static class OnSearchBookClickListener implements View.OnClickListener {
        private String key;
        private WeakReference<Context> mContext;

        OnSearchBookClickListener(Context context, String key) {
            this.key = key;
            mContext = new WeakReference<>(context);
        }

        @Override
        public void onClick(View v) {
            Context context = mContext.get();
            if (context != null) {
                SearchActivity.startActivity(context, key);
            }
        }
    }

    private static class OnPostClickListener implements View.OnClickListener {
        Entities.RichPost richPost;
        private WeakReference<Context> mContext;

        OnPostClickListener(Context context, Entities.RichPost richPost) {
            this.richPost = richPost;
            mContext = new WeakReference<>(context);
        }

        @Override
        public void onClick(View v) {
            Context context = mContext.get();
            if (context != null) {
                richPost.startTargetActivity(context);
            }
        }
    }

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        Adapter() {
            super(null);
            addItemType(Constant.ITEM_TYPE_TEXT, R.layout.item_view_posts_section);
            addItemType(Constant.ITEM_TYPE_HELPFUL, R.layout.item_view_review_helpful);
            addItemType(Constant.ITEM_TYPE_COMMENT, R.layout.item_view_comment);
            addItemType(Constant.ITEM_TYPE_VOTE, R.layout.item_view_vote);
        }

        @Override
        protected void convert(final CommonViewHolder holder, MultiItemEntity item) {
            switch (holder.getItemViewType()) {
            case Constant.ITEM_TYPE_TEXT:
                Entities.PostSection section = (Entities.PostSection) item;
                holder.setText(R.id.section_text_view, section.text);
                break;
            case Constant.ITEM_TYPE_HELPFUL:
                Entities.Helpful helpful = (Entities.Helpful) item;
                holder.setText(R.id.yes_count_text, String.valueOf(helpful.yes))
                        .setText(R.id.no_count_text, String.valueOf(helpful.no))
                        .addOnClickListener(R.id.yes_ll_view)
                        .addOnClickListener(R.id.no_ll_view);
                break;
            case Constant.ITEM_TYPE_COMMENT:
                final Entities.Comment comment = (Entities.Comment) item;
                holder.setCircleImageUrl(R.id.comment_image, comment.avatar(), R.mipmap.avatar_default)
                        .setText(R.id.comment_floor, AppUtils.getString(R.string.comment_floor, comment.floor))
                        .setText(R.id.comment_title, AppUtils.getString(R.string.book_detail_review_author, comment.nickname(), comment.lv()))
                        .setText(R.id.comment_content, comment.content);
                String replayTo = comment.replayTo();
                TextView replayToTv = holder.getView(R.id.comment_reply_to);
                if (!TextUtils.isEmpty(replayTo)) {
                    replayToTv.setVisibility(View.VISIBLE);
                    replayToTv.setText(replayTo);
                } else {
                    replayToTv.setVisibility(View.GONE);
                }
                TextView likeView = holder.getView(R.id.comment_like_count);
                TextView timeView = holder.getView(R.id.comment_time);
                likeView.setText(AppUtils.getString(R.string.comment_like_count, comment.likeCount));
                timeView.setText(AppUtils.getDescriptionTimeFromDateString(comment.created));
                if (comment.isBest) {
                    likeView.setVisibility(View.VISIBLE);
                    timeView.setVisibility(View.INVISIBLE);
                } else {
                    likeView.setVisibility(View.INVISIBLE);
                    timeView.setVisibility(View.VISIBLE);
                }
                holder.setOnClickListener(R.id.reply_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                holder.setOnClickListener(R.id.agree_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.isActivated()) {
                            ToastUtils.showShortToast("已同意");
                            return;
                        }
                        v.setActivated(true);
                    }
                });
                holder.setOnClickListener(R.id.more_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMoreDialog(comment, v.getContext());
                    }
                });
                holder.setOnClickListener(R.id.comment_rl_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SwipeItemLayout sil = (SwipeItemLayout) holder.itemView;
                        if (sil.isOpen()) {
                            sil.close();
                        } else {
                            sil.open();
                        }
                    }
                });
                break;
            case Constant.ITEM_TYPE_VOTE:
                Entities.DiscussionDetail.PostDetail.Vote vote = (Entities.DiscussionDetail.PostDetail.Vote) item;
                holder.setImageResource(R.id.vote_item_number, vote.itemNumberRes)
                        .setText(R.id.vote_content, vote.content);
                break;
            }
        }

        private void showMoreDialog(final Entities.Comment comment, final Context context) {
            final String[] items;
            if (TextUtils.isEmpty(comment.replayTo())) {
                items = new String[] { "举报" };
            } else {
                items = new String[] { "查看回复的楼层", "举报" };
            }
            ItemDialog.Builder builder = new ItemDialog.Builder(context);
            builder.setTitle(R.string.more)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                            case 0:
                                if (items.length == 1) {
                                    showReportDialog(context);
                                } else {

                                }
                                break;
                            case 1:
                                showReportDialog(context);
                                break;
                            }
                        }
                    }).show();
        }

        private void showReportDialog(Context context) {
            String[] items = context.getResources().getStringArray(R.array.report_type);
            ItemDialog.Builder builder = new ItemDialog.Builder(context);
            builder.setTitle("举报")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            }
                            ToastUtils.showShortToast(R.string.report_result_toast);
                        }
                    }).show();
        }
    }
}
