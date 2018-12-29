package com.xzhou.book.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 帖子详情，包括 书评、讨论、综合讨论、女生话题等等
 */
public class PostsDetailActivity extends BaseActivity<PostsDetailContract.Presenter> implements PostsDetailContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.load_error_view)
    ImageView mLoadErrorView;
    @BindView(R.id.common_load_view)
    ProgressBar mLoadView;
    @BindView(R.id.comment_edit_view)
    EditText mEditView;
    @BindView(R.id.comment_send_view)
    ImageView mCommentSendView;

    public static void startActivity(Context context, String reviewId) {
        Intent intent = new Intent(context, PostsDetailActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_detail);
    }

    @Override
    protected PostsDetailContract.Presenter createPresenter() {
        return new PostsDetailPresenter(this);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setNavigationIcon(R.mipmap.ab_back);
        mToolbar.setTitle("详情");
        mToolbar.setTitleTextAppearance(this, R.style.TitleTextStyle);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void setPresenter(PostsDetailContract.Presenter presenter) {
    }

    @OnClick({ R.id.load_error_view, R.id.comment_send_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.load_error_view:
            break;
        case R.id.comment_send_view:
            break;
        }
    }

    private static class Adapter extends BaseMultiItemQuickAdapter<MultiItemEntity, CommonViewHolder> {

        public Adapter(List<MultiItemEntity> data) {
            super(data);
        }

        @Override
        protected void convert(CommonViewHolder holder, MultiItemEntity item) {

        }
    }
}
