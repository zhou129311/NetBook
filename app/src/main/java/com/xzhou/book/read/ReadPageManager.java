package com.xzhou.book.read;

public class ReadPageManager {

    private ReadPage mReadPage;
    private ReadCartoonPage mCartoonPage;

    public ReadPageManager() {

    }

    public void setReadPage(ReadPage page) {
        mReadPage = page;
    }

    public ReadPage getReadPage() {
        return mReadPage;
    }

    public void setReadCartoonPage(ReadCartoonPage page) {
        mCartoonPage = page;
    }

    public ReadCartoonPage getReadCartoonPage() {
        return mCartoonPage;
    }
}
