package com.xzhou.book.community;

import com.xzhou.book.common.BasePresenter;

public class PostsDetailPresenter extends BasePresenter<PostsDetailContract.View> implements PostsDetailContract.Presenter {

    public PostsDetailPresenter(PostsDetailContract.View view) {
        super(view);
    }

    @Override
    public boolean start() {
        return super.start();
    }
}
