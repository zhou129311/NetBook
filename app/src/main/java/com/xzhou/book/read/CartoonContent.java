package com.xzhou.book.read;

import android.graphics.Bitmap;

public class CartoonContent {

    public String bookId;
    public Bitmap bitmap;
    public String title;
    public int chapter;
    public int curPage;
    public int totalPage;
    public boolean isShow;
    public boolean isStart;
    public boolean isEnd;
    public boolean isLoading;

    public @ReadPresenter.Error
    int error = ReadPresenter.Error.NONE;
}
