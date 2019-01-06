package com.xzhou.book;

import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xzhou.book.utils.Constant;

import java.util.List;

public class BookManager {

    public static class LocalBook implements MultiItemEntity, Parcelable {
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
        public boolean isBookshelf;

        public LocalBook() {

        }

        public LocalBook(Parcel in) {
            _id = in.readString();
            updated = in.readLong();
            readTime = in.readLong();
            title = in.readString();
            lastChapter = in.readString();
            cover = in.readString();
            curSource = in.readString();
            curChapter = in.readInt();
            chapterCount = in.readInt();
            allSource = in.createStringArrayList();
        }

        public static final Creator<LocalBook> CREATOR = new Creator<LocalBook>() {
            @Override
            public LocalBook createFromParcel(Parcel in) {
                return new LocalBook(in);
            }

            @Override
            public LocalBook[] newArray(int size) {
                return new LocalBook[size];
            }
        };

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_LOCAL_BOOK;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(_id);
            dest.writeLong(updated);
            dest.writeLong(readTime);
            dest.writeString(title);
            dest.writeString(lastChapter);
            dest.writeString(cover);
            dest.writeString(curSource);
            dest.writeInt(curChapter);
            dest.writeInt(chapterCount);
            dest.writeStringList(allSource);
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
