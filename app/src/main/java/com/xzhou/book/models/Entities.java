package com.xzhou.book.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.DrawableRes;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.xzhou.book.R;
import com.xzhou.book.community.PostsDetailActivity;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.net.ZhuiShuSQApi;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Entities {
    public static class RankLv0 implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = -9180488290284440929L;
        public String name;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT;
        }
    }

    public static class RankLv1 extends AbstractExpandableItem<RankLv2> implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = -1912241267177192958L;
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

        private static final long serialVersionUID = -2004408387569841136L;

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

    public static class CategoryLv0 implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 4503104075382863616L;
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
        private static final long serialVersionUID = -6510140023060532790L;
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

    public static class NetBook implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 8124319381906598755L;
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

        public String webUrl;

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

    public static class BookSource implements Serializable {
        public static final Type TYPE = new TypeToken<List<BookSource>>() {
        }.getType();
        private static final long serialVersionUID = 2232095540676922303L;

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

    public static class Recommend implements Serializable {
        public static final Type TYPE = new TypeToken<Recommend>() {
        }.getType();
        private static final long serialVersionUID = -314045338627102267L;

        public List<NetBook> books;

    }

    public static class BookMixAToc implements Serializable {
        public static final Type TYPE = new TypeToken<BookMixAToc>() {
        }.getType();
        private static final long serialVersionUID = -7022321056936782674L;

        public BookAToc mixToc;
    }

    public static class BookAToc implements Serializable {
        public static final Type TYPE = new TypeToken<BookAToc>() {
        }.getType();
        private static final long serialVersionUID = 1326518435778665143L;

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

    public static class Chapters implements Serializable {
        public static final Type TYPE = new TypeToken<List<Chapters>>() {
        }.getType();
        private static final long serialVersionUID = -250188387162998804L;

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

    public static class ChapterRead implements Serializable {
        public static final Type TYPE = new TypeToken<ChapterRead>() {
        }.getType();
        private static final long serialVersionUID = 8019998476123502165L;
        public Chapter chapter;
    }

    public static class Chapter implements Serializable {
        public static final Type TYPE = new TypeToken<Chapter>() {
        }.getType();
        private static final long serialVersionUID = 5453537778626171634L;
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
                return new ArrayList<>();
            }
            String[] list = images.split(",");
            return Arrays.asList(list);
        }

        public List<String> getImageScales() {
            if (TextUtils.isEmpty(imageScale)) {
                return new ArrayList<>();
            }
            String[] list = imageScale.split(",");
            return Arrays.asList(list);
        }

        @Override
        public String toString() {
            return "Chapter{" +
                    "title='" + title + '\'' +
                    ", id='" + id + '\'' +
                    ", images='" + images + '\'' +
                    ", imageScale='" + imageScale + '\'' +
                    '}';
        }
    }

    public static class HotWord implements Serializable {
        public static final Type TYPE = new TypeToken<HotWord>() {
        }.getType();
        private static final long serialVersionUID = 8484249905415090498L;

        public List<String> hotWords;
    }

    public static class AutoSuggest implements Serializable {
        public static final Type TYPE = new TypeToken<AutoSuggest>() {
        }.getType();
        private static final long serialVersionUID = -4389212954767932923L;

        public List<Suggest> keywords;
    }

    /**
     * "text": "妖神记",
     * "tag": "bookname", tag bookauthor
     * "id": "55aed62b1aa7a382698aeae4",
     * "author": "发飙的蜗牛",
     * "contentType": "txt" "picture"
     */
    public static class Suggest implements Serializable {
        private static final long serialVersionUID = -5997328821423890809L;
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

    public static class AutoComplete implements Serializable {
        public static final Type TYPE = new TypeToken<AutoComplete>() {
        }.getType();
        private static final long serialVersionUID = -1476539022633507083L;

        public List<String> keywords;
    }

    public static class SearchResult implements Serializable {
        public static final Type TYPE = new TypeToken<SearchResult>() {
        }.getType();
        private static final long serialVersionUID = 534631677737924730L;

        public List<SearchBook> books;
    }

    public static class SearchBook extends NetBook {
        private static final long serialVersionUID = 9143168492051165754L;
        public boolean hasCp;
        public String aliases;
        public String superscript;
        public int wordCount; //字数

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_BOOK_BY_SEARCH;
        }
    }

    public static class BooksByTag implements Serializable {
        public static final Type TYPE = new TypeToken<BooksByTag>() {
        }.getType();
        private static final long serialVersionUID = -1335408562764472266L;

        public List<TagBook> books;

        public static class TagBook extends NetBook {
            private static final long serialVersionUID = 2647013732145388280L;
            public String majorCate;
            public String minorCate;
            public List<String> tags;

            @Override
            public int getItemType() {
                return tags == null ? Constant.ITEM_TYPE_BOOK_BY_AUTHOR : Constant.ITEM_TYPE_BOOK_BY_TAG;
            }
        }
    }

    public static class HotReview implements Serializable {
        public static final Type TYPE = new TypeToken<HotReview>() {
        }.getType();
        private static final long serialVersionUID = 671010256822939874L;
        public List<Reviews> reviews;
    }

    public static class Reviews implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 6594789351870269974L;
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

    public static class RecommendBookList implements Serializable {
        public static final Type TYPE = new TypeToken<RecommendBookList>() {
        }.getType();
        private static final long serialVersionUID = -7367402286952831123L;

        public List<BookListBean> booklists;
    }

    public static class BookDetail implements Serializable {
        public static final Type TYPE = new TypeToken<BookDetail>() {
        }.getType();
        private static final long serialVersionUID = 4337701199927804961L;

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
            if (cover != null && cover.startsWith("https")) {
                return cover;
            }
            return ZhuiShuSQApi.IMG_BASE_URL + cover;
        }

        public boolean isPicture() {
            return "picture".equals(contentType);
        }
    }

    public static class RankingList implements Serializable {
        public static final Type TYPE = new TypeToken<RankingList>() {
        }.getType();
        private static final long serialVersionUID = -266883377968780617L;

        public List<RankLv1> female;
        public List<RankLv1> male;
        public List<RankLv1> picture;
        public List<RankLv1> epub;
    }

    public static class Rankings implements Serializable {
        public static final Type TYPE = new TypeToken<Rankings>() {
        }.getType();
        private static final long serialVersionUID = -2883712310300836939L;

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

    public static class SearchBookList implements Serializable {
        public static final Type TYPE = new TypeToken<SearchBookList>() {
        }.getType();
        private static final long serialVersionUID = -8219577493348600922L;

        public String ok;
        public int total;
        public List<UgcBookList> ugcbooklists;

        public static class UgcBookList implements MultiItemEntity, Serializable {
            private static final long serialVersionUID = -5784992198253143686L;
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

            @Override
            public int getItemType() {
                return Constant.ITEM_TYPE_UGC_BOOK_LIST;
            }

            public String cover() {
                return cover == null ? null : ZhuiShuSQApi.IMG_BASE_URL + cover;
            }
        }
    }

    public static class BookLists implements Serializable {
        public static final Type TYPE = new TypeToken<BookLists>() {
        }.getType();
        private static final long serialVersionUID = 5703939287108296077L;

        public List<BookListBean> bookLists;
    }

    //书单列表
    public static class BookListBean implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = -2230487416004690409L;
        private String _id;
        private String id;
        public String title;
        public String author;
        public String desc;
        public String gender;
        public int collectorCount;
        private String cover;
        public int bookCount;

        public List<String> covers;
        public boolean isDistillate; //精品书单
        public String created;
        public String updated;
        public Highlight highlight;

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

    public static class BookListTags implements Serializable {
        public static final Type TYPE = new TypeToken<List<BookListTags>>() {
        }.getType();
        private static final long serialVersionUID = 2184291046781015239L;

        public String name;
        public List<String> tags;
    }

    public static class BookListDetail implements Serializable {
        public static final Type TYPE = new TypeToken<BookListDetail>() {
        }.getType();
        private static final long serialVersionUID = -2508257227621938141L;

        public BookList bookList;

        public static class BookList implements Serializable {
            private static final long serialVersionUID = 7542864738121152522L;
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

        public static class BooksBean implements Serializable {
            private static final long serialVersionUID = -877098187170153307L;
            public BookBean book;
            public String comment;

            public static class BookBean implements Serializable {
                private static final long serialVersionUID = 15112697960589438L;
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

    public static class CategoryList implements Serializable {
        public static final Type TYPE = new TypeToken<CategoryList>() {
        }.getType();
        private static final long serialVersionUID = -3508231042411920480L;

        public List<Category> male; //男生
        public List<Category> female; //女生
        public List<Category> picture; //漫画
        public List<Category> press; //出版

        public static class Category implements Serializable {
            private static final long serialVersionUID = 8228067616565153842L;
            public String name;
            public int bookCount;
            public List<String> minors;
        }
    }

    public static class CategoryListLv2 implements Serializable {
        public static final Type TYPE = new TypeToken<CategoryListLv2>() {
        }.getType();
        private static final long serialVersionUID = -8335335881648243895L;

        public List<Category> male;
        public List<Category> female;
        public List<Category> picture; //漫画
        public List<Category> press; //出版

        public static class Category implements Serializable {
            private static final long serialVersionUID = -6364697795582709572L;
            public String major;
            public List<String> mins;
        }
    }

    public static class BooksByCats implements Serializable {
        public static final Type TYPE = new TypeToken<BooksByCats>() {
        }.getType();
        private static final long serialVersionUID = -1372795524182374938L;
        public List<CatBook> books;

        public static class CatBook extends NetBook {
            private static final long serialVersionUID = 7918738076925282174L;
            public String majorCate;
            public List<String> tags;
        }
    }

    //综合讨论区帖子列表
    public static class DiscussionList implements Serializable {
        public static final Type TYPE = new TypeToken<DiscussionList>() {
        }.getType();
        private static final long serialVersionUID = -5525830495946252290L;
        public List<Posts> posts;
    }

    public static class Posts implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = -2112428348261246258L;
        public String _id;
        public String title;
        public String type; //type=vote 投票  review 书评
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
            return "review".equals(type);
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
    public static class DiscussionDetail implements Serializable {
        public static final Type TYPE = new TypeToken<DiscussionDetail>() {
        }.getType();
        private static final long serialVersionUID = 1196838662222907274L;
        public PostDetail post;

        public static class PostDetail implements Serializable {
            private static final long serialVersionUID = -7495352065630942615L;
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
            public PostBook book;

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

            static class Author implements Serializable {
                private static final long serialVersionUID = 1586665621373770284L;
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

            static class PostBook implements Serializable {
                private static final long serialVersionUID = -2411399512027520700L;
                private String _id;
                private String title;
                private String cover;
                private int postCount;
                private int latelyFollower;
                private int followerCount;
                private String id;
                private Object retentionRatio;
            }

            public static class Vote implements MultiItemEntity, Serializable {
                private static final long serialVersionUID = 1321789582240496772L;
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
    public static class CommentList implements Serializable {
        public static final Type TYPE = new TypeToken<CommentList>() {
        }.getType();
        private static final long serialVersionUID = -8523560177164443917L;
        public List<Comment> comments;
    }

    public static class Comment implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 4937947744093502285L;
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

        static class ReplyTo implements Serializable {
            private static final long serialVersionUID = -1615777799412391571L;
            private String _id;
            private int floor;//楼层
            private Author author; //回复XXX

            public String nickname() {
                return author == null ? "" : author.nickname;
            }

            static class Author implements Serializable {
                private static final long serialVersionUID = -6229864775655527053L;
                private String _id;
                private String nickname;
            }
        }
    }

    public static class PostSection implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = -5400900050812469223L;
        public String text;

        public PostSection(String text) {
            this.text = text;
        }

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_TEXT;
        }
    }

    public static class Helpful implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 3726918062779885415L;
        public int total;
        public int no;
        public int yes;

        @Override
        public int getItemType() {
            return Constant.ITEM_TYPE_HELPFUL;
        }
    }

    //书评区列表
    public static class PostReviewList implements Serializable {
        public static final Type TYPE = new TypeToken<PostReviewList>() {
        }.getType();
        private static final long serialVersionUID = 3924751709076210194L;
        public List<PostsReviews> reviews;
    }

    public static class PostsReviews implements MultiItemEntity, Serializable {
        private static final long serialVersionUID = 1716484738624516479L;
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

        public static class Book implements Serializable {
            private static final long serialVersionUID = 8196748055606935418L;
            public String _id;
            public String cover;
            public String title;
            public String site;
            public String type;
        }
    }

    public static class ReviewDetail implements Serializable {
        public static final Type TYPE = new TypeToken<ReviewDetail>() {
        }.getType();
        private static final long serialVersionUID = -8892513155130320888L;
        public ReviewDetailHeader review;
    }

    public static class ReviewDetailHeader implements Serializable {
        private static final long serialVersionUID = 2606438032303717138L;
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

        static class ReviewBook implements Serializable {
            private static final long serialVersionUID = 7903682719875619332L;
            private String _id;
            private String cover;
            private String author;
            private String title;
            private String id;
        }

        static class ReviewAuthor implements Serializable {
            private static final long serialVersionUID = -8046737239973377034L;
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

    public static class BookHelpList implements Serializable {
        public static final Type TYPE = new TypeToken<BookHelpList>() {
        }.getType();
        private static final long serialVersionUID = 8222522964500523139L;
        public List<HelpsBean> helps;

        public static class HelpsBean implements MultiItemEntity, Serializable {
            private static final long serialVersionUID = 4776505755538829516L;
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

    public static class BookHelp implements Serializable {
        public static final Type TYPE = new TypeToken<BookHelp>() {
        }.getType();
        private static final long serialVersionUID = -6379329188066668669L;
        public HelpDetail help;

        public static class HelpDetail implements Serializable {
            private static final long serialVersionUID = -1525084132173575489L;
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

            static class Author implements Serializable {
                private static final long serialVersionUID = 8122834472750663625L;
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

    public static class Login implements Serializable {
        public static final Type TYPE = new TypeToken<Login>() {
        }.getType();
        private static final long serialVersionUID = 247622130995452712L;
        public UserBean user;
        public String token;

        public static class UserBean implements Serializable {
            private static final long serialVersionUID = -8358199002978973566L;
            public String _id;
            public String nickname;
            public String avatar;
            public int exp;
            public int lv;
            public String gender;
            public String type;

            public String avatar() {
                return ZhuiShuSQApi.IMG_BASE_URL + avatar;
            }
        }
    }

    public static class Updated implements Serializable {
        public static final Type TYPE = new TypeToken<List<Updated>>() {
        }.getType();
        private static final long serialVersionUID = -4613607655337320780L;

        public String _id;
        public String author;
        public boolean allowMonthly;
        public String referenceSource;
        public String updated;
        public String chaptersCount;
        public String lastChapter;
    }

    public static class HelpSearchResult implements Serializable {
        public static final Type TYPE = new TypeToken<HelpSearchResult>() {
        }.getType();
        public String next;//MTA=
        public List<Question> questions;
    }

    public static class Question implements Serializable {
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

    public static class Highlight implements Serializable {
        private static final long serialVersionUID = -2561657440368018660L;
        public List<String> title;
    }

    public static class Author implements Serializable {
        private static final long serialVersionUID = -6355424730258880688L;
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
    public static class Answer implements Serializable {
        private static final long serialVersionUID = 1614776734002863193L;
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

    public static class RichPost implements Serializable {
        private static final long serialVersionUID = 4373049668833164042L;
        public String idType;
        public String id;

        public void startTargetActivity(Context context) {
            if (idType.equals("post")) {
                PostsDetailActivity.startActivity(context, id, PostsDetailActivity.TYPE_DISCUSS);
            } else if (idType.equals("book")) {
                BookDetailActivity.startActivity(context, id);
            } else {
                Log.e("startTargetActivity error: idType = " + idType);
            }
        }
    }

    public static class SupportBean implements Serializable {
        public static final Type TYPE = new TypeToken<SupportBean>() {
        }.getType();
        private static final long serialVersionUID = 7366321973435017459L;

        public String name;
        public String url;
        public String icon;
        public String searchUrl;
        public String searchKey;
        public String pageKey;
        public List<SupportBeanEntry> entry;
        public int checkIndex;

        @Override
        public String toString() {
            return "SupportBean{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", icon='" + icon + '\'' +
                    ", searchUrl='" + searchUrl + '\'' +
                    ", searchKey='" + searchKey + '\'' +
                    ", pageKey='" + pageKey + '\'' +
                    ", entry=" + entry +
                    '}';
        }
    }

    public static class SupportBeanEntry implements Serializable {
        private static final long serialVersionUID = -442833043524291115L;
        public String gender;
        public String desc;
        public String rootUrl;
        public List<SupportBeanEntryParam> params;

        @Override
        public String toString() {
            return "SupportBeanEntry{" +
                    "gender='" + gender + '\'' +
                    ", desc='" + desc + '\'' +
                    ", rootUrl='" + rootUrl + '\'' +
                    ", params=" + params +
                    '}';
        }
    }

    public static class SupportBeanEntryParam implements Serializable {
        private static final long serialVersionUID = 3344817710113187851L;
        public String name;
        public String key;
        public List<SupportBeanEntryParamEnum> enums;
        private List<String> tags;
        public int checkIndex = 0;

        public List<String> getTags() {
            if (tags == null) {
                tags = new ArrayList<>();
                for (SupportBeanEntryParamEnum entryParamEnum : enums) {
                    tags.add(entryParamEnum.name + "," + entryParamEnum.value);
                }
            }
            return tags;
        }

        @Override
        public String toString() {
            return "SupportBeanEntryParam{" +
                    "name='" + name + '\'' +
                    ", key='" + key + '\'' +
                    ", enums=" + enums +
                    '}';
        }
    }

    public static class SupportBeanEntryParamEnum implements Serializable {
        private static final long serialVersionUID = -1157398056819279717L;
        public String name;
        public String value;

        @Override
        public String toString() {
            return "SupportBeanEntryParamEnum{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public static class ThirdBookData implements Serializable {
        private static final long serialVersionUID = -7613650711016024906L;
        public int pageCount = 1;
        public int pageCurrent = 0;
        public List<SearchModel.SearchBook> list;

        @Override
        public String toString() {
            return "ThirdBookData{" +
                    "pageCount=" + pageCount +
                    ", pageCurrent=" + pageCurrent +
                    ", list=" + list +
                    '}';
        }
    }

    public static class ThirdBookDetail {
        public String title;
        public String image;
        public String author;
        public String readUrl;
        public String tags;
        public String lastUpdate;
        public String lastChapter;
        public String intro;
        public List<Pair<String, String>> list = new ArrayList<>();;

        @Override
        public String toString() {
            return "ThirdBookDetail{" +
                    "title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", readUrl='" + readUrl + '\'' +
                    ", image='" + image + '\'' +
                    ", list='" + list + '\'' +
                    ", tags='" + tags + '\'' +
                    ", lastUpdate='" + lastUpdate + '\'' +
                    ", lastChapter='" + lastChapter + '\'' +
                    ", intro='" + intro + '\'' +
                    '}';
        }
    }
}
