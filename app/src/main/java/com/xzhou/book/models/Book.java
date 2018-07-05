package com.xzhou.book.models;

import java.io.Serializable;

public class Book implements Serializable {

    public String id;
    public String author;
    public String cover;
    public String shortIntro;
    public String title;
    public boolean hasCp;
    public boolean isTop = false;
    public boolean isSeleted = false;
    public boolean showCheckBox = false;
    public boolean isFromSD = false;
    public String path = "";
    public int latelyFollower;
    public double retentionRatio;
    public String updated = "";
    public int chaptersCount;
    public String lastChapter;
    public String recentReadingTime = "";

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Book) {
            Book bean = (Book) obj;
            return this.id.equals(bean.id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
