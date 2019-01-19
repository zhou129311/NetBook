package com.xzhou.book.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.GridItemDecoration;
import com.xzhou.book.common.MyGridLayoutManager;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.common.TabActivity;
import com.xzhou.book.community.PostsDetailActivity;
import com.xzhou.book.db.BookManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Constant.TabSource;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.utils.SnackBarUtils;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.DrawableButton;
import com.xzhou.book.widget.RatingBar;
import com.xzhou.book.widget.TagColor;
import com.xzhou.book.widget.TagGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookDetailActivity extends BaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {
    private static final String TAG = "BookDetailActivity";

    public static final String EXTRA_BOOK_ID = "extra_bookId";

    @BindView(R.id.detail_book_img)
    ImageView detailBookImg;
    @BindView(R.id.detail_book_title)
    TextView detailBookTitle;
    @BindView(R.id.detail_book_author)
    TextView detailBookAuthor;
    @BindView(R.id.detail_book_cat)
    TextView detailBookCat;
    @BindView(R.id.detail_word_count)
    TextView detailWordCount;
    @BindView(R.id.detail_last_updated)
    TextView detailLastUpdated;
    @BindView(R.id.detail_join)
    DrawableButton mJoinBtn;
    @BindView(R.id.detail_read)
    DrawableButton mReadBtn;
    @BindView(R.id.detail_lat_follower)
    TextView detailLatFollower;
    @BindView(R.id.detail_retention_ratio)
    TextView detailRetentionRatio;
    @BindView(R.id.detail_day_word_count)
    TextView detailDayWordCount;
    @BindView(R.id.detail_intro)
    TextView detailIntro;

    //热门书评
    @BindView(R.id.detail_more_reviews)
    TextView detailMoreReviews;
    @BindView(R.id.detail_reviews_recycler_view)
    RecyclerView detailReviewsRecyclerView;
    @BindView(R.id.detail_group_reviews_divider)
    View detailGroupReviewsDivider;
    @BindView(R.id.detail_group_reviews)
    RelativeLayout detailGroupReviews;

    //本书社区
    @BindView(R.id.detail_community_title)
    TextView detailCommunityTitle;
    @BindView(R.id.detail_community_count)
    TextView detailCommunityCount;
    @BindView(R.id.detail_group_community)
    RelativeLayout detailGroupCommunity;

    //推荐列表
    @BindView(R.id.detail_group_recommend_divider)
    View detailGroupRecommendDivider;
    @BindView(R.id.detail_recommend)
    TextView detailRecommend;
    @BindView(R.id.detail_more_recommend)
    TextView detailMoreRecommend;
    @BindView(R.id.detail_recommend_recycler_view)
    RecyclerView detailRecommendRecyclerView;
    @BindView(R.id.detail_group_recommend)
    RelativeLayout detailGroupRecommend;

    //本书标签
    @BindView(R.id.detail_group_tag_divider)
    View detailGroupTagDivider;
    @BindView(R.id.detail_group_tag)
    TagGroup detailGroupTag;

    //占位
    @BindView(R.id.place_view)
    FrameLayout mPlaceView;
    @BindView(R.id.load_error_view)
    View mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;

    private Entities.BookDetail mDetail;
    private SnackBarUtils mSnackBar;

    public static void startActivity(Context context, String bookId) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected BookDetailContract.Presenter createPresenter() {
        return new BookDetailPresenter(this, getIntent().getStringExtra(EXTRA_BOOK_ID));
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(R.string.book_detail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_download && mDetail != null) {
            if (!mPresenter.download()) {
                ToastUtils.showShortToast("正在缓存中...");
            } else {
                BookProvider.insertOrUpdate(new BookProvider.LocalBook(mDetail), false);
                updateJoinBtn(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetail != null) {
            updateJoinBtn(BookProvider.hasCacheData(mDetail._id));
        }
    }

    private void startData() {
        if (mPresenter.start()) {
            mPlaceView.setVisibility(View.VISIBLE);
            mLoadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onInitBookDetail(Entities.BookDetail detail) {
        mLoadView.setVisibility(View.GONE);
        if (detail != null) {
            mDetail = detail;
            ViewGroup parent = (ViewGroup) mPlaceView.getParent();
            parent.removeView(mPlaceView);
            detailBookTitle.setFocusable(true);

            ImageLoader.showRoundImageUrl(this, detailBookImg, detail.cover(), R.mipmap.ic_cover_default);
            detailBookTitle.setText(detail.title);
            detailBookAuthor.setText(detail.author);
            detailBookCat.setText(getString(R.string.book_detail_cat, detail.cat));
            detailWordCount.setText(AppUtils.formatWordCount(detail.wordCount));
            detailLastUpdated.setText(AppUtils.getDescriptionTimeFromDateString(detail.updated));
            updateJoinBtn(BookProvider.hasCacheData(mDetail._id));
            detailLatFollower.setText(String.valueOf(detail.latelyFollower));
            detailRetentionRatio.setText(AppUtils.isEmpty(detail.retentionRatio) ?
                    "-" : String.format(getString(R.string.book_detail_retention_ratio), detail.retentionRatio));
            detailDayWordCount.setText(detail.serializeWordCount < 1 ? "-" : String.valueOf(detail.serializeWordCount));
            detailIntro.setText(AppUtils.isEmpty(detail.longIntro) ? AppUtils.getString(R.string.detail_no_intro) : detail.longIntro);
            initTagView(detail.tags);
            initCommunity();
        } else {
            mLoadErrorView.setVisibility(View.VISIBLE);
        }
    }

    private void updateJoinBtn(boolean activated) {
        mJoinBtn.setActivated(activated);
        if (!mJoinBtn.isActivated()) {
            mJoinBtn.setText(R.string.book_detail_join_collection);
        } else {
            mJoinBtn.setText(R.string.book_detail_remove_collection);
        }
    }

    private void initTagView(List<String> tags) {
        if (tags == null || tags.size() < 1) {
            detailGroupTagDivider.setVisibility(View.GONE);
            detailGroupTag.setVisibility(View.GONE);
        } else {
            List<TagColor> colors = TagColor.getRandomColors(tags.size());
            detailGroupTag.setTags(colors, (String[]) tags.toArray(new String[0]));
            detailGroupTag.setOnTagClickListener(new TagGroup.OnTagClickListener() {
                @Override
                public void onTagClick(String tag) {
                    if (AppUtils.isEmpty(tag)) {
                        return;
                    }
                    Entities.TabData data = new Entities.TabData();
                    data.title = tag;
                    data.source = TabSource.SOURCE_TAG;
                    data.params = new String[] { tag };
                    TabActivity.startActivity(mActivity, data);
                }
            });
        }
    }

    private void initCommunity() {
        detailCommunityTitle.setText(getString(R.string.book_detail_community, mDetail.title));
        detailCommunityCount.setText(getString(R.string.book_detail_post_count, mDetail.postCount));
        detailGroupCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.startDiscussionByBook(mActivity, mDetail.title, mDetail._id, 0);
            }
        });
    }

    @Override
    public void onInitReviews(List<MultiItemEntity> list) {
        if (list != null && list.size() > 0) {
            detailGroupReviewsDivider.setVisibility(View.VISIBLE);
            detailGroupReviews.setVisibility(View.VISIBLE);
            initRecyclerView(list, detailReviewsRecyclerView);
        }
    }

    @Override
    public void onInitRecommend(List<MultiItemEntity> list) {
        if (list != null && list.size() > 0) {
            detailGroupRecommendDivider.setVisibility(View.VISIBLE);
            detailGroupRecommend.setVisibility(View.VISIBLE);
            initRecyclerView(list, detailRecommendRecyclerView);
        }
    }

    @Override
    public void onStartDownload() {
        String text = getString(R.string.book_read_download_start, mDetail.title);
        if (mSnackBar == null) {
            mSnackBar = SnackBarUtils.makeIndefinite(getContentView(), text);
            mSnackBar.show(getResources().getColor(R.color.colorPrimary));
        } else {
            mSnackBar.show();
            mSnackBar.setText(text);
        }
    }

    @Override
    public void onProgress(int progress, int max) {
        String text = getString(R.string.book_read_download_progress, mDetail.title, progress, max);
        if (mSnackBar == null) {
            mSnackBar = SnackBarUtils.makeIndefinite(getContentView(), text);
            mSnackBar.show(getResources().getColor(R.color.colorPrimary));
        } else {
            mSnackBar.show();
            mSnackBar.setText(text);
        }
    }

    @Override
    public void onEndDownload(int failedCount, int error) {
        String text = getString(R.string.book_read_download_complete, mDetail.title);
        if (failedCount > 0) {
            text = getString(R.string.book_read_download_complete2, mDetail.title, failedCount);
        }
        if (mSnackBar == null) {
            mSnackBar = SnackBarUtils.makeIndefinite(getContentView(), text);
            mSnackBar.show(getResources().getColor(R.color.colorPrimary));
        } else {
            mSnackBar.show();
            mSnackBar.setText(text);
        }
        getContentView().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSnackBar.hide();
            }
        }, 2000);
    }

    private void initRecyclerView(List<MultiItemEntity> list, final RecyclerView recyclerView) {
        Adapter adapter = new Adapter(list);
        adapter.bindToRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);
        if (list.get(0).getItemType() == Constant.ITEM_TYPE_REVIEWS) {
            recyclerView.setLayoutManager(new MyLinearLayoutManager(this, true));
        } else {
            final int spanCount = 4;
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int space = (recyclerView.getWidth() - (AppUtils.dip2px(60) * spanCount)) / (spanCount - 1);
                    recyclerView.addItemDecoration(new GridItemDecoration(spanCount, space, 0));
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            recyclerView.setLayoutManager(new MyGridLayoutManager(this, spanCount, true));
        }
    }

    @OnClick({ R.id.load_error_view, R.id.detail_book_author, R.id.detail_join, R.id.detail_read
            , R.id.detail_intro, R.id.detail_more_reviews, R.id.detail_more_recommend })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.load_error_view:
            startData();
            break;
        case R.id.detail_book_author: {
            Entities.TabData data = new Entities.TabData();
            data.title = detailBookAuthor.getText().toString();
            data.source = TabSource.SOURCE_AUTHOR;
            data.params = new String[] { data.title };
            TabActivity.startActivity(mActivity, data);
            break;
        }
        case R.id.detail_join:
            if (BookProvider.hasCacheData(mDetail._id)) {
                BookProvider.delete(mDetail._id, mDetail.title);
                updateJoinBtn(false);
            } else {
                BookProvider.insertOrUpdate(new BookProvider.LocalBook(mDetail), false);
                updateJoinBtn(true);
            }
            break;
        case R.id.detail_read:
            ReadActivity.startActivity(mActivity, new BookProvider.LocalBook(mDetail));
            break;
        case R.id.detail_intro:
            if (detailIntro.getMaxLines() == 4) {
                detailIntro.setMaxLines(Integer.MAX_VALUE);
            } else {
                detailIntro.setMaxLines(4);
            }
            break;
        case R.id.detail_more_reviews:
            AppUtils.startDiscussionByBook(mActivity, mDetail.title, mDetail._id, 1);
            break;
        case R.id.detail_more_recommend: {
            AppUtils.startRecommendByBook(mActivity, getIntent().getStringExtra(EXTRA_BOOK_ID));
            break;
        }
        }
    }

    @Override
    public void setPresenter(BookDetailContract.Presenter presenter) {
    }

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        Adapter(List<MultiItemEntity> data) {
            super(data);
            addItemType(Constant.ITEM_TYPE_REVIEWS, R.layout.item_view_review);
            addItemType(Constant.ITEM_TYPE_NET_BOOK, R.layout.book_detail_item_recommend_view);
        }

        @Override
        protected void convert(CommonViewHolder holder, MultiItemEntity item) {
            switch (holder.getItemViewType()) {
            case Constant.ITEM_TYPE_REVIEWS:
                final Entities.Reviews reviews = (Entities.Reviews) item;
                holder.setCircleImageUrl(R.id.review_img, reviews.avatar(), R.mipmap.avatar_default)
                        .setText(R.id.review_author, AppUtils.getString(R.string.book_detail_review_author,
                                reviews.nickname(), reviews.lv()))
                        .setText(R.id.review_title, reviews.title)
                        .setText(R.id.review_content, reviews.content)
                        .setText(R.id.review_useful_yes, String.valueOf(reviews.yes()));
                RatingBar ratingBar = holder.getView(R.id.review_rating_bar);
                ratingBar.setStarCount(reviews.rating);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostsDetailActivity.startActivity(mContext, reviews._id, PostsDetailActivity.TYPE_REVIEW);
                    }
                });
                break;
            case Constant.ITEM_TYPE_NET_BOOK:
                final Entities.NetBook book = (Entities.NetBook) item;
                holder.setRoundImageUrl(R.id.book_detail_recommend_img, book.cover(), R.mipmap.ic_cover_default)
                        .setText(R.id.book_detail_recommend_title, book.title);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BookDetailActivity.startActivity(getRecyclerView().getContext(), book._id);
                    }
                });
                break;
            }
        }
    }
}
