package com.xzhou.book.common;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ImageLoader;

public class CommonViewHolder extends BaseViewHolder {

    public CommonViewHolder(View view) {
        super(view);
    }

    public CommonViewHolder setImageUrl(@IdRes int viewId, String url, @DrawableRes int defaultId) {
        ImageView view = getView(viewId);
        ImageLoader.showImageUrl(view.getContext(), view, url, defaultId);
        return this;
    }

    public CommonViewHolder setRoundImageUrl(@IdRes int viewId, String url, @DrawableRes int defaultId) {
        ImageView view = getView(viewId);
        ImageLoader.showRoundImageUrl(view.getContext(), view, url, defaultId);
        return this;
    }

    public CommonViewHolder setCircleImageUrl(@IdRes int viewId, String url, @DrawableRes int defaultId) {
        ImageView view = getView(viewId);
        ImageLoader.showCircleImageUrl(view.getContext(), view, url, defaultId);
        return this;
    }

    public CommonViewHolder changeImageViewSize(@IdRes int viewId, int width, int height) {
        ImageView imageView = getView(viewId);
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = AppUtils.dip2px(width);
        lp.height = AppUtils.dip2px(height);
        imageView.setLayoutParams(lp);
        return this;
    }
}
