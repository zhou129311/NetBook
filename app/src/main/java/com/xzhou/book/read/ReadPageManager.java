package com.xzhou.book.read;

import android.view.View;

public class ReadPageManager {

    private ReadPage mReadPage;
    private View mPageEndView;

    public ReadPageManager() {

    }

    public void setReadPage(ReadPage page) {
        mReadPage = page;
    }

    public ReadPage getReadPage() {
        return mReadPage;
    }

    public View getPageView() {
        if (mReadPage.isPageEnd()) {
            return mPageEndView;
        }
        return mReadPage;
    }

    public void setPageEndView(View view) {
        mPageEndView = view;
    }

}
