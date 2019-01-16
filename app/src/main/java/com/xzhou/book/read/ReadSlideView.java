package com.xzhou.book.read;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xzhou.book.db.BookProvider;
import com.xzhou.book.R;
import com.xzhou.book.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadSlideView extends LinearLayout {

    @BindView(R.id.source_recycler_view)
    RecyclerView mRecyclerView;

    private BookProvider.LocalBook mBook;

    public ReadSlideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);
    }

    public void setBook(BookProvider.LocalBook book) {
        mBook = book;
    }

    public void setSource() {

    }

    @OnClick({ R.id.more_source_tv, R.id.discussion_item_tv, R.id.recommend_item_tv })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.more_source_tv:

            break;
        case R.id.discussion_item_tv:
            AppUtils.startDiscussionByBook(getContext(), mBook.title, mBook._id, 0);
            break;
        case R.id.recommend_item_tv:
            AppUtils.startRecommendByBook(getContext(), mBook._id);
            break;
        }
    }
}
