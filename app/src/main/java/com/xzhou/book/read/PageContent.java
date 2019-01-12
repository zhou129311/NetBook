package com.xzhou.book.read;

import java.util.List;

public class PageContent {
    public String bookId;
    public int chapter = -1;
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
        chapter = -1;
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
            return "";
        }
        return mPageLines.getPageContent();
    }

    public List<String> getLines() {
        if (mPageLines == null) {
            return null;
        }
        return mPageLines.lines;
    }

    @Override
    public String toString() {
        return "PageContent{" +
                "bookId='" + bookId + '\'' +
                ", chapter=" + chapter +
                ", chapterTitle='" + chapterTitle + '\'' +
                ", isEnd=" + isEnd +
                ", isStart=" + isStart +
                ", isShow=" + isShow +
                ", isLoading=" + isLoading +
                ", pageSize=" + pageSize +
                ", page=" + (mPageLines != null ? mPageLines.page : "null") +
                '}';
    }
}
