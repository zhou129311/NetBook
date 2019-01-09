package com.xzhou.book.read;

import com.xzhou.book.utils.Log;

public class PageContent {
    public String bookId;
    public int chapter;
    public PageLines mPageLines;
    public String chapterTitle = "";
    public boolean isLoading; //章节内容是否加载中
    public boolean isShow;
    public boolean isEnd; //是否最后一页
    public boolean isStart; //是否第一页
    public int pageSize; //当前章节总页数

    public @ReadPresenter.Error
    int error = ReadPresenter.Error.NONE;

    public void clear() {
        if (mPageLines != null) {
            mPageLines = null;
        }
        pageSize = 0;
        chapterTitle = "";
        isShow = false;
        isLoading = false;
        isEnd = false;
        isStart = false;
        error = ReadPresenter.Error.NONE;
    }

    public String getCurPagePos() {
        if (pageSize > 0 && mPageLines != null) {
            return (mPageLines.page + 1) + "/" + pageSize;
        } else {
            return "";
        }
    }

    public String getPageContent() {
        if (mPageLines == null) {
            Log.e("getPageContent is null");
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String line : mPageLines.lines) {
            sb.append(line);
        }
        return sb.toString();
    }
}
