package com.xzhou.book.read;

import com.xzhou.book.utils.Log;

public class PageContent {
    public String bookId;
    public int chapter;
    public PageLines mPageLines;
    public String chapterTitle = "";
    public String curPagePos = "";
    public boolean isLoading; //章节内容是否加载中
    public boolean isShow;
    public boolean isEnd; //最后一页
    public boolean isStart; //第一页

    public @ReadPresenter.Error
    int error = ReadPresenter.Error.NONE;

    public void clear() {
        if (mPageLines != null) {
            mPageLines = null;
        }
        chapterTitle = "";
        curPagePos = "";
        isShow = false;
        isLoading = false;
        isEnd = false;
        isStart = false;
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
