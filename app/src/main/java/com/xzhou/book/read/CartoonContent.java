package com.xzhou.book.read;

import android.graphics.Bitmap;

public class CartoonContent {

    public String bookId;
    public Bitmap bitmap;
    public String title;
    public String url;
    public float maxScale;
    public int chapter;
    public int curPage;
    public int totalPage;
    public boolean isShow;
    public boolean isStart;
    public boolean isEnd;
    public boolean isLoading;

    public @ReadPresenter.Error
    int error = ReadPresenter.Error.NONE;

    @Override
    public String toString() {
        return "CartoonContent{" +
                "bookId='" + bookId + '\'' +
                ", bitmap=" + bitmap +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", maxScale=" + maxScale +
                ", chapter=" + chapter +
                ", curPage=" + curPage +
                ", totalPage=" + totalPage +
                ", isShow=" + isShow +
                ", isStart=" + isStart +
                ", isEnd=" + isEnd +
                ", isLoading=" + isLoading +
                ", error=" + error +
                '}';
    }
}
