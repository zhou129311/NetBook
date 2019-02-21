package com.xzhou.book.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.xzhou.book.R;
import com.xzhou.book.community.PostsDetailActivity;
import com.xzhou.book.datasource.ZhuiShuSQApi;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

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
        public @DrawableRes
        int resId;

        public ImageText(String name, int resId) {
            this.name = name;
            this.resId = resId;
        }
    }

    public static class TabData implements Parcelable {
        public String title; //标题
        public @Constant.TabSource
        int source; //来源
        public String[] params; //参数
        public String[] filtrate; //筛选
        public int curFiltrate = -1; //当前筛选

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
            curFiltrate = in.readInt();
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
            dest.writeInt(curFiltrate);
        }

        @Override
        public String toString() {
            return "TabData{" +
                    "title='" + title + '\'' +
                    ", source=" + source +
                    ", params='" + Arrays.toString(params) + '\'' +
                    ", filtrate='" + Arrays.toString(filtrate) + '\'' +
                    ", curFiltrate='" + curFiltrate + '\'' +
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
        private String cover;
        public String site; // vip.zhuishu
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

        public boolean isPicture() {
            return "picture".equals(contentType);
        }
    }

    public static class BookSource {
        public static final Type TYPE = new TypeToken<List<BookSource>>() {
        }.getType();

        public String _id;//"5ae1820df5f2af0100f45a3a"
        public String lastChapter;
        public String link;// "http://book.my716.com/getBooks.aspx?method\u003dchapterList\u0026bookId\u003d2292476"
        public String source;// "my176"
        public String name;//176小说
        public boolean isCharge;
        public int chaptersCount;
        public String updated;
        public boolean starting;
        public String host;//"book.my716.com"
    }

    public static class Recommend {
        public static final Type TYPE = new TypeToken<Recommend>() {
        }.getType();

        public List<NetBook> books;

    }

    public static class BookMixAToc {
        public static final Type TYPE = new TypeToken<BookMixAToc>() {
        }.getType();

        public BookAToc mixToc;
    }

    public static class BookAToc {
        public static final Type TYPE = new TypeToken<BookAToc>() {
        }.getType();

        public String _id; //sourceId
        public String book;// bookId
        public String link; // "http://book.my716.com/getBooks.aspx?method=chapterList&bookId=2292476"
        public String source;//my176
        public String name;//176小说
        public String host;//book.my716.com
        public String chaptersUpdated;
        public String updated;
        public List<Chapters> chapters;
    }

    public static class Chapters {
        public static final Type TYPE = new TypeToken<List<Chapters>>() {
        }.getType();

        public String title;
        public String link;
        public String id;
        public int currency;
        public boolean unreadble;
        public boolean isVip;
        public boolean hasLocal;

        public Chapters(String title, String link) {
            this.title = title;
            this.link = link;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Chapters) {
                return TextUtils.equals(((Chapters) obj).link, link);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return link.hashCode();
        }
    }

    public static class ChapterRead {
        public static final Type TYPE = new TypeToken<ChapterRead>() {
        }.getType();
        public Chapter chapter;
    }

    public static class Chapter {
        //文字
        public String title;
        public String body;
        //漫画
        public String id; //595481552e554d6604a61976
        public boolean isVip; // false
        public int currency; // 29
        public String images; // "http://cartoon.zhuishushenqi.com/api/v1/bookcenter/scenethumbnail/1/181576/U_a817e0cf-3e14-48f2-90e9-f2ecbf4de0a4.JPG"
        public String imageScale; // "1.41,1.41,1.41"
        public String created;
        public String updated;

        public List<String> getImages() {
            if (TextUtils.isEmpty(images)) {
                return null;
            }
            String[] list = images.split(",");
            return Arrays.asList(list);
        }

        public List<String> getImageScales() {
            if (TextUtils.isEmpty(imageScale)) {
                return null;
            }
            String[] list = imageScale.split(",");
            return Arrays.asList(list);
        }
    }

    public static class HotWord {
        public static final Type TYPE = new TypeToken<HotWord>() {
        }.getType();

        public List<String> hotWords;
    }

    public static class AutoSuggest {
        public static final Type TYPE = new TypeToken<AutoSuggest>() {
        }.getType();

        public List<Suggest> keywords;
    }

    /**
     * "text": "妖神记",
     * "tag": "bookname", tag bookauthor
     * "id": "55aed62b1aa7a382698aeae4",
     * "author": "发飙的蜗牛",
     * "contentType": "txt" "picture"
     */
    public static class Suggest {
        public String text;
        public String tag;
        public String id;
        public String author;
        public String contentType;
        public String gender;
        public String major;
        public String minor;
        public List<String> minors;

        public boolean isPicture() {
            return "picture".equals(contentType);
        }

        public boolean isTag() {
            return "tag".equals(tag);
        }

        public boolean isAuthor() {
            return "bookauthor".equals(tag);
        }

        public boolean isBook() {
            return "bookname".equals(tag);
        }

        public boolean isCat() {
            return "cat".equals(tag);
        }

        public int getImgRes() {
            int res;
            if (isTag()) {
                res = R.mipmap.ic_search_result_tag;
            } else if (isCat()) {
                res = R.mipmap.ic_search_result_cat;
            } else if (isAuthor()) {
                res = R.mipmap.ic_search_result_aut;
            } else {
                res = R.mipmap.ic_search_result_book;
            }
            return res;
        }
    }

    public static class AutoComplete {
        public static final Type TYPE = new TypeToken<AutoComplete>() {
        }.getType();

        public List<String> keywords;
    }

    public static class SearchResult {
        public static final Type TYPE = new TypeToken<SearchResult>() {
        }.getType();

        public List<SearchBook> books;
    }

    public static class SearchBook extends NetBook {
        public boolean hasCp;
        public String aliases;
        public String superscript;
        public int wordCount; //字数

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_BOOK_BY_SEARCH;
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

        private Author author;
        private Helpful helpful;
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
            return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
        }

        public String nickname() {
            return author == null ? "" : author.nickname;
        }

        public int lv() {
            return author == null ? 0 : author.lv;
        }

        public int yes() {
            return helpful == null ? 0 : helpful.yes;
        }
    }

    public static class RecommendBookList {
        public static final Type TYPE = new TypeToken<RecommendBookList>() {
        }.getType();

        public List<BookListBean> booklists;
    }

    public static class BookDetail {
        public static final Type TYPE = new TypeToken<BookDetail>() {
        }.getType();

        public String _id;
        public String author;
        public int banned;
        private String cover;
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
        public String contentType; // picture 漫画
        public int reviewCount;
        public int totalFollower;
        public boolean cpOnly;
        public boolean hasCp;
        public boolean _le;
        public List<String> tags;
        public List<String> tocs;
        public List<String> categories;
        public Object gender; // MLGB, 偶尔是String，偶尔是Array

        public String cover() {
            return ZhuiShuSQApi.IMG_BASE_URL + cover;
        }

        public boolean isPicture() {
            return "picture".equals(contentType);
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

    public static class SearchBookList {
        public static final Type TYPE = new TypeToken<SearchBookList>() {
        }.getType();

        public String ok;
        public int total;
        public List<UgcBookList> ugcbooklists;

        public static class UgcBookList {
            public String _id;
            public Author author;
            public String desc;
            public String title;
            public int bookCount;
            public int collectorCount;
            public String cover;
            public List<String> covers;
            public String gender;
            public boolean isDistillate; //精品书单
            public String created;
            public String updated;
            public Highlight highlight;
        }
    }

    public static class BookLists {
        public static final Type TYPE = new TypeToken<BookLists>() {
        }.getType();

        public List<BookListBean> bookLists;
    }

    //书单列表
    public class BookListBean implements MultiItemEntity {
        private String _id;
        private String id;
        public String title;
        public String author;
        public String desc;
        public String gender;
        public int collectorCount;
        private String cover;
        public int bookCount;

        public String id() {
            return AppUtils.isEmpty(_id) ? id : _id;
        }

        public String cover() {
            return cover == null ? null : ZhuiShuSQApi.IMG_BASE_URL + cover;
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_NET_BOOK_LIST;
        }
    }

    public static class BookListTags {
        public static final Type TYPE = new TypeToken<List<BookListTags>>() {
        }.getType();

        public String name;
        public List<String> tags;
    }

    public static class BookListDetail {
        public static final Type TYPE = new TypeToken<BookListDetail>() {
        }.getType();

        public BookList bookList;

        public static class BookList {
            public String _id;
            public String updated;
            public String title;
            private Author author;
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

            public String avatar() {
                return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
            }

            public String nickname() {
                return author == null ? "" : author.nickname;
            }
        }

        public static class BooksBean {
            public BookBean book;
            public String comment;

            public static class BookBean {
                public String _id;
                public String title;
                public String author;
                public String longIntro;
                private String cover;
                public String site;
                public int banned;
                public int latelyFollower;
                public int latelyFollowerBase;
                public int wordCount;
                public String minRetentionRatio;
                public double retentionRatio;

                public String cover() {
                    return ZhuiShuSQApi.IMG_BASE_URL + cover;
                }
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

    //综合讨论区帖子列表
    public static class DiscussionList {
        public static final Type TYPE = new TypeToken<DiscussionList>() {
        }.getType();
        public List<Posts> posts;
    }

    public static class Posts implements MultiItemEntity {
        public String _id;
        public String title;
        public String type; //type=vote 投票  review 书评
        public String book; //bookId
        public int likeCount; //赞同数
        public String block; // ramble original review
        public String state; //state=hot focus 热门  distillate 精品 normal 普通
        public String updated;
        public String created;
        public int commentCount; //评论人数
        public int voteCount; // 投票人数
        public boolean haveImage; //内容是否包含图片
        private Author author;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_DISCUSSION;
        }

        public String avatar() {
            return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
        }

        public String authorId() {
            return author == null ? "" : author._id;
        }

        public String nickname() {
            return author == null ? "" : author.nickname;
        }

        public boolean isVote() {
            return "vote".equals(type);
        }

        public boolean isReview() {
            return "review".equals(type) && !TextUtils.isEmpty(book);
        }

        public boolean isHot() {
            return "hot".equals(state) || "focus".equals(state);
        }

        public boolean isDistillate() {
            return "distillate".equals(state);
        }

        public boolean isOfficial() {
            return author != null && "official".equals(author.type);
        }

        public boolean isDoyen() {
            return author != null && "doyen".equals(author.type);
        }

        public int authorLv() {
            return author == null ? 1 : author.lv;
        }

        public String gender() {
            return author == null ? "" : author.gender;
        }

        @Override
        public String toString() {
            return "Posts{" +
                    "title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", state='" + state + '\'' +
                    ", author=" + author +
                    '}';
        }
    }

    //综合讨论区帖子详情
    public static class DiscussionDetail {
        public static final Type TYPE = new TypeToken<DiscussionDetail>() {
        }.getType();
        public PostDetail post;

        public static class PostDetail {
            public String _id;
            public String title;
            public String content;
            private Author author;
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
            public List<Vote> votes;

            public String nickname() {
                return author == null ? "" : author.nickname;
            }

            public boolean isVote() {
                return "vote".equals(type);
            }

            public boolean isHot() {
                return "hot".equals(state) || "focus".equals(state);
            }

            public boolean isOfficial() {
                return author != null && "official".equals(author.type);
            }

            public boolean isDoyen() {
                return author != null && "doyen".equals(author.type);
            }

            public int lv() {
                return author == null ? 1 : author.lv;
            }

            public String avatar() {
                return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
            }

            static class Author {
                private String _id;
                private String avatar;
                private String nickname;
                private String type;
                private int lv;
                private String gender;
                private Object rank;
                private String created;
                private String id;
            }

            public static class Vote implements MultiItemEntity {
                public String content;
                public int count;
                public String id;
                public @DrawableRes
                int itemNumberRes;

                @Override
                public int getItemType() {
                    return Constant.ITEM_TYPE_VOTE;
                }
            }
        }
    }

    //评论
    public static class CommentList {
        public static final Type TYPE = new TypeToken<CommentList>() {
        }.getType();
        public List<Comment> comments;
    }

    public static class Comment implements MultiItemEntity {
        public String _id;
        public String content;
        private Author author;
        public int floor; //楼层
        public int likeCount; //同感
        public String created;
        private ReplyTo replyTo;
        private ReplyTo replyAuthor;

        public boolean isBest; //是否是神评论

        public String avatar() {
            return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
        }

        public int lv() {
            return author == null ? 0 : author.lv;
        }

        public String nickname() {
            return author == null ? "" : author.nickname;
        }

        public String replayTo() {
            return replyTo == null ? null : AppUtils.getString(R.string.comment_reply_to, replyTo.nickname(), replyTo.floor);
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_COMMENT;
        }

        static class ReplyTo {
            private String _id;
            private int floor;//楼层
            private Author author; //回复XXX

            public String nickname() {
                return author == null ? "" : author.nickname;
            }

            static class Author {
                private String _id;
                private String nickname;
            }
        }
    }

    public static class PostSection implements MultiItemEntity {
        public String text;

        public PostSection(String text) {
            this.text = text;
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT;
        }
    }

    public static class Helpful implements MultiItemEntity {
        public int total;
        public int no;
        public int yes;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_HELPFUL;
        }
    }

    //书评区列表
    public static class PostReviewList {
        public static final Type TYPE = new TypeToken<PostReviewList>() {
        }.getType();
        public List<PostsReviews> reviews;
    }

    public static class PostsReviews implements MultiItemEntity {
        public String _id;
        public String title;
        private Book book;
        private Helpful helpful;
        public int likeCount;
        public String state;
        public String updated;
        public String created;

        public boolean isHot() {
            return "hot".equals(state) || "focus".equals(state);
        }

        public boolean isDistillate() {
            return "distillate".equals(state);
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_POSTS_REVIEW;
        }

        public String cover() {
            return book == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + book.cover;
        }

        public String bookType() {
            return book == null ? "" : book.type;
        }

        public String bookTitle() {
            return book == null ? "" : book.title;
        }

        public int helpfulYes() {
            return helpful == null ? 0 : helpful.yes;
        }

        static class Book {
            private String _id;
            private String cover;
            private String title;
            private String site;
            private String type;
        }
    }

    public static class ReviewDetail {
        public static final Type TYPE = new TypeToken<ReviewDetail>() {
        }.getType();
        public ReviewDetailHeader review;
    }

    public static class ReviewDetailHeader {
        public String _id;
        public int rating;
        public String content;
        public String title;
        public String type; //review
        private ReviewBook book;
        private ReviewAuthor author;
        public Helpful helpful;
        public String state; //normal
        public String updated;
        public String created;
        public int commentCount;
        public String shareLink;
        public String id;

        public String cover() {
            return book == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + book.cover;
        }

        public String bookId() {
            return book == null ? "" : (book._id == null ? "" : book.id);
        }

        public String bookTitle() {
            return book == null ? "" : book.title;
        }

        public String avatar() {
            return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
        }

        public String nickname() {
            return author == null ? "" : author.nickname;
        }

        public int lv() {
            return author == null ? 1 : author.lv;
        }

        public int yes() {
            return helpful == null ? 0 : helpful.yes;
        }

        public int no() {
            return helpful == null ? 0 : helpful.no;
        }

        public boolean isOfficial() {
            return author != null && "official".equals(author.type);
        }

        public boolean isDoyen() {
            return author != null && "doyen".equals(author.type);
        }

        static class ReviewBook {
            private String _id;
            private String cover;
            private String author;
            private String title;
            private String id;
        }

        static class ReviewAuthor {
            public String _id;
            private String avatar;
            private String nickname;
            public String type;
            private int lv;
            public String gender;
            public Object rank;
            public String created;
            public String id;

            @Override
            public String toString() {
                return "ReviewAuthor{" +
                        "nickname='" + nickname + '\'' +
                        ", type='" + type + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "ReviewDetailHeader{" +
                    "type='" + type + '\'' +
                    ", book=" + book +
                    ", author=" + author +
                    ", state='" + state + '\'' +
                    '}';
        }
    }

    public static class BookHelpList {
        public static final Type TYPE = new TypeToken<BookHelpList>() {
        }.getType();
        public List<HelpsBean> helps;

        public static class HelpsBean implements MultiItemEntity {
            public String _id;
            public String title;
            private Author author;
            public int likeCount;
            public String state;
            public String updated;
            public String created;
            public int commentCount;

            public String avatar() {
                return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
            }

            public String nickname() {
                return author == null ? "" : author.nickname;
            }

            public int lv() {
                return author == null ? 1 : author.lv;
            }

            public boolean isOfficial() {
                return author != null && "official".equals(author.type);
            }

            public boolean isDoyen() {
                return author != null && "doyen".equals(author.type);
            }

            public boolean isHot() {
                return "hot".equals(state) || "focus".equals(state);
            }

            public boolean isDistillate() {
                return "distillate".equals(state);
            }

            @Override
            public int getItemType() {
                return Constant.ITEM_TYPE_POSTS_HELP;
            }
        }
    }

    public static class BookHelp {
        public static final Type TYPE = new TypeToken<BookHelp>() {
        }.getType();
        public HelpDetail help;

        public static class HelpDetail {
            public String _id;
            public String type;
            private Author author;
            public String title;
            public String content;
            public String state;
            public String updated;
            public String created;
            public int commentCount;
            public String shareLink;
            public String id;

            public String avatar() {
                return author == null ? "" : ZhuiShuSQApi.IMG_BASE_URL + author.avatar;
            }

            public String nickname() {
                return author == null ? "" : author.nickname;
            }

            public int lv() {
                return author == null ? 1 : author.lv;
            }

            public boolean isOfficial() {
                return author != null && "official".equals(author.type);
            }

            public boolean isDoyen() {
                return author != null && "doyen".equals(author.type);
            }

            static class Author {
                private String _id;
                private String avatar;
                private String nickname;
                private String type;
                private int lv;
                private String gender;
                private Object rank;
                private String created;
                private String id;
            }
        }
    }

    public static class Login {
        public static final Type TYPE = new TypeToken<Login>() {
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

    public static class Updated {
        public static final Type TYPE = new TypeToken<List<Updated>>() {
        }.getType();

        public String _id;
        public String author;
        public boolean allowMonthly;
        public String referenceSource;
        public String updated;
        public String chaptersCount;
        public String lastChapter;
    }

    public static class HelpSearchResult {
        public static final Type TYPE = new TypeToken<HelpSearchResult>() {
        }.getType();
        public String next;//MTA=
        public List<Question> questions;
    }

    public static class Question {
        public String id; // 5a700da3c0f1873406c20041
        public String title; //找书找书找书?
        public String desc; //只记得刚开始穿越到战狼2里面，好想还有就是杀了谁可以获得那个人的一项能力
        public List<String> tags;//59c0c48d027094c1468c4cb7
        public List<Tag> tagList;
        public Author author;
        public String created;
        public int answerCount; //21
        public int followCount; //3
        public boolean isFollow; //false
        public Answer bestAnswer;
        public String shareLink;
        public String shareIcon;
        public int readCount; //1129
        public Highlight highlight;

        public static class Tag {
            public String id; // 59c0c48d027094c1468c4cb7
            public String name; //男频
            public int order; //0
        }
    }

    public static class Highlight {
        public List<String> title;
    }

    public static class Author {
        public String _id;
        public String avatar;
        public String nickname;
        public String monthly;
        private String type;//type=official 官方  type=normal 普通  type=commentator 评论员 doyen 首席
        public int lv;
        public String gender;
    }

    /**
     * {
     * "id": "5a701c2cc0f1873406c2008c",
     * "question": "5a700da3c0f1873406c20041",
     * "title": "",
     * "content": "{{type:book,id:56ce9cdb363f92a007273896,title:战狼2：国家利刃,author:飞永,cover:/agent/http%3A%2F%2Fimg.1391.com%2Fapi%2Fv1%2Fbookcenter%2Fcover%2F1%2F858780%2F_858780_124995.jpg%2F,latelyFollower:544,wordCount:1046075,retentionRatio:33.3}}\n推荐给你看看这个，一直放在书架里还没看到的。你说的那个好像没有看到过哦，抱歉",
     * "author": {
     * "_id": "55824cce2460cf522ee58069",
     * "avatar": "/avatar/40/a1/40a1088b44838f26c0403982f999e3d0",
     * "nickname": "暖心",
     * "monthly": false,
     * "lv": 10,
     * "gender": "female"
     * },
     * "created": "2018-01-30T07:18:04.958Z",
     * "commentCount": 5,
     * "upvoteCount": 20,
     * "isUpvote": false,
     * "bestComments": [],
     * "shareLink": "",
     * "shareIcon": ""
     * }
     */
    public static class Answer {
        public String id;
        public String question;
        public String title;
        public String content;
        public Author author;
        public String created;
        public int commentCount;
        public int upvoteCount;
        public boolean isUpvote;
        public Object bestComments;
        public String shareLink;
        public String shareIcon;
    }

    public static class RichPost {
        public int postType;
        public String idType;
        public String id;

        public void startTargetActivity(Context context) {
            if (idType.equals("post")) {
                PostsDetailActivity.startActivity(context, id, postType);
            } else if (idType.equals("book")) {
                BookDetailActivity.startActivity(context, id);
            } else {
                Log.e("startTargetActivity error: idType = " + idType);
            }
        }
    }
}
