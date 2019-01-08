package com.xzhou.book.read;

import java.util.List;

public class PageLines {

    public int page; //当前章节中第几页
    public List<String> lines;
    public int startPos; //单页内容第一个字符在章节buffer中的位置
    public int endPos;

}
