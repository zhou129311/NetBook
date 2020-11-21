package com.xzhou.book.find;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.models.ThirdWebsiteHtmlParse;
import com.xzhou.book.net.OkHttpUtils;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-21
 * Change List:
 */
public class ThirdBookViewModel extends AndroidViewModel {

    public final MutableLiveData<Entities.ThirdBookDetail> mData = new MutableLiveData<>();
    private SearchModel.SearchBook mBook;

    public ThirdBookViewModel(@NonNull Application application) {
        super(application);
    }

    public void load(SearchModel.SearchBook book) {
        if (book == null) {
            return;
        }
        mBook = book;
        new Thread(() -> {
            Entities.ThirdBookDetail data = null;
            String html = OkHttpUtils.getPcRel(mBook.readUrl);
            if (html != null) {
                if (mBook.readUrl.contains("readnovel")) {
                    data = ThirdWebsiteHtmlParse.xsydwInfo(mBook.readUrl, html);
                } else if (mBook.readUrl.contains("xxsy")) {
                    data = ThirdWebsiteHtmlParse.xxsyInfo(mBook.readUrl, html);
                } else if (mBook.readUrl.contains("hongxiu")) {
                    data = ThirdWebsiteHtmlParse.hxtxInfo(mBook.readUrl, html);
                } else if (mBook.readUrl.contains("xs8")) {
                    data = ThirdWebsiteHtmlParse.hxtxInfo(mBook.readUrl, html);
                } else if (mBook.readUrl.contains("qidian")) {
                    data = ThirdWebsiteHtmlParse.qdzwwInfo(mBook.readUrl, html);
                }
            }
            mData.postValue(data);
        }).start();
    }

}
