package com.xzhou.book.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.utils.Constant;

import java.util.HashMap;

public class BaiduEntities {
    public static final int PARSE_BQG = 0;
    public static final int PARSE_LWTX = 1;
    public static final int PARSE_GGD = 2;
    public static final int PARSE_BLXS = 3;

    public static final HashMap<String, Integer> BOOK_HOSTS = new HashMap<String, Integer>() {
        {
            put("www.tianxiabachang.cn", PARSE_BQG);
            put("wap.x4399.com", PARSE_BQG);
            put("www.x4399.com", PARSE_BQG);
            put("www.lwtxt.cc", PARSE_LWTX);
            put("www.oldtimes.cc", PARSE_LWTX);
            put("m.ggdown.org", PARSE_GGD);
            put("www.ggdown.org", PARSE_GGD);
            put("m.boluoxs.com", PARSE_BLXS);
            put("www.boluoxs.com", PARSE_BLXS);
        }
    };

    public static class BaiduBook implements MultiItemEntity, Parcelable {
        public String image;
        public String sourceName;
        public String sourceHost;
        public String mobReadUrl;
        public String author;
        public String bookName;
        public String readUrl;
        public String latestChapterName;
        public String latestChapterUrl;
        public String id;

        public BaiduBook() {
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_BAIDU_BOOK;
        }

        public boolean hasValid() {
            boolean valid = !TextUtils.isEmpty(readUrl) && !TextUtils.isEmpty(bookName);
            if (valid) {
                id = String.valueOf((bookName + ":" + readUrl).hashCode());
            }
            return valid;
        }

        private BaiduBook(Parcel in) {
            image = in.readString();
            sourceName = in.readString();
            sourceHost = in.readString();
            mobReadUrl = in.readString();
            author = in.readString();
            bookName = in.readString();
            readUrl = in.readString();
            latestChapterName = in.readString();
            latestChapterUrl = in.readString();
            id = in.readString();
        }

        @Override
        public String toString() {
            return "BaiduBook{" +
                    "id='" + id + '\'' +
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

        public static final Creator<BaiduBook> CREATOR = new Creator<BaiduBook>() {
            @Override
            public BaiduBook createFromParcel(Parcel in) {
                return new BaiduBook(in);
            }

            @Override
            public BaiduBook[] newArray(int size) {
                return new BaiduBook[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(image);
            dest.writeString(sourceName);
            dest.writeString(sourceHost);
            dest.writeString(mobReadUrl);
            dest.writeString(author);
            dest.writeString(bookName);
            dest.writeString(readUrl);
            dest.writeString(latestChapterName);
            dest.writeString(latestChapterUrl);
            dest.writeString(id);
        }
    }

}
