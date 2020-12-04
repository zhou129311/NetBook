package com.xzhou.book.net;

import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaiduSearch extends JsoupSearch {

    public BaiduSearch() {
        super("BaiduSearch");
    }

    /*BAIDUID=B4182E17D5732E369C8084335DDFB608:FG=1; BIDUPSID=B4182E17D5732E369C8084335DDFB608;
    PSTM=1546614379; MCITY=-2654%3A; BD_UPN=12314753; delPer=0; BD_CK_SAM=1;
    PSINO=7; BD_HOME=0; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598;
    yjs_js_security_passport=ecd2384c6abeb7009a9454c709a948cacfb90fbb_1575196333_js;
    COOKIE_SESSION=79_1_9_9_18_25_0_0_9_7_114_0_0_0_134_125_1575190520_1575196936_1575196811%7C9%235599107_10_1575196811%7C4;
    shifen[9196833561_56029]=1575196813; BCLID=7419429757975099374;
    BDSFRCVID=mfKOJeC62688UlcwIvy3Mq6N9g7NlBOTH6ao8WMIEIkHvOCd8j-lEG0PtU8g0Ku-hD88ogKK3gOTH4PF_2uxOjjg8UtVJeC6EG0Ptf8g0M5;
    H_BDCLCKID_SF=JbAjoKK5tKvbfP0kh-QJhnQH-UnLqbjN3j7Z0lOnMp05e66CM6rf2R0d2fteJDut5554atJKQfcW8DO
    _e6Kbejj-DNKJqbbfb-oL3b5eb40_Hn7zeToEQx4pbt-qJfomBGOhbCTjJKL-Sq3Gef4KDlo35x5nBT5KabTX2hIXJMJNeP
    j85UvoyRKkQN3TBPKO5bRiLRomQUT1Dn3oyTbJXp0njb3ly5jtMgOBBJ0yQ4b4OR5JjxonDhOyyGCet5_tJn4OV-582R7_KJ
    O1MtbOq4IOqxby26nnfnReaJ5n0-nnhnc_bPTbXPAdhb79K5J3fg77_qjlyIoSjq8Ry6CKD553ea0fJjnH5Cn0QJ5JbTr5jJ6P-DTM
    -t4V-fPX5-RLfbTp_p7F5l8-hljF2fjr0R-9WxD8WR_qBJ5LbRv7QnOxOKQphToJjR8F245BqIrzWHLD-DnN3KJmjlC9bT3v5tDphfjj2-biWb
    7M2MbdJUJP_IoG2Mn8M4bb3qOpBtQmJeTxoUtbWDFKbKP9j5t5DTPLMfc02t-DHj60WRT85KP-b5rnhPF3LU-rXP6-35KHam_JWtPKbMcVh-To
    etcf3PugXfrJ5q37JD6y5xT92-oAfpjNjMRGBpDu0PoxJpOdBRbMopvaKJuWfIovbURvDP-g3-AJQU5dtjTO2bc_5KnlfMQ_bf--QfbQ0hOhqP-jBR
    IE_K8atKDKMIvT5tQ_M4F_qlQDetJyaR3L0qvvWJ5TMC_Ce57lh-tWyqj8JMrDJe_80K3kapocShPC-tPW3UuPQtrWBn590KPOBj6s3l02V-bae-t2
    ynQDM-8OqPRMW20e0h7mWIbmsxA45J7cM4IseboJLfT-0bc4KKJxbnLWeIJEjj6jK4JKDGLtqT5P; H_PS_PSSID=1434_21096_30211_30125_29700;
    ZD_ENTRY=baidu; H_PS_645EC=237aRS1b6HA8ybjpJ5R7K3c0%2Fq8X3D5WA3CveDxbN0B28Aegl%2Fc9qLimL94; BDSVRTM=0*/
    private Map<String, String> getCookies() {
        Map<String, String> cookies = new HashMap<>();
//        String url = "http://www.baidu.com.cn/s?wd=%E6%90%9C%E7%8B%97%E8%81%94%E7%9B%9F%E7%99%BB%E5%BD%95";
//        Response req = OkHttpUtils.getPcRel(url);
//        if (req != null) {
//            List<String> cookieList = req.headers().values("Set-Cookie");
//            Log.i(TAG, "cookieList = " + cookieList);
//            if (cookieList != null) {
//                for (String cookie : cookieList) {
//                    if (cookie.contains("SNUID=")) {
//                        cookies.put("SNUID", cookie.substring(6));
//                    } else if (cookie.contains("SUID=")) {
//                        cookies.put("SUID", cookie.substring(5));
//                    }
//                }
//            }
//        }
        cookies.put("BAIDUID", "B4182E17D5732E369C8084335DDFB608:FG=1");
        cookies.put("'BIDUPSID'", "B4182E17D5732E369C8084335DDFB608");
        return cookies;
    }

    public List<SearchModel.SearchBook> parseSearchKey(String key) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        mBookHosts.clear();
        List<SearchModel.SearchBook> bookList = new ArrayList<>();
        try {
            trustEveryone();
            key = URLEncoder.encode(key, "gb2312");
            String url = "http://www.baidu.com.cn/s?wd=" + key + "&cl=3";
//            Document document = Jsoup.parse(OkHttpUtils.getPcRel(url).body().string());
            Document document = Jsoup.connect(url).userAgent(UA).cookies(getCookies()).timeout(30000).get();
//            logi("document = " + document.html());
            Element body = document.body();
            Elements page = body.select("div#page");
            Elements a = page.select("a");
            List<String> pages = new ArrayList<>();
            if (a != null) {
                for (Element element : a) {
                    String link = element.attr("href");
                    if (link != null && link.startsWith("/s")) {
                        pages.add("http://www.baidu.com" + link);
                    }
                    if (pages.size() > 8) {
                        break;
                    }
                }
            }
            List<SearchModel.SearchBook> list1 = getBookListForDocument(document);
            if (list1 != null) {
                bookList.addAll(list1);
            }
            for (String pageUrl : pages) {
                if (mCancel) {
                    break;
                }
                Document pageDocument = Jsoup.connect(pageUrl).userAgent(UA).cookies(getCookies()).timeout(30000).get();
                List<SearchModel.SearchBook> list2 = getBookListForDocument(pageDocument);
                if (list2 != null) {
                    bookList.addAll(list2);
                }
//                    if (bookList.size() > 10 && mCurParseSize > 0) {
//                        break;
//                    }
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return bookList;
    }

    @Override
    public List<SearchModel.SearchBook> parseFirstPageHtml(String html) {
        mCurSize = 0;
        mCurParseSize = 0;
        mCancel = false;
        mBookHosts.clear();
        List<SearchModel.SearchBook> list = null;
        try {
            Document document = Jsoup.parse(html);
            Element body = document.body();
            Elements page = body.select("div#page");
            Elements a = page.select("a");
            if (mPageUrlList == null) {
                mPageUrlList = new ArrayList<>();
            } else {
                mPageUrlList.clear();
            }
            if (a != null) {
                for (Element element : a) {
                    String link = element.attr("href");
                    if (link != null && link.startsWith("/s")) {
                        mPageUrlList.add("http://www.baidu.com" + link);
                    }
                    if (mPageUrlList.size() > 8) {
                        break;
                    }
                    Log.i(TAG, "page: " + link);
                }
            }
            list = getBookListForDocument(document);
            if (mUrlCallback != null) {
                if (mPageUrlList.size() > 0 && !mCancel) {
                    String url = mPageUrlList.remove(0);
                    mUrlCallback.onNextUrl(url);
                } else {
                    mUrlCallback.onLoadEnd();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return list;
    }
}
