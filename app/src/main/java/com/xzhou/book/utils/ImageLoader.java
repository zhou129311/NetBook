package com.xzhou.book.utils;

import android.annotation.DrawableRes;
import android.annotation.NonNull;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

public class ImageLoader {

    private static RequestOptions mOptions = new RequestOptions()
            .centerInside();

    public static RequestOptions getOptions(@DrawableRes int placeholder, @DrawableRes int error) {
        if (error < 0) {
            return mOptions.placeholder(placeholder);
        }
        return mOptions.placeholder(placeholder).error(error);
    }

    public static RequestOptions getOptions(@DrawableRes int placeholder) {
        return mOptions.placeholder(placeholder);
    }

    public static void changeImageViewSize(ImageView imageView, int width, int height) {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = AppUtils.dip2px(width);
        lp.height = AppUtils.dip2px(height);
        imageView.setLayoutParams(lp);
    }

    public static void showImageFile(Context context, ImageView imageView, @NonNull File file, @DrawableRes int placeholder) {
        showImageFile(context, imageView, file, placeholder, -1);
    }

    public static void showImageFile(Context context, ImageView imageView, @NonNull File file, @DrawableRes int placeholder, @DrawableRes int error) {
        if (!file.exists()) {
            return;
        }
        if (file.getAbsolutePath().toLowerCase().endsWith("gif")) {
            Glide.with(context).asGif().load(file).apply(getOptions(placeholder, error).diskCacheStrategy(DiskCacheStrategy.DATA)).into(imageView);
        } else {
            Glide.with(context).asBitmap().load(file).apply(getOptions(placeholder, error)).into(imageView);
        }
    }

    public static void showImageGif(Context context, ImageView imageView, @NonNull File file, RequestListener<GifDrawable> listener) {
        if (!file.exists()) {
            return;
        }
        Glide.with(context).asGif().listener(listener).load(file).apply(mOptions
                .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, @NonNull String uri, @DrawableRes int placeholder) {
        Glide.with(context).load(uri).apply(getOptions(placeholder)).into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, @NonNull String uri, @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context).load(uri).apply(getOptions(placeholder, error)).into(imageView);
    }

    public static void showImageBitmap(Context context, ImageView imageView, @NonNull Bitmap bitmap, @DrawableRes int error) {
        Glide.with(context).load(bitmap).apply(getOptions(error, error)).into(imageView);
    }

    public static void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    public static void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }
}
