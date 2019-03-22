package com.xzhou.book.read;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.xzhou.book.MyApp;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

public class ChapterBufferImage {
    private static final String TAG = "ChapterBufferImage";
    private Entities.Chapter mReadChapter;
    private List<String> mUrls;
    private List<String> mScales;
    private String mBookId;
    private int mChapter;
    private int totalPage;
    private LruCache<String, Bitmap> mImageCache;

    public ChapterBufferImage(String bookId, int chapter, LruCache<String, Bitmap> imageCache) {
        mBookId = bookId;
        mChapter = chapter;
        mImageCache = imageCache;
    }

    public boolean openCacheBookChapter() {
        File file = new File(FileUtils.getCartoonReadPath(mBookId, mChapter));
        RandomAccessFile raf = null;
        try {
            long len = file.length();
            byte[] bytes = new byte[(int) len];
            raf = new RandomAccessFile(file, "r");
            raf.read(bytes);
            String json = new String(bytes);
            mReadChapter = new Gson().fromJson(json, Entities.Chapter.TYPE);
            if (mReadChapter != null) {
                mUrls = mReadChapter.getImages();
                mScales = mReadChapter.getImageScales();
                totalPage = mUrls.size();
                for (int i = 0; i < totalPage; i++) {
                    File bmpFile = new File(FileUtils.getCartoonPicPath(mBookId, mChapter, i));
                    String url = mUrls.get(i);
                    Bitmap bitmap = mImageCache.get(url);
                    if (bitmap == null) {
                        bitmap = Glide.with(MyApp.getContext()).asBitmap().load(bmpFile).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                        mImageCache.put(url, bitmap);
//                        Log.i(TAG, url + ", bitmap = " + bitmap.getByteCount());
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AppUtils.close(raf);
        }
        return false;
    }

    public boolean openNetBookChapter(Entities.Chapter data, boolean hasSave) {
        mReadChapter = data;
        mUrls = mReadChapter.getImages();
        mScales = mReadChapter.getImageScales();
        totalPage = mUrls.size();
        boolean allDown = true;
        for (int i = 0; i < totalPage; i++) {
            try {
                String url = mUrls.get(i);
                Bitmap bitmap = mImageCache.get(url);
                if (bitmap == null) {
                    bitmap = Glide.with(MyApp.getContext()).asBitmap().load(url).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    mImageCache.put(url, bitmap);
//                    Log.i(TAG, url + ", bitmap = " + bitmap.getByteCount());
                }
                if (hasSave) {
                    File file = new File(FileUtils.getCartoonPicPath(mBookId, mChapter, i));
                    if (file.exists() && file.length() == bitmap.getByteCount()) {
                        continue;
                    }
                    FileUtils.saveBitmap(bitmap, FileUtils.getCartoonPicPath(mBookId, mChapter, i));
                }
            } catch (Exception e) {
                e.printStackTrace();
                allDown = false;
            }
        }
        if (allDown) {
            if (hasSave) {
                try {
                    String content = new Gson().toJson(mReadChapter);
                    FileUtils.writeFile(FileUtils.getCartoonReadPath(mBookId, mChapter), content, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    public String getUrl(int pos) {
        if (mUrls == null || pos < 0 || pos > mUrls.size()) {
            return null;
        }
        return mUrls.get(pos);
    }

    public String getEndUrl() {
        if (mUrls == null || mUrls.size() <= 0) {
            return null;
        }
        return mUrls.get(mUrls.size() - 1);
    }

    public int getEndPos() {
        if (mUrls == null || mUrls.size() <= 0) {
            return -1;
        }
        return mUrls.size() - 1;
    }

    public int getPageCount() {
        if (mUrls == null) {
            return 0;
        }
        return mUrls.size();
    }

    public int getChapter(){
        return mChapter;
    }

    public float getScale(int pos) {
        if (mScales == null || pos < 0 || pos > mScales.size()) {
            return 1.5f;
        }
        try {
            return Float.valueOf(mScales.get(pos));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 1.5f;
    }
}
