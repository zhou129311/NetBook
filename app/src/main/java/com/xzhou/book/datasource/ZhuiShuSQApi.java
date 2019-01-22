package com.xzhou.book.datasource;

import android.text.TextUtils;

import com.xzhou.book.models.Entities;
import com.xzhou.book.models.Entities.HttpResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZhuiShuSQApi {
    public static final String TAG = "ZhuiShuSQApi";
    public static final String IMG_BASE_URL = "http://statics.zhuishushenqi.com";
    public static final String API_BASE_URL = "http://api.zhuishushenqi.com";
    public static final String IGNORE_HOST = "vip.zhuishushenqi.com";

    public static final String DURATION = "duration";
    public static final String DISTILLATE = "distillate";
    public static final String SORT = "sort";
    public static final String TYPE = "type";

    private static ExecutorService mPool;

    private ZhuiShuSQApi() {
    }

    public static ExecutorService getPool() {
        if (mPool == null || mPool.isTerminated() || mPool.isShutdown()) {
            mPool = Executors.newFixedThreadPool(5);
        }
        return mPool;
    }

    public static void getAggregationSource(String bookId) {
        HttpRequest request = new HttpRequest("/aggregation-source/by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", "bookId");
        params.put("v", "5");
    }

    /**
     * 根据bookids获取最近更新
     *
     * @param bookIds xxxx,xxxxx,xxxx
     */
    public static List<Entities.Updated> getBookshelfUpdated(String bookIds) {
        HttpRequest request = new HttpRequest("/book");
        HashMap<String, String> params = new HashMap<>();
        params.put("view", "updated");
        params.put("id", bookIds);
        return (List<Entities.Updated>) OkHttpUtils.getObject(request, Entities.Updated.TYPE, params, false);
    }

    /**
     * 根据性别获取推荐列表
     *
     * @param gender male  female
     * @return Recommend
     */
    public static Entities.Recommend getRecommend(String gender) {
        HttpRequest request = new HttpRequest("/book/recommend");
        HashMap<String, String> params = new HashMap<>();
        params.put("gender", gender);
        return (Entities.Recommend) OkHttpUtils.getObject(request, Entities.Recommend.TYPE, params);
    }

    /**
     * 大家都在搜
     *
     * @return HotWord
     */
    public static Entities.HotWord getHotWord() {
        HttpRequest request = new HttpRequest("/book/hot-word");
        return (Entities.HotWord) OkHttpUtils.getObject(request, Entities.HotWord.TYPE, null);
    }

    /**
     * 关键字自动补全
     *
     * @return AutoComplete
     */
    public static Entities.AutoComplete getAutoComplete(String query) {
        HttpRequest request = new HttpRequest("/book/auto-complete");
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        return (Entities.AutoComplete) OkHttpUtils.getObject(request, Entities.AutoComplete.TYPE, params);
    }

    /**
     * 书籍查询
     *
     * @return SearchResult
     */
    public static Entities.SearchResult getSearchResult(String query, int start, int limit) {
        HttpRequest request = new HttpRequest("/book/fuzzy-search");
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.SearchResult) OkHttpUtils.getObject(request, Entities.SearchResult.TYPE, params);
    }

    /**
     * 通过作者查询书籍
     *
     * @return BooksByTag
     */
    public static Entities.BooksByTag searchBooksByAuthor(String author) {
        HttpRequest request = new HttpRequest("/book/accurate-search");
        HashMap<String, String> params = new HashMap<>();
        params.put("author", author);
        return (Entities.BooksByTag) OkHttpUtils.getObject(request, Entities.BooksByTag.TYPE, params);
    }

    /**
     * 获取书籍详情
     *
     * @param bookId bookId
     * @return BookDetail
     */
    public static Entities.BookDetail getBookDetail(String bookId) {
        HttpRequest request = new HttpRequest("/book", bookId);
        return (Entities.BookDetail) OkHttpUtils.getObject(request, Entities.BookDetail.TYPE, null);
    }

    /**
     * @param book bookId
     * @return HotReview
     */
    public static Entities.HotReview getHotReview(String book) {
        HttpRequest request = new HttpRequest("/post/review/best-by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", book);
        return (Entities.HotReview) OkHttpUtils.getObject(request, Entities.HotReview.TYPE, params);
    }

    /**
     * 获取书籍对应的推荐书籍
     *
     * @param bookId bookId
     * @return Recommend
     */
    public static Entities.Recommend getRecommendBook(String bookId) {
        HttpRequest request = new HttpRequest("/book", bookId, "recommend");
        HashMap<String, String> params = new HashMap<>();
        return (Entities.Recommend) OkHttpUtils.getObject(request, Entities.Recommend.TYPE, params);
    }

    /**
     * 获取书籍对应的推荐书单
     *
     * @param bookId bookId
     * @param limit  4
     * @return RecommendBookList
     */
    public static Entities.RecommendBookList getRecommendBookList(String bookId, String limit) {
        HttpRequest request = new HttpRequest("/book-list", bookId, "recommend");
        HashMap<String, String> params = new HashMap<>();
        params.put("limit", limit);
        return (Entities.RecommendBookList) OkHttpUtils.getObject(request, Entities.RecommendBookList.TYPE, params);
    }

    public static Entities.BooksByTag getBooksByTag(String tags, int start, int limit) {
        HttpRequest request = new HttpRequest("/book/by-tags");
        HashMap<String, String> params = new HashMap<>();
        params.put("tags", tags);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.BooksByTag) OkHttpUtils.getObject(request, Entities.BooksByTag.TYPE, params);
    }

    /**
     * 获得混合书源
     *
     * @return BookMixAToc
     */
    public static Entities.BookMixAToc getBookMixToc(String bookId) {
        HttpRequest request = new HttpRequest("/mix-toc", bookId);
        return (Entities.BookMixAToc) OkHttpUtils.getObject(request, Entities.BookMixAToc.TYPE, null);
    }

    /**
     * 获取混合源章节列表
     *
     * @param bookId   bookId
     * @param sourceId sourceId
     * @return BookMixAToc
     */
    public static Entities.BookAToc getBookMixAToc(String bookId, String sourceId) {
        Entities.BookAToc aToc = null;
        if (!TextUtils.isEmpty(sourceId)) { //获取其他源
            HttpRequest request = new HttpRequest("/atoc", sourceId);
            HashMap<String, String> params = new HashMap<>();
            params.put("view", "chapters");
            aToc = (Entities.BookAToc) OkHttpUtils.getObject(request, Entities.BookAToc.TYPE, params, true);
        } else { //默认源
            Entities.BookMixAToc mixAToc = getBookMixAToc(bookId);
            if (mixAToc != null) {
                aToc = mixAToc.mixToc;
            }
        }
        return aToc;
    }

    private static Entities.BookMixAToc getBookMixAToc(String bookId) {
        HttpRequest request = new HttpRequest("/mix-atoc", bookId);
        HashMap<String, String> params = new HashMap<>();
        params.put("view", "chapters");
        return (Entities.BookMixAToc) OkHttpUtils.getObject(request, Entities.BookMixAToc.TYPE, params, true);
    }

    /**
     * @param url getBookMixAToc中获取的link
     * @return ChapterRead
     */
    public static Entities.ChapterRead getChapterRead(String url) {
        HttpRequest request = new HttpRequest("http://chapter2.zhuishushenqi.com/chapter", url);
        return (Entities.ChapterRead) OkHttpUtils.getObject(request, Entities.ChapterRead.TYPE, null);
    }

    /**
     * 获取正版源(若有) 与 盗版源
     *
     * @param book bookid
     */
    public static List<Entities.BookSource> getBookSource(String book) {
        HttpRequest request = new HttpRequest("/atoc");
        HashMap<String, String> params = new HashMap<>();
        params.put("view", "summary");
        params.put("book", book);
        return (List<Entities.BookSource>) OkHttpUtils.getObject(request, Entities.BookSource.TYPE, params);
    }

    /**
     * 获取所有排行榜
     *
     * @return RankingList
     */
    public static Entities.RankingList getRanking() {
        HttpRequest request = new HttpRequest("/ranking/gender");
        return (Entities.RankingList) OkHttpUtils.getObject(request, Entities.RankingList.TYPE, null);
    }

    /**
     * 获取单一排行榜
     * 周榜：rankingId->_id
     * 月榜：rankingId->monthRank
     * 总榜：rankingId->totalRank
     *
     * @return Rankings
     */
    public static Entities.Rankings getRanking(String rankingId) {
        HttpRequest request = new HttpRequest("/ranking", rankingId);
        return (Entities.Rankings) OkHttpUtils.getObject(request, Entities.Rankings.TYPE, null);
    }

    /**
     * 获取主题书单列表
     * 本周最热：duration=last-seven-days&sort=collectorCount
     * 最新发布：duration=all&sort=created
     * 最多收藏：duration=all&sort=collectorCount
     *
     * @param tag    {@link #getBookListTags()}
     * @param gender male、female
     * @param limit  20
     * @return BookLists
     */
    public static Entities.BookLists getBookLists(String duration, String sort, int start, int limit, String tag, String gender) {
        HttpRequest request = new HttpRequest("/book-list");
        HashMap<String, String> params = new HashMap<>();
        params.put(DURATION, duration);
        params.put(SORT, sort);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        params.put("tag", tag);
        params.put("gender", gender);
        return (Entities.BookLists) OkHttpUtils.getObject(request, Entities.BookLists.TYPE, params);
    }

    /**
     * 获取主题书单标签列表
     *
     * @return List<BookListTags>
     */
    public static List<Entities.BookListTags> getBookListTags() {
        HttpRequest request = new HttpRequest("/book-list/tagType");
        return (List<Entities.BookListTags>) OkHttpUtils.getObject(request, Entities.BookListTags.TYPE, null);
    }

    /**
     * 获取书单详情
     *
     * @return BookListDetail
     */
    public static Entities.BookListDetail getBookListDetail(String bookListId) {
        HttpRequest request = new HttpRequest("/book-list", bookListId);
        return (Entities.BookListDetail) OkHttpUtils.getObject(request, Entities.BookListDetail.TYPE, null);
    }

    /**
     * 获取分类
     *
     * @return CategoryList
     */
    public static Entities.CategoryList getCategoryList() {
        HttpRequest request = new HttpRequest("/cats/lv2/statistics");
        return (Entities.CategoryList) OkHttpUtils.getObject(request, Entities.CategoryList.TYPE, null);
    }

    /**
     * 获取二级分类
     *
     * @return CategoryListLv2
     */
    public static Entities.CategoryListLv2 getCategoryListLv2() {
        HttpRequest request = new HttpRequest("/cats/lv2");
        return (Entities.CategoryListLv2) OkHttpUtils.getObject(request, Entities.CategoryListLv2.TYPE, null);
    }

    /**
     * 按分类获取书籍列表
     *
     * @param gender male、female
     * @param type   hot(热门)、new(新书)、reputation(好评)、over(完结)
     * @param major  玄幻
     * @param minor  东方玄幻、异界大陆、异界争霸、远古神话
     * @param limit  50
     * @return BooksByCats
     */
    public static Entities.BooksByCats getBooksByCats(String gender, String type, String major, String minor, int start, int limit) {
        HttpRequest request = new HttpRequest("/book/by-categories");
        HashMap<String, String> params = new HashMap<>();
        params.put("gender", gender);
        params.put("type", type);
        params.put("major", major);
        params.put("minor", minor);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.BooksByCats) OkHttpUtils.getObject(request, Entities.BooksByCats.TYPE, params);
    }

    /**
     * 获取综合讨论区帖子列表
     * 全部、默认排序  post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&
     * 精品、默认排序  post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&distillate=true
     *
     * @param block      ramble:综合讨论区
     *                   original：原创区
     * @param duration   all
     * @param sort       updated(默认排序)
     *                   created(最新发布)
     *                   comment-count(最多评论)
     * @param type       all
     * @param start      0
     * @param limit      20
     * @param distillate true(精品)
     * @return DiscussionList
     */
    public static Entities.DiscussionList getBookDiscussionList(String block, String duration, String sort, String type,
                                                                int start, int limit, boolean distillate) {
        HttpRequest request = new HttpRequest("/post/by-block");
        HashMap<String, String> params = new HashMap<>();
        params.put("block", block);
        params.put(DURATION, duration);
        params.put(SORT, sort);
        params.put(TYPE, type);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        if (distillate) {
            params.put(DISTILLATE, "true");
        }
        return (Entities.DiscussionList) OkHttpUtils.getObject(request, Entities.DiscussionList.TYPE, params);
    }

    /**
     * 获取综合讨论区帖子详情
     *
     * @param discussionId->_id
     * @return DiscussionDetail
     */
    public static Entities.DiscussionDetail getBookDiscussionDetail(String discussionId) {
        HttpRequest request = new HttpRequest("/post", discussionId);
        return (Entities.DiscussionDetail) OkHttpUtils.getObject(request, Entities.DiscussionDetail.TYPE, null);
    }

    /**
     * 获取神评论列表(综合讨论区、书评区、书荒区皆为同一接口)
     *
     * @param discussionId->_id
     * @return Comment
     */
    public static Entities.CommentList getBestComments(String discussionId) {
        HttpRequest request = new HttpRequest("/post", discussionId, "comment/best");
        return (Entities.CommentList) OkHttpUtils.getObject(request, Entities.CommentList.TYPE, null);
    }

    /**
     * 获取综合讨论区帖子详情内的评论列表
     *
     * @param discussionId->_id
     * @param start             0
     * @param limit             30
     * @return Comment
     */
    public static Entities.CommentList getBookDiscussionComments(String discussionId, int start, int limit) {
        HttpRequest request = new HttpRequest("/post", discussionId, "comment");
        HashMap<String, String> params = new HashMap<>();
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.CommentList) OkHttpUtils.getObject(request, Entities.CommentList.TYPE, params);
    }

    /**
     * 获取书评区帖子列表
     * 全部、全部类型、默认排序  /post/review?duration=all&sort=updated&type=all&start=0&limit=20
     * 精品、玄幻奇幻、默认排序  /post/review?duration=all&sort=updated&type=xhqh&start=0&limit=20&distillate=true
     *
     * @param duration   all
     * @param sort       updated(默认排序)
     *                   created(最新发布)
     *                   helpful(最有用的)
     *                   comment-count(最多评论)
     * @param type       all(全部类型)、xhqh(玄幻奇幻)、dsyn(都市异能)...
     * @param start      0
     * @param limit      20
     * @param distillate true(精品) 、空字符（全部）
     * @return PostReviewList
     */
    public static Entities.PostReviewList getBookReviewList(String duration, String sort, String type, int start, int limit, boolean distillate) {
        HttpRequest request = new HttpRequest("/post/review");
        HashMap<String, String> params = new HashMap<>();
        params.put(DURATION, duration);
        params.put(SORT, sort);
        params.put(TYPE, type);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        if (distillate) {
            params.put(DISTILLATE, "true");
        }
        return (Entities.PostReviewList) OkHttpUtils.getObject(request, Entities.PostReviewList.TYPE, params);
    }

    /**
     * 获取书评帖子详情
     *
     * @param bookReviewId->_id
     * @return ReviewDetail
     */
    public static Entities.ReviewDetail getBookReviewDetail(String bookReviewId) {
        HttpRequest request = new HttpRequest("/post/review", bookReviewId);
        return (Entities.ReviewDetail) OkHttpUtils.getObject(request, Entities.ReviewDetail.TYPE, null);
    }

    /**
     * 获取书评区、书荒区帖子详情内的评论列表
     *
     * @param bookReviewId->_id
     * @param start             0
     * @param limit             30
     * @return Comment
     */
    public static Entities.CommentList getBookReviewComments(String bookReviewId, int start, int limit) {
        HttpRequest request = new HttpRequest("/post/review", bookReviewId, "comment");
        HashMap<String, String> params = new HashMap<>();
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.CommentList) OkHttpUtils.getObject(request, Entities.CommentList.TYPE, params);
    }

    /**
     * 获取书荒区帖子列表
     * 全部、默认排序  http://api.zhuishushenqi.com/post/help?duration=all&sort=updated&start=0&limit=20&distillate=
     * 精品、默认排序  http://api.zhuishushenqi.com/post/help?duration=all&sort=updated&start=0&limit=20&distillate=true
     *
     * @param duration   all
     * @param sort       updated(默认排序)
     *                   created(最新发布)
     *                   comment-count(最多评论)
     * @param start      0
     * @param limit      20
     * @param distillate true(精品) 、空字符（全部）
     * @return BookHelpList
     */
    public static Entities.BookHelpList getBookHelpList(String duration, String sort, int start, int limit, boolean distillate) {
        HttpRequest request = new HttpRequest("/post/help");
        HashMap<String, String> params = new HashMap<>();
        params.put(DURATION, duration);
        params.put(SORT, sort);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        if (distillate) {
            params.put(DISTILLATE, "true");
        }
        return (Entities.BookHelpList) OkHttpUtils.getObject(request, Entities.BookHelpList.TYPE, params);
    }

    /**
     * 获取书荒区帖子详情
     *
     * @param helpId->_id
     * @return BookHelp
     */
    public static Entities.BookHelp getBookHelpDetail(String helpId) {
        HttpRequest request = new HttpRequest("/post/help", helpId);
        return (Entities.BookHelp) OkHttpUtils.getObject(request, Entities.BookHelp.TYPE, null);
    }

    /**
     * 获取bookId对应的讨论列表
     *
     * @param bookId bookId
     * @param sort   updated(默认排序)
     *               created(最新发布)
     *               comment-count(最多评论)
     * @param type   normal 话题
     *               vote 投票
     *               "" 所有
     * @param start  0
     * @param limit  20
     * @return DiscussionList
     */
    public static Entities.DiscussionList getBookDiscussionList(String bookId, String sort, String type, int start, int limit) {
        HttpRequest request = new HttpRequest("/post/by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", bookId);
        params.put("sort", sort);
        params.put("type", type);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.DiscussionList) OkHttpUtils.getObject(request, Entities.DiscussionList.TYPE, params);
    }

    /**
     * 获取bookId对应的书评列表
     *
     * @param bookId bookId
     * @param sort   updated(默认排序)
     *               created(最新发布)
     *               helpful(最有用的)
     *               comment-count(最多评论)
     * @param start  0
     * @param limit  20
     * @return HotReview
     */
    public static Entities.HotReview getBookReviewList(String bookId, String sort, int start, int limit) {
        HttpRequest request = new HttpRequest("/post/review/by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", bookId);
        params.put("sort", sort);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return (Entities.HotReview) OkHttpUtils.getObject(request, Entities.HotReview.TYPE, params);
    }

    /**
     * 获取综合讨论区帖子列表
     * 全部、默认排序  http://api.zhuishushenqi.com/post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&distillate=
     * 精品、默认排序  http://api.zhuishushenqi.com/post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&distillate=true
     *
     * @param block      ramble:综合讨论区
     *                   original：原创区
     * @param duration   all
     * @param sort       updated(默认排序)
     *                   created(最新发布)
     *                   comment-count(最多评论)
     * @param type       all
     * @param start      0
     * @param limit      20
     * @param distillate true(精品)
     * @return DiscussionList
     */
    public static Entities.DiscussionList getGirlBookDisscussionList(String block, String duration, String sort, String type, int start, int limit, boolean distillate) {
        HttpRequest request = new HttpRequest("/post/by-block");
        HashMap<String, String> params = new HashMap<>();
        params.put("duration", duration);
        params.put("block", block);
        params.put("sort", sort);
        params.put("type", type);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        if (distillate) {
            params.put("distillate", "true");
        }
        return (Entities.DiscussionList) OkHttpUtils.getObject(request, Entities.DiscussionList.TYPE, params);
    }

    /**
     * 第三方登陆
     *
     * @return Login
     */
    public static HttpResult login(String platform_uid, String platform_token, String platform_code) {
        HttpRequest request = new HttpRequest("/user/login");
        HashMap<String, String> body = new HashMap<>();
        body.put("platform_uid", platform_uid);
        body.put("platform_token", platform_token);
        body.put("platform_code", platform_code);
        String content = new JSONObject(body).toString();
        return OkHttpUtils.post(request, Entities.Login.TYPE, content);
    }
}
