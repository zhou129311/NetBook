package com.xzhou.book.datasource;

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

    private static ZhuiShuSQApi sInstance;
    private static ExecutorService mPool;

    private ZhuiShuSQApi() {
    }

    public static ZhuiShuSQApi get() {
        if (sInstance == null) {
            sInstance = new ZhuiShuSQApi();
        }
        return sInstance;
    }

    public static ExecutorService getPool() {
        if (mPool == null || mPool.isTerminated() || mPool.isShutdown()) {
            mPool = Executors.newFixedThreadPool(5);
        }
        return mPool;
    }

    /**
     * 根据性别获取推荐列表
     *
     * @param gender male  female
     * @return Recommend
     */
    public Entities.Recommend getRecommend(String gender) {
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
    public HttpResult getHotWord() {
        HttpRequest request = new HttpRequest("/book/hot-word");
        return OkHttpUtils.get(request, Entities.HotWord.TYPE, null);
    }

    /**
     * 关键字自动补全
     *
     * @return AutoComplete
     */
    public HttpResult getAutoComplete(String query) {
        HttpRequest request = new HttpRequest("/book/auto-complete");
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        return OkHttpUtils.get(request, Entities.AutoComplete.TYPE, params);
    }

    /**
     * 书籍查询
     *
     * @return SearchResult
     */
    public HttpResult getSearchResult(String query, int start, int limit) {
        HttpRequest request = new HttpRequest("/book/fuzzy-search");
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("start", String.valueOf(start));
        params.put("limit", String.valueOf(limit));
        return OkHttpUtils.get(request, Entities.SearchResult.TYPE, params);
    }

    /**
     * 通过作者查询书名
     *
     * @return BooksByTag
     */
    public Entities.BooksByTag searchBooksByAuthor(String author) {
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
    public Entities.BookDetail getBookDetail(String bookId) {
        HttpRequest request = new HttpRequest("/book", bookId);
        return (Entities.BookDetail) OkHttpUtils.getObject(request, Entities.BookDetail.TYPE, null);
    }

    /**
     * @param book bookId
     * @return
     */
    public Entities.HotReview getHotReview(String book) {
        HttpRequest request = new HttpRequest("/post/review/best-by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", book);
        return (Entities.HotReview) OkHttpUtils.getObject(request, Entities.HotReview.TYPE, params);
    }

    /**
     * 获取书籍对应的推荐书单
     *
     * @param bookId bookId
     * @return Recommend
     */
    public Entities.Recommend getRecommendBook(String bookId) {
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
    public Entities.RecommendBookList getRecommendBookList(String bookId, String limit) {
        HttpRequest request = new HttpRequest("/book-list", bookId, "recommend");
        HashMap<String, String> params = new HashMap<>();
        params.put("limit", limit);
        return (Entities.RecommendBookList) OkHttpUtils.getObject(request, Entities.RecommendBookList.TYPE, params);
    }

    public Entities.BooksByTag getBooksByTag(String tags, int start, int limit) {
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
    public HttpResult getBookMixToc(String bookId) {
        HttpRequest request = new HttpRequest("/mix-toc", bookId);
        return OkHttpUtils.get(request, Entities.BookMixAToc.TYPE, null);
    }

    /**
     * @param bookId 书源ID
     * @return BookMixAToc
     */
    public HttpResult getBookMixAToc(String bookId) {
        HttpRequest request = new HttpRequest("/mix-atoc", bookId);
        HashMap<String, String> params = new HashMap<>();
        params.put("view", "chapters");
        return OkHttpUtils.get(request, Entities.BookMixAToc.TYPE, params);
    }

    /**
     * @param url getBookMixAToc中获取的link
     * @return ChapterRead
     */
    public HttpResult getChapterRead(String url) {
        HttpRequest request = new HttpRequest("http://chapter2.zhuishushenqi.com/chapter", url);
        return OkHttpUtils.get(request, Entities.ChapterRead.TYPE, null);
    }

    /**
     * 获取正版源(若有) 与 盗版源
     *
     * @param book bookid
     */
    public List<Entities.BookSource> getBookSource(String book) {
        HttpRequest request = new HttpRequest("/atoc");
        HashMap<String, String> params = new HashMap<>();
        params.put("view", "summary");
        params.put("book", book);
        Object o = OkHttpUtils.getObject(request, Entities.BookSource.TYPE, params);
        if (o instanceof List) {
            return (List<Entities.BookSource>) o;
        } else {
            return null;
        }
    }

    /**
     * 获取所有排行榜
     *
     * @return RankingList
     */
    public Entities.RankingList getRanking() {
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
    public Entities.Rankings getRanking(String rankingId) {
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
    public HttpResult getBookLists(String duration, String sort, String start, String limit, String tag, String gender) {
        HttpRequest request = new HttpRequest("/book-list");
        HashMap<String, String> params = new HashMap<>();
        params.put("duration", duration);
        params.put("sort", sort);
        params.put("start", start);
        params.put("limit", limit);
        params.put("tag", tag);
        params.put("gender", gender);
        return OkHttpUtils.get(request, Entities.BookLists.TYPE, params);
    }

    /**
     * 获取主题书单标签列表
     *
     * @return List<BookListTags>
     */
    public HttpResult getBookListTags() {
        HttpRequest request = new HttpRequest("/book-list/tagType");
        return OkHttpUtils.get(request, Entities.BookListTags.TYPE, null);
    }

    /**
     * 获取书单详情
     *
     * @return BookListDetail
     */
    public HttpResult getBookListDetail(String bookListId) {
        HttpRequest request = new HttpRequest("/book-list", bookListId);
        return OkHttpUtils.get(request, Entities.BookListDetail.TYPE, null);
    }

    /**
     * 获取分类
     *
     * @return CategoryList
     */
    public Entities.CategoryList getCategoryList() {
        HttpRequest request = new HttpRequest("/cats/lv2/statistics");
        return (Entities.CategoryList) OkHttpUtils.getObject(request, Entities.CategoryList.TYPE, null);
    }

    /**
     * 获取二级分类
     *
     * @return CategoryListLv2
     */
    public Entities.CategoryListLv2 getCategoryListLv2() {
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
    public Entities.BooksByCats getBooksByCats(String gender, String type, String major, String minor, int start, int limit) {
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
    public HttpResult getBookDisscussionList(String block, String duration, String sort, String type, String start, String limit, String distillate) {
        HttpRequest request = new HttpRequest("/post/by-block");
        HashMap<String, String> params = new HashMap<>();
        params.put("block", block);
        params.put("duration", duration);
        params.put("sort", sort);
        params.put("type", type);
        params.put("start", start);
        params.put("limit", limit);
        params.put("distillate", distillate);
        return OkHttpUtils.get(request, Entities.DiscussionList.TYPE, params);
    }

    /**
     * 获取综合讨论区帖子详情
     *
     * @param disscussionId->_id
     * @return Disscussion
     */
    public HttpResult getBookDisscussionDetail(String disscussionId) {
        HttpRequest request = new HttpRequest("/post", disscussionId);
        return OkHttpUtils.get(request, Entities.Disscussion.TYPE, null);
    }

    /**
     * 获取神评论列表(综合讨论区、书评区、书荒区皆为同一接口)
     *
     * @param disscussionId->_id
     * @return CommentList
     */
    public HttpResult getBestComments(String disscussionId) {
        HttpRequest request = new HttpRequest("/post", disscussionId, "comment/best");
        return OkHttpUtils.get(request, Entities.CommentList.TYPE, null);
    }

    /**
     * 获取综合讨论区帖子详情内的评论列表
     *
     * @param disscussionId->_id
     * @param start              0
     * @param limit              30
     * @return CommentList
     */
    public HttpResult getBookDisscussionComments(String disscussionId, String start, String limit) {
        HttpRequest request = new HttpRequest("/post", disscussionId, "comment");
        HashMap<String, String> params = new HashMap<>();
        params.put("start", start);
        params.put("limit", limit);
        return OkHttpUtils.get(request, Entities.CommentList.TYPE, params);
    }

    /**
     * 获取书评区帖子列表
     * 全部、全部类型、默认排序  http://api.zhuishushenqi.com/post/review?duration=all&sort=updated&type=all&start=0&limit=20&distillate=
     * 精品、玄幻奇幻、默认排序  http://api.zhuishushenqi.com/post/review?duration=all&sort=updated&type=xhqh&start=0&limit=20&distillate=true
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
     * @return BookReviewList
     */
    public HttpResult getBookReviewList(String duration, String sort, String type, String start, String limit, String distillate) {
        HttpRequest request = new HttpRequest("/post/review");
        HashMap<String, String> params = new HashMap<>();
        params.put("duration", duration);
        params.put("sort", sort);
        params.put("type", type);
        params.put("start", start);
        params.put("limit", limit);
        params.put("distillate", distillate);
        return OkHttpUtils.get(request, Entities.BookReviewList.TYPE, params);
    }

    /**
     * 获取书评区帖子详情
     *
     * @param bookReviewId->_id
     * @return BookReview
     */
    public HttpResult getBookReviewDetail(String bookReviewId) {
        HttpRequest request = new HttpRequest("/post/review", bookReviewId);
        return OkHttpUtils.get(request, Entities.BookReview.TYPE, null);
    }

    /**
     * 获取书评区、书荒区帖子详情内的评论列表
     *
     * @param bookReviewId->_id
     * @param start             0
     * @param limit             30
     * @return CommentList
     */
    public HttpResult getBookReviewComments(String bookReviewId, String start, String limit) {
        HttpRequest request = new HttpRequest("/post/review", bookReviewId, "comment");
        HashMap<String, String> params = new HashMap<>();
        params.put("start", start);
        params.put("limit", limit);
        return OkHttpUtils.get(request, Entities.CommentList.TYPE, params);
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
    public HttpResult getBookHelpList(String duration, String sort, String start, String limit, String distillate) {
        HttpRequest request = new HttpRequest("/post/help");
        HashMap<String, String> params = new HashMap<>();
        params.put("duration", duration);
        params.put("sort", sort);
        params.put("start", start);
        params.put("limit", limit);
        params.put("distillate", distillate);
        return OkHttpUtils.get(request, Entities.BookHelpList.TYPE, params);
    }

    /**
     * 获取书荒区帖子详情
     *
     * @param helpId->_id
     * @return BookHelp
     */
    public HttpResult getBookHelpDetail(String helpId) {
        HttpRequest request = new HttpRequest("/post/help", helpId);
        return OkHttpUtils.get(request, Entities.BookHelp.TYPE, null);
    }

    /**
     * 第三方登陆
     *
     * @return Login
     */
    public HttpResult login(String platform_uid, String platform_token, String platform_code) {
        HttpRequest request = new HttpRequest("/user/login");
        HashMap<String, String> body = new HashMap<>();
        body.put("platform_uid", platform_uid);
        body.put("platform_token", platform_token);
        body.put("platform_code", platform_code);
        String content = new JSONObject(body).toString();
        return OkHttpUtils.post(request, Entities.Login.TYPE, content);
    }

    /**
     * 获取书籍详情讨论列表
     *
     * @param book  bookId
     * @param sort  updated(默认排序)
     *              created(最新发布)
     *              comment-count(最多评论)
     * @param type  normal
     *              vote
     * @param start 0
     * @param limit 20
     * @return DiscussionList
     */
    public HttpResult getBookDetailDisscussionList(String book, String sort, String type, String start, String limit) {
        HttpRequest request = new HttpRequest("/post/by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", book);
        params.put("sort", sort);
        params.put("type", type);
        params.put("start", start);
        params.put("limit", limit);
        return OkHttpUtils.get(request, Entities.DiscussionList.TYPE, params);
    }

    /**
     * 获取书籍详情书评列表
     *
     * @param book  bookId
     * @param sort  updated(默认排序)
     *              created(最新发布)
     *              helpful(最有用的)
     *              comment-count(最多评论)
     * @param start 0
     * @param limit 20
     * @return HotReview
     */
    public HttpResult getBookDetailReviewList(String book, String sort, String start, String limit) {
        HttpRequest request = new HttpRequest("/post/review/by-book");
        HashMap<String, String> params = new HashMap<>();
        params.put("book", book);
        params.put("sort", sort);
        params.put("start", start);
        params.put("limit", limit);
        return OkHttpUtils.get(request, Entities.HotReview.TYPE, params);
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
    public HttpResult getGirlBookDisscussionList(String block, String duration, String sort, String type, String start, String limit, String distillate) {
        HttpRequest request = new HttpRequest("/post/by-block");
        HashMap<String, String> params = new HashMap<>();
        params.put("block", block);
        params.put("sort", sort);
        params.put("start", start);
        params.put("duration", duration);
        params.put("type", type);
        params.put("distillate", distillate);
        params.put("limit", limit);
        return OkHttpUtils.get(request, Entities.DiscussionList.TYPE, params);
    }
}
