package com.xzhou.book;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.utils.Constant;

import java.util.List;

public class BookManager {

    public static class LocalBook implements MultiItemEntity {
        public String _id;
        public long updated;
        public long readTime;
        public String title;
        public String lastChapter;
        public String cover;
        public String curSource;
        public int curChapter; //position
        public int chapterCount;
        public List<String> allSource;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_LOCAL_BOOK;
        }
    }

    private static BookManager sInstance;

    public static BookManager get() {
        if (sInstance == null) {
            sInstance = new BookManager();
        }
        return sInstance;
    }

    private BookManager() {

    }

    public void init() {

    }

}
