package com.xzhou.book.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.Util;
import com.xzhou.book.BuildConfig;

import java.io.File;
import java.security.MessageDigest;

public class ImageLoader {

    private static GlideRoundTransform sRoundTransform = new GlideRoundTransform();
    private static GlideCircleTransform sCircleTransform = new GlideCircleTransform();

    private static RequestOptions getOptions(@DrawableRes int placeholder, @DrawableRes int error) {
        if (error != 0) {
            return new RequestOptions().centerInside().placeholder(placeholder);
        }
        return new RequestOptions().centerInside().placeholder(placeholder).error(error);
    }

    private static RequestOptions getOptions(@DrawableRes int placeholder) {
        return new RequestOptions().centerInside().placeholder(placeholder);
    }

    public static RequestOptions getCircleOptions(@DrawableRes int placeholder) {
        return new RequestOptions().centerInside().placeholder(placeholder).transform(sCircleTransform);
    }

    private static RequestOptions getRoundOptions(@DrawableRes int placeholder) {
        return new RequestOptions().centerInside().placeholder(placeholder).transform(sRoundTransform);
    }

    public static void showImageFile(Context context, ImageView imageView, File file, @DrawableRes int placeholder) {
        showImageFile(context, imageView, file, placeholder, 0);
    }

    public static void showImageFile(Context context, ImageView imageView, File file, @DrawableRes int placeholder, @DrawableRes int error) {
        if (!file.exists()) {
            return;
        }
        if (file.getAbsolutePath().toLowerCase().endsWith("gif")) {
            Glide.with(context).asGif().load(file).apply(getOptions(placeholder, error).diskCacheStrategy(DiskCacheStrategy.DATA)).into(imageView);
        } else {
            Glide.with(context).asBitmap().load(file).apply(getOptions(placeholder, error)).into(imageView);
        }
    }

    public static void showImageGif(Context context, ImageView imageView, File file, RequestListener<GifDrawable> listener) {
        if (!file.exists()) {
            return;
        }
        Glide.with(context).asGif().listener(listener).load(file).apply(new RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, String uri, @DrawableRes int placeholder) {
        if (AppSettings.HAS_SAVING_TRAFFIC || uri == null) {
            uri = idToUri(placeholder);
        }
        Glide.with(context).load(uri).apply(getOptions(placeholder)).into(imageView);
    }

    public static void showImageUrl(Context context, ImageView imageView, String uri, @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context).load(uri).apply(getOptions(placeholder, error)).into(imageView);
    }

    public static void showCircleImageUrl(Context context, ImageView imageView, String uri, @DrawableRes int placeholder) {
        if (AppSettings.HAS_SAVING_TRAFFIC || uri == null) {
            uri = idToUri(placeholder);
        }
        Glide.with(context).load(uri).apply(getCircleOptions(placeholder)).into(imageView);
    }

    public static void showRoundImageUrl(Context context, ImageView imageView, String uri, @DrawableRes int placeholder) {
        if (AppSettings.HAS_SAVING_TRAFFIC || uri == null) {
            uri = idToUri(placeholder);
        }
        Glide.with(context).load(uri).apply(getRoundOptions(placeholder)).into(imageView);
    }

    public static void showImageBitmap(Context context, ImageView imageView, Bitmap bitmap, @DrawableRes int error) {
        Glide.with(context).load(bitmap).apply(getOptions(error, error)).into(imageView);
    }

    public static void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    public static void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }

    public static String idToUri(int resourceId) {
        return "android.resource://" + BuildConfig.APPLICATION_ID + "/" + resourceId;
    }

    private static class GlideCircleTransform extends BitmapTransformation {
        private final String TAG = getClass().getName();

        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            int size = Math.min(toTransform.getWidth(), toTransform.getHeight());
            int x = (toTransform.getWidth() - size) / 2;
            int y = (toTransform.getHeight() - size) / 2;
            Bitmap squared = Bitmap.createBitmap(toTransform, x, y, size, size);
            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GlideCircleTransform;
        }

        @Override
        public int hashCode() {
            return Util.hashCode(TAG.hashCode());
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(TAG.getBytes(CHARSET));
        }
    }

    private static class GlideRoundTransform extends BitmapTransformation {
        private final String TAG = getClass().getName();

        private float mRadius = 0f;

        GlideRoundTransform() {
            this(2);
        }

        GlideRoundTransform(int dp) {
            mRadius = Resources.getSystem().getDisplayMetrics().density * dp;
        }

        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Bitmap result = pool.get(toTransform.getWidth(), toTransform.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(toTransform, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, toTransform.getWidth(), toTransform.getHeight());
            canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GlideRoundTransform;
        }

        @Override
        public int hashCode() {
            return Util.hashCode(TAG.hashCode());
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(TAG.getBytes(CHARSET));
        }
    }
}
