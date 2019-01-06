package com.xzhou.book.read;

import com.xzhou.book.utils.Log;

import java.util.List;

public class PageContent {
    public String bookId;
    public int chapter;
    public int pageNumber;
    public List<String> lines;
    public int startPos; //单页内容第一个字符在章节buffer中的位置
    public int endPos;

    public String getPageContent() {
        if (lines == null) {
            Log.e("getPageContent is null");
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
        }
        return sb.toString();
    }
}
