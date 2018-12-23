package com.xzhou.book.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.ImageLoader;
import com.xzhou.book.widget.DrawableButton;
import com.xzhou.book.widget.RatingBar;
import com.xzhou.book.widget.TagColor;
import com.xzhou.book.widget.TagGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookDetailActivity extends BaseActivity implements BookDetailContract.View {
    private static final String TAG = "BookDetailActivity";

    public static final String EXTRA_BOOKID = "extra_bookid";

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
    @BindView(R.id.detail_collector)
    DrawableButton detailCollector;
    @BindView(R.id.detail_read)
    DrawableButton detailRead;
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

    private BookDetailContract.Presenter mPresenter;

    public static void startActivity(Context context, String bookId) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra(EXTRA_BOOKID, bookId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        mPresenter = new BookDetailPresenter(this, getIntent().getStringExtra(EXTRA_BOOKID));
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitle(R.string.book_detail);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startData();
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
        mPresenter.destroy();
    }

    @Override
    public void onInitBookDetail(Entities.BookDetail detail) {
        mLoadView.setVisibility(View.GONE);
        if (detail != null) {
            detailBookTitle.setFocusable(true);
            mPlaceView.setVisibility(View.GONE);
            mLoadErrorView.setVisibility(View.GONE);

            ImageLoader.showImageUrl(this, detailBookImg, detail.cover(), R.mipmap.ic_cover_default);
            detailBookTitle.setText(detail.title);
            detailBookAuthor.setText(detail.author);
            detailBookCat.setText(getString(R.string.book_detail_cat, detail.cat));
            detailWordCount.setText(AppUtils.formatWordCount(detail.wordCount));
            detailLastUpdated.setText(AppUtils.getDescriptionTimeFromDateString(detail.updated));
            detailCollector.setActivated(detail.isSaveBookshelf);
            if (!detailCollector.isActivated()) {
                detailCollector.setText(R.string.book_detail_join_collection);
            } else {
                detailCollector.setText(R.string.book_detail_remove_collection);
            }
            detailLatFollower.setText(String.valueOf(detail.latelyFollower));
            detailRetentionRatio.setText(AppUtils.isEmpty(detail.retentionRatio) ?
                    "-" : String.format(getString(R.string.book_detail_retention_ratio), detail.retentionRatio));
            detailDayWordCount.setText(detail.serializeWordCount < 1 ? "-" : String.valueOf(detail.serializeWordCount));
            detailIntro.setText(AppUtils.isEmpty(detail.longIntro) ? "暂无简介" : detail.longIntro);
            initTagView(detail.tags);
            initCommunity(detail);
        } else {
            mLoadErrorView.setVisibility(View.VISIBLE);
        }
    }

    private void initTagView(List<String> tags) {
        if (tags == null || tags.size() < 1) {
            detailGroupTagDivider.setVisibility(View.GONE);
            detailGroupTag.setVisibility(View.GONE);
        } else {
            List<TagColor> colors = TagColor.getRandomColors(tags.size());
            detailGroupTag.setTags(colors, (String[]) tags.toArray(new String[tags.size()]));
            detailGroupTag.setOnTagClickListener(new TagGroup.OnTagClickListener() {
                @Override
                public void onTagClick(String tag) {

                }
            });
        }
    }

    private void initCommunity(Entities.BookDetail detail) {
        detailCommunityTitle.setText(getString(R.string.book_detail_community, detail.title));
        detailCommunityCount.setText(getString(R.string.book_detail_post_count, detail.postCount));
        detailGroupCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //书籍社区详情
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

    private void initRecyclerView(List<MultiItemEntity> list, RecyclerView recyclerView) {
        Adapter adapter = new Adapter(list);
        adapter.bindToRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);
        if (list.get(0).getItemType() == Constant.ITEM_TYPE_REVIEWS) {
            recyclerView.setLayoutManager(new MyLinearLayoutManager(this, true));
        } else {
            int spanCount = 4;
            recyclerView.setLayoutManager(new MyGridLayoutManager(this, spanCount, true));
            int space = (recyclerView.getWidth() - AppUtils.dip2px(60)) / (spanCount - 1);
            recyclerView.addItemDecoration(new GridItemDecoration(spanCount, space, 0));
        }
    }

    @Override
    public void setPresenter(BookDetailContract.Presenter presenter) {
    }

    @OnClick({R.id.load_error_view, R.id.detail_book_author, R.id.detail_collector, R.id.detail_read
            , R.id.detail_intro, R.id.detail_more_reviews, R.id.detail_more_recommend})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.load_error_view:
                startData();
                break;
            case R.id.detail_book_author:
                break;
            case R.id.detail_collector:
                break;
            case R.id.detail_read:
                break;
            case R.id.detail_intro:
                if (detailIntro.getMaxLines() == 4) {
                    detailIntro.setMaxLines(20);
                } else {
                    detailIntro.setMaxLines(4);
                }
                break;
            case R.id.detail_more_reviews:
                break;
            case R.id.detail_more_recommend:
                break;
        }
    }

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        Adapter(List<MultiItemEntity> data) {
            super(data);
            addItemType(Constant.ITEM_TYPE_REVIEWS, R.layout.book_detail_item_review_view);
            addItemType(Constant.ITEM_TYPE_NET_BOOK, R.layout.book_detail_item_recommend_view);
        }

        @Override
        protected void convert(CommonViewHolder holder, MultiItemEntity item) {
            switch (holder.getItemViewType()) {
                case Constant.ITEM_TYPE_REVIEWS:
                    Entities.Reviews reviews = (Entities.Reviews) item;
                    holder.setCircleImageUrl(R.id.review_img, reviews.avatar(), R.mipmap.avatar_default)
                            .setText(R.id.review_author, AppUtils.getString(R.string.book_detail_review_author,
                                    reviews.author.nickname, reviews.author.lv))
                            .setText(R.id.review_title, reviews.title)
                            .setText(R.id.review_content, reviews.content)
                            .setText(R.id.review_useful_yes, String.valueOf(reviews.helpful.yes));
                    RatingBar ratingBar = holder.getView(R.id.review_rating_bar);
                    ratingBar.setActiveCount(reviews.rating);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    break;
                case Constant.ITEM_TYPE_NET_BOOK:
                    Entities.NetBook book = (Entities.NetBook) item;
                    holder.setImageUrl(R.id.book_detail_recommend_img, book.cover(), R.mipmap.ic_cover_default)
                            .setText(R.id.book_detail_recommend_title, book.title);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    break;
            }
        }
    }
}
