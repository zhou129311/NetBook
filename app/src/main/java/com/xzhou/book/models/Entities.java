package com.xzhou.book.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.utils.Constant;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class Entities {
    public static class RankLv0 implements MultiItemEntity {
        public String name;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT;
        }
    }

    public static class RankLv1 extends AbstractExpandableItem<RankLv2> implements MultiItemEntity {
        public String _id;
        public String title;
        private String cover;
        public boolean collapse; // true 是否折叠
        public String monthRank;
        public String totalRank;
        String shortTitle;

        public String url() {
            return cover == null ? null : ZhuiShuSQApi.IMG_BASE_URL + cover;
        }

        public RankLv1(String title) {
            this.title = title;
        }

        RankLv1(RankLv1 ranking) {
            _id = ranking._id;
            title = ranking.title;
            cover = ranking.cover;
            collapse = ranking.collapse;
            monthRank = ranking.monthRank;
            totalRank = ranking.totalRank;
            shortTitle = ranking.shortTitle;
        }

        @Override
        public String toString() {
            return "RankLv1{" +
                    "_id='" + _id + '\'' +
                    ", title='" + title + '\'' +
                    ", cover='" + cover + '\'' +
                    ", collapse=" + collapse +
                    ", monthRank='" + monthRank + '\'' +
                    ", totalRank='" + totalRank + '\'' +
                    ", shortTitle='" + shortTitle + '\'' +
                    '}';
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT_IMAGE;
        }

        @Override
        public int getLevel() {
            return Constant.ITEM_TYPE_TEXT_IMAGE;
        }
    }

    public static class RankLv2 extends RankLv1 {

        public RankLv2(RankLv1 ranking) {
            super(ranking);
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT_IMAGE_2;
        }

        @Override
        public int getLevel() {
            return Constant.ITEM_TYPE_TEXT_IMAGE_2;
        }
    }

    public static class CategoryLv0 implements MultiItemEntity {
        public String title;

        public CategoryLv0(String title) {
            this.title = title;
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT;
        }
    }

    public static class CategoryLv1 extends CategoryLv0 {
        public int bookCount;
        public String gender;
        public List<String> minors;

        @Override
        public String toString() {
            return "CategoryLv1{" +
                    "title='" + title + '\'' +
                    ", bookCount=" + bookCount +
                    ", gender='" + gender + '\'' +
                    '}';
        }

        public CategoryLv1(CategoryList.Category category, String gender, List<String> minors) {
            super(category.name);
            bookCount = category.bookCount;
            this.gender = gender;
            this.minors = minors;
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT_GRID;
        }
    }

    public static class ImageText {
        public String name;
        public @IdRes
        int resId;

        public ImageText(String name, int resId) {
            this.name = name;
            this.resId = resId;
        }
    }

    public static class TabData implements Parcelable {
        public String title;
        public @Constant.TabSource
        int source;
        public String[] params;
        public String[] filtrate;

        public TabData() {
        }

        TabData(Parcel in) {
            title = in.readString();
            source = in.readInt();
            int length = in.readInt();
            if (length > 0) {
                params = new String[length];
                in.readStringArray(params);
            }
            length = in.readInt();
            if (length > 0) {
                filtrate = new String[length];
                in.readStringArray(filtrate);
            }
        }

        public static final Creator<TabData> CREATOR = new Creator<TabData>() {
            @Override
            public TabData createFromParcel(Parcel in) {
                return new TabData(in);
            }

            @Override
            public TabData[] newArray(int size) {
                return new TabData[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeInt(source);
            if (params == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(params.length);
                dest.writeStringArray(params);
            }
            if (filtrate == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(filtrate.length);
                dest.writeStringArray(filtrate);
            }
        }

        @Override
        public String toString() {
            return "TabData{" +
                    "title='" + title + '\'' +
                    ", source=" + source +
                    ", params='" + Arrays.toString(params) + '\'' +
                    ", filtrate='" + Arrays.toString(filtrate) + '\'' +
                    '}';
        }
    }

    public static class HttpResult<T> {
        public boolean ok;
        public T data;
    }

    public static class NetBook implements MultiItemEntity {
        public String _id;
        public String title;
        public String author;
        public String shortIntro = "";
        public String cover;
        public String site;
        public String cat;
        public int banned;
        public int latelyFollowerBase;
        public String minRetentionRatio;

        public String lastChapter;
        public String updated; //最近更新时间
        public String contentType;
        public int latelyFollower; //最近阅读人数
        public double retentionRatio; //留存率 73.76
        public int chaptersCount;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_NET_BOOK;
        }

        public String cover() {
            return cover == null ? null : ZhuiShuSQApi.IMG_BASE_URL + cover;
        }
    }

    public static class BookSource {
        public static final Type TYPE = new TypeToken<List<BookSource>>() {
        }.getType();

        public String _id;
        public String lastChapter;
        public String link;
        public String source;
        public String name;
        public boolean isCharge;
        public int chaptersCount;
        public String updated;
        public boolean starting;
        public String host;
    }

    public static class Recommend {
        public static final Type TYPE = new TypeToken<Recommend>() {
        }.getType();

        public List<NetBook> books;

    }

    public static class BookMixAToc {
        public static final Type TYPE = new TypeToken<HttpResult<BookMixAToc>>() {
        }.getType();

        public MixToc mixToc;

        public static class MixToc {
            public String _id;
            public String book;
            public String chaptersUpdated;
            public int chaptersCount1;

            public List<Chapters> chapters;

            public static class Chapters {
                public String title;
                public String link;
                public String id;
                public int currency;
                public boolean unreadble;
                public boolean isVip;

                public Chapters(String title, String link) {
                    this.title = title;
                    this.link = link;
                }
            }
        }
    }

    public static class ChapterRead {
        public static final Type TYPE = new TypeToken<HttpResult<ChapterRead>>() {
        }.getType();
        public Chapter chapter;

        public static class Chapter {
            public String title;
            public String body;
            public String cpContent;

            public Chapter(String title, String body) {
                this.title = title;
                this.body = body;
            }

            public Chapter(String title, String body, String cpContent) {
                this.title = title;
                this.body = body;
                this.cpContent = cpContent;
            }
        }
    }

    public static class HotWord {
        public static final Type TYPE = new TypeToken<HttpResult<HotWord>>() {
        }.getType();

        public List<String> hotWords;
    }

    public static class AutoComplete {
        public static final Type TYPE = new TypeToken<HttpResult<AutoComplete>>() {
        }.getType();

        public List<String> keywords;
    }

    public static class SearchResult {
        public static final Type TYPE = new TypeToken<HttpResult<SearchResult>>() {
        }.getType();

        public List<SearchBook> books;

        public static class SearchBook {
            public String _id;
            public boolean hasCp;
            public String title;
            public String aliases;
            public String cat;
            public String author;
            public String site; //书源
            public String cover;
            public String shortIntro;
            public String lastChapter;
            public String retentionRatio; //留存率 73.76
            public String contentType; //txt
            public String superscript;
            public int banned;
            public int latelyFollower; //22259 人在追
            public int wordCount; //字数
        }
    }

    public static class BooksByTag {
        public static final Type TYPE = new TypeToken<BooksByTag>() {
        }.getType();

        public List<TagBook> books;

        public static class TagBook extends NetBook {
            public String majorCate;
            public String minorCate;
            public List<String> tags;

            @Override
            public int getItemType() {
                return tags == null ? Constant.ITEM_TYPE_BOOK_BY_AUTHOR : Constant.ITEM_TYPE_BOOK_BY_TAG;
            }
        }
    }

    public static class HotReview {
        public static final Type TYPE = new TypeToken<HotReview>() {
        }.getType();
        public List<Reviews> reviews;
    }

    public static class Reviews implements MultiItemEntity {
        public String _id;
        public int rating;
        public String content;
        public String title;

        public Author author;
        public Helpful helpful;
        public int likeCount;
        public String state;
        public String updated;
        public String created;
        public int commentCount;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_REVIEWS;
        }

        public String avatar() {
            return ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
        }

        public static class Author {
            public String _id;
            public String avatar;
            public String nickname;
            public String type;
            public int lv;
            public String gender;
        }

        public static class Helpful {
            public int yes;
            public int total;
            public int no;
        }
    }

    public static class RecommendBookList {
        public static final Type TYPE = new TypeToken<RecommendBookList>() {
        }.getType();

        public List<RecommendBook> booklists;
    }

    public static class RecommendBook implements MultiItemEntity {
        public String id;
        public String title;
        public String author;
        public String desc;
        public int bookCount;
        public String cover;
        public int collectorCount;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_NET_BOOK_LIST;
        }
    }

    public static class BookDetail {
        public static final Type TYPE = new TypeToken<BookDetail>() {
        }.getType();

        public String _id;
        public String author;
        public int banned;
        public String cover;
        public String creater;
        public Object dramaPoint;
        public int followerCount;
        public int gradeCount;
        public boolean isSerial; //连载 or 完结
        public String lastChapter;
        public int latelyFollower;
        public String longIntro;
        public int postCount; // 社区帖子数
        public int serializeWordCount;
        public String site;
        public String title;
        public Object totalPoint;
        public String type;
        public String updated;
        public Object writingPoint;
        public boolean hasNotice;
        public int tagStuck;
        public int chaptersCount;
        public int tocCount;
        public String tocUpdated;
        public String retentionRatio;
        public boolean hasCmread;
        public String thirdFlagsUpdated;
        public int wordCount;
        public String cat;
        public String majorCate;
        public String minorCate;
        public int reviewCount;
        public int totalFollower;
        public boolean cpOnly;
        public boolean hasCp;
        public boolean _le;
        public List<String> tags;
        public List<String> tocs;
        public List<String> categories;
        public Object gender; // MLGB, 偶尔是String，偶尔是Array

        // me add
        public boolean isSaveBookshelf;

        public String cover() {
            return ZhuiShuSQApi.IMG_BASE_URL + cover;
        }
    }

    public static class RankingList {
        public static final Type TYPE = new TypeToken<RankingList>() {
        }.getType();

        public List<RankLv1> female;
        public List<RankLv1> male;
        public List<RankLv1> picture;
        public List<RankLv1> epub;
    }

    public static class Rankings {
        public static final Type TYPE = new TypeToken<Rankings>() {
        }.getType();

        public RankingBean ranking;

        public static class RankingBean {
            public String _id;
            public String updated;
            public String title;
            public String tag;
            public String cover;
            public int __v;
            public String monthRank;
            public String totalRank;
            public boolean isSub;
            public boolean collapse;
            @SerializedName("new")
            public boolean newX;
            public String gender;
            public int priority;
            public String created;
            public String id;
            public List<NetBook> books;
        }
    }

    public static class BookLists {
        public static final Type TYPE = new TypeToken<HttpResult<BookLists>>() {
        }.getType();

        public List<BookListsBean> bookLists;

        public class BookListsBean {
            public String _id;
            public String title;
            public String author;
            public String desc;
            public String gender;
            public int collectorCount;
            public String cover;
            public int bookCount;
        }
    }

    public static class BookListTags {
        public static final Type TYPE = new TypeToken<HttpResult<List<BookListTags>>>() {
        }.getType();

        public String name;
        public List<String> tags;
    }

    public static class BookListDetail {
        public static final Type TYPE = new TypeToken<HttpResult<BookListDetail>>() {
        }.getType();

        public BookListBean bookList;

        public static class BookListBean {
            public String _id;
            public String updated;
            public String title;
            public AuthorBean author;
            public String desc;
            public String gender;
            public String created;
            public Object stickStopTime;
            public boolean isDraft;
            public Object isDistillate;
            public int collectorCount;
            public String shareLink;
            public String id;
            public List<String> tags;
            public List<BooksBean> books;
        }

        public static class AuthorBean {
            public String _id;
            public String avatar;
            public String nickname;
            public String type;
            public int lv;
        }

        public static class BooksBean {
            public BookBean book;
            public String comment;

            public static class BookBean {
                public String _id;
                public String title;
                public String author;
                public String longIntro;
                public String cover;
                public String site;
                public int banned;
                public int latelyFollower;
                public int latelyFollowerBase;
                public int wordCount;
                public String minRetentionRatio;
                public double retentionRatio;
            }
        }
    }

    public static class CategoryList {
        public static final Type TYPE = new TypeToken<CategoryList>() {
        }.getType();

        public List<Category> male; //男生
        public List<Category> female; //女生
        public List<Category> picture; //漫画
        public List<Category> press; //出版

        public static class Category {
            public String name;
            public int bookCount;
            public List<String> minors;
        }
    }

    public static class CategoryListLv2 {
        public static final Type TYPE = new TypeToken<CategoryListLv2>() {
        }.getType();

        public List<Category> male;
        public List<Category> female;
        public List<Category> picture; //漫画
        public List<Category> press; //出版

        public static class Category {
            public String major;
            public List<String> mins;
        }
    }

    public static class BooksByCats {
        public static final Type TYPE = new TypeToken<BooksByCats>() {
        }.getType();
        public List<CatBook> books;

        public static class CatBook extends NetBook {
            public String majorCate;
            public List<String> tags;
        }
    }

    public static class DiscussionList {
        public static final Type TYPE = new TypeToken<HttpResult<DiscussionList>>() {
        }.getType();
        public List<PostsBean> posts;

        public static class PostsBean {
            public String _id;
            public String title;
            public String type;
            public int likeCount;
            public String block;
            public String state;
            public String updated;
            public String created;
            public int commentCount;
            public int voteCount;
            public AuthorBean author;

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
            }
        }
    }

    public static class Disscussion {
        public static final Type TYPE = new TypeToken<HttpResult<Disscussion>>() {
        }.getType();
        public PostBean post;

        public static class PostBean {
            public String _id;
            public String title;
            public String content;
            public AuthorBean author;
            public String type;
            public boolean isStopPriority;
            public boolean deleted;
            public int likeCount;
            public boolean isStick;
            public String block;
            public String state;
            public String updated;
            public String created;
            public int commentCount;
            public int voteCount;
            public String shareLink;
            public String id;
            public List<?> votes;

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
                public Object rank;
                public String created;
                public String id;
            }
        }
    }

    public static class CommentList {
        public static final Type TYPE = new TypeToken<HttpResult<CommentList>>() {
        }.getType();
        public List<CommentsBean> comments;

        public static class CommentsBean {
            public String _id;
            public String content;
            public AuthorBean author;
            public int floor;
            public int likeCount;
            public String created;
            public ReplyToBean replyTo;

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
            }

            public static class ReplyToBean {
                public String _id;
                public int floor;
                public AuthorBean author;

                public static class AuthorBean {
                    public String _id;
                    public String nickname;
                }
            }
        }
    }

    public static class BookReviewList {
        public static final Type TYPE = new TypeToken<HttpResult<BookReviewList>>() {
        }.getType();
        public List<ReviewsBean> reviews;

        public static class ReviewsBean {
            public String _id;
            public String title;
            public BookBean book;
            public HelpfulBean helpful;
            public int likeCount;
            public String state;
            public String updated;
            public String created;

            public static class BookBean {
                public String _id;
                public String cover;
                public String title;
                public String site;
                public String type;
            }

            public static class HelpfulBean {
                public int total;
                public int no;
                public int yes;
            }
        }
    }

    public static class BookReview {
        public static final Type TYPE = new TypeToken<HttpResult<BookReview>>() {
        }.getType();
        public ReviewBean review;

        public static class ReviewBean {
            public String _id;
            public int rating;
            public String content;
            public String title;
            public String type;
            public BookBean book;
            public AuthorBean author;
            public HelpfulBean helpful;
            public String state;
            public String updated;
            public String created;
            public int commentCount;
            public String shareLink;
            public String id;

            public static class BookBean {
                public String _id;
                public String cover;
                public String title;
                public String id;
            }

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
                public Object rank;
                public String created;
                public String id;
            }

            public static class HelpfulBean {
                public int total;
                public int yes;
                public int no;
            }
        }
    }

    public static class BookHelpList {
        public static final Type TYPE = new TypeToken<HttpResult<BookHelpList>>() {
        }.getType();
        public List<HelpsBean> helps;

        public static class HelpsBean {
            public String _id;
            public String title;
            public AuthorBean author;
            public int likeCount;
            public String state;
            public String updated;
            public String created;
            public int commentCount;

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
            }
        }
    }

    public static class BookHelp {
        public static final Type TYPE = new TypeToken<HttpResult<BookHelp>>() {
        }.getType();
        public HelpBean help;

        public static class HelpBean {
            public String _id;
            public String type;
            public AuthorBean author;
            public String title;
            public String content;
            public String state;
            public String updated;
            public String created;
            public int commentCount;
            public String shareLink;
            public String id;

            public static class AuthorBean {
                public String _id;
                public String avatar;
                public String nickname;
                public String type;
                public int lv;
                public String gender;
                public Object rank;
                public String created;
                public String id;
            }
        }
    }

    public static class Login {
        public static final Type TYPE = new TypeToken<HttpResult<Login>>() {
        }.getType();
        public UserBean user;
        public String token;

        public static class UserBean {
            public String _id;
            public String nickname;
            public String avatar;
            public int exp;
            public int lv;
            public String gender;
            public String type;
        }
    }
}
