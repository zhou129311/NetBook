package com.xzhou.book;

public class BookManager {

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
