package com.xzhou.book.models;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class BaiduEntities {
    public static final List<String> BOOK_HOSTS = new ArrayList<String>() {
        {
            add("https://www.tianxiabachang.cn"); //笔趣阁
            add("www.sodu.cc"); //SoDu
            add("www.77nt.com"); //平板电子书网
        }
    };

    public static class BaiduBook implements MultiItemEntity {
        public String image;
        public String sourceName;
        public String sourceHost;
        public String mobReadUrl;
        public String author;
        public String bookName;
        public String readUrl;
        public String latestChapterName;
        public String latestChapterUrl;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_BAIDU_BOOK;
        }

        public boolean hasValid() {
            return !TextUtils.isEmpty(readUrl) && !TextUtils.isEmpty(bookName) && !TextUtils.isEmpty(mobReadUrl);
        }

        @Override
        public String toString() {
            return "BaiduBook{" +
                    "image='" + image + '\'' +
                    ", sourceName='" + sourceName + '\'' +
                    ", sourceHost='" + sourceHost + '\'' +
                    ", author='" + author + '\'' +
                    ", bookName='" + bookName + '\'' +
                    ", readUrl='" + readUrl + '\'' +
                    ", mobReadUrl='" + mobReadUrl + '\'' +
                    ", latestChapterName='" + latestChapterName + '\'' +
                    ", latestChapterUrl='" + latestChapterUrl + '\'' +
                    '}';
        }
    }

}
