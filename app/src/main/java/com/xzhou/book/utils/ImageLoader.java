package com.xzhou.book.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.security.MessageDigest;

public class ImageLoader {

    private static RequestOptions getOptions(@DrawableRes int placeholder, @DrawableRes int error) {
        if (error != 0) {
            return new RequestOptions().centerInside().placeholder(placeholder);
        }
        return new RequestOptions().centerInside().placeholder(placeholder).error(error);
    }

    private static RequestOptions getOptions(@DrawableRes int placeholder) {
        return new RequestOptions().centerInside().placeholder(placeholder);
    }

    private static RequestOptions getCircleOptions(@DrawableRes int placeholder) {
        return new RequestOptions().centerInside().placeholder(placeholder).transform(new GlideCircleTransform());
    }

    public static void showImageFile(Context context, ImageView imageView, @NonNull File file, @DrawableRes int placeholder) {
        showImageFile(context, imageView, file, placeholder, 0);
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
        Glide.with(context).asGif().listener(listener).load(file).apply(new RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, @NonNull String uri, @DrawableRes int placeholder) {
        Glide.with(context).load(uri).apply(getOptions(placeholder)).into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, @NonNull String uri, @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context).load(uri).apply(getOptions(placeholder, error)).into(imageView);
    }

    public static void showCircleImageUrl(Context context, ImageView imageView, @NonNull String uri, @DrawableRes int placeholder) {
        Glide.with(context).load(uri).apply(getCircleOptions(placeholder)).into(imageView);
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

    private static class GlideCircleTransform extends BitmapTransformation {

        protected Bitmap transform(BitmapPool pool, Bitmap toTransform,
                                   int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null)
                return null;
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP,
                    BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

        }
    }
}
