package com.xzhou.book.models;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public abstract class HtmlParse {
    protected String TAG = "HtmlParse";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";

    private static final Pattern PATTERN1 = Pattern.compile("<div(.*?)>");
    private static final Pattern PATTERN2 = Pattern.compile("<fon(.*?)>");
    private static final Pattern PATTERN3 = Pattern.compile("<p(.*?)>");
    private static final Pattern PATTERN4 = Pattern.compile("<!--(.*?)-->");
    private static final List<String> E_TAGS = new ArrayList<String>() {
        {
            add("div.kongwen");
            add("div.readmiddle");
            add("div.bottem");
            add("div.con_l");
            add("div#stsm");
            add("div#ali");
            add("a");
            add("b");
            //add("p");
            //add("fon");
            add("font");
            add("strong");
            add("script");
            add("h1");
        }
    };

    private static final List<String> COMPARATOR2 = new ArrayList<String>() {
        {
            add("www.cdzdgw.com");
            add("www.xszww.com");
            add("www.biquge.lu");
        }
    };

    public List<Entities.Chapters> parseChapters(String readUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(readUrl).userAgent(USER_AGENT).timeout(10000).get();
            if (readUrl.endsWith("index.html")) {
                readUrl = readUrl.replace("index.html", "");
            }
            if (readUrl.endsWith("list/")) {
                readUrl = readUrl.replace("list/", "");
            }
            return parseChapters(readUrl, document);
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return null;
    }

    public abstract List<Entities.Chapters> parseChapters(String readUrl, Document document);

    public Entities.ChapterRead parseChapterRead(String chapterUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(chapterUrl).userAgent(USER_AGENT).timeout(10000).get();
            Log.i(TAG, "parseChapterRead:baseUri=" + document.baseUri());
            Entities.ChapterRead read = parseChapterRead(chapterUrl, document);
            if (read != null && read.chapter != null && read.chapter.body != null && TextUtils.isEmpty(read.chapter.body.trim())) {
                return null;
            }
            return read;
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return null;
    }

    public abstract Entities.ChapterRead parseChapterRead(String chapterUrl, Document document);

    List<Entities.Chapters> sortAndRemoveDuplicate(List<Entities.Chapters> list, String host) {
        Comparator<Entities.Chapters> comparator;
        if (COMPARATOR2.contains(host)) {
            comparator = sComparator2;
        } else {
            comparator = sComparator1;
        }
        if (list.size() > 0) {
            try {
                Set<Entities.Chapters> s = new TreeSet<>(comparator);
                s.addAll(list);
                return new ArrayList<>(s);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
        return list;
    }

    protected String formatContent(Elements content) {
        return formatContent("", content);
    }

    protected String formatContent(String chapterUrl, Elements content) {
        if (content == null || content.isEmpty()) {
            Log.i(TAG, "formatContent:content = null");
            return "";
        }
        if (!chapterUrl.contains("www.35xs.com")
                && !chapterUrl.contains("www.f96.la")
                && !chapterUrl.contains("www.tuhaoxs.com")
                && !chapterUrl.contains("www.1dwx.com")) {
            removeContentTag(content);
        }
        String text = content.toString();
        Log.i(TAG, "formatContent:text = " + text);

        Matcher m4 = PATTERN4.matcher(text);
        while (m4.find()) {
            text = text.replace(m4.group(), "");
        }

        Matcher m1 = PATTERN1.matcher(text);
        while (m1.find()) {
            text = text.replace(m1.group(), "");
        }

        Matcher m2 = PATTERN2.matcher(text);
        while (m2.find()) {
            text = text.replace(m2.group(), "");
        }

        if (!chapterUrl.contains("www.35xs.com")
                && !chapterUrl.contains("www.1dwx.com")
                && !chapterUrl.contains("www.tuhaoxs.com")
                && !chapterUrl.contains("www.f96.la")) {
            Matcher m3 = PATTERN3.matcher(text);
            while (m3.find()) {
                text = text.replace(m3.group(), "");
            }
        }
        text = text.replace("</div>", "");
        text = text.replace("</fon>", "");
        text = text.replace("<span>", "");
        text = text.replace("</span>", "");
        text = text.replace("<center>", "");
        text = text.replace("</center>", "");
        text = text.replace("&amp;", "&");
        text = text.replace("amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("lt;", "<");
        text = text.replace("gt;", ">");
        return text;
    }

    private void removeContentTag(Elements content) {
        for (String tag : E_TAGS) {
            content.select(tag).remove();
        }
    }

    String replaceCommon(String text) {
        text = text.replace("\n", "");
        text = text.replace("<br>", "\n");
        text = text.replace("<p>", "\n");
        text = text.replace("</p>", "\n");
        text = text.replace("&nbsp;", "");
        text = text.replace(" ", "");
        text = text.replace("　", "");
        if (text.startsWith("\n")) {
            text = text.substring(1);
        }
        return text;
    }

    void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private static Comparator<Entities.Chapters> sComparator1 = new Comparator<Entities.Chapters>() {
        @Override
        public int compare(Entities.Chapters o1, Entities.Chapters o2) {
            String link1 = getLinkIndex(o1.link);
            String link2 = getLinkIndex(o2.link);
            int rel;
            try {
                int int1 = Integer.valueOf(link1);
                int int2 = Integer.valueOf(link2);
                return int1 - int2;
            } catch (NumberFormatException ignored) {
            }
            if (link1.length() > link2.length()) {
                rel = 1;
            } else if (link1.length() < link2.length()) {
                rel = -1;
            } else {
                rel = link1.compareTo(link2);
            }
            return rel;
        }
    };

    private static Comparator<Entities.Chapters> sComparator2 = new Comparator<Entities.Chapters>() {
        @Override
        public int compare(Entities.Chapters o1, Entities.Chapters o2) {
            String link1 = getLinkIndex(o1.link);
            String link2 = getLinkIndex(o2.link);
            int rel;
            try {
                int int1 = Integer.valueOf(link1);
                int int2 = Integer.valueOf(link2);
                return int2 - int1;
            } catch (NumberFormatException ignored) {
            }
            if (link1.length() > link2.length()) {
                rel = -1;
            } else if (link1.length() < link2.length()) {
                rel = 1;
            } else {
                rel = link2.compareTo(link1);
            }
            return rel;
        }
    };

    private static String getLinkIndex(String link) {
        String last1 = link.substring(link.lastIndexOf("/") + 1);
        int i = last1.lastIndexOf(".");
        if (i > 0) {
            return last1.substring(0, i);
        }
        return link;
    }

    protected void logd(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.d(TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.d(TAG, str);
    }

    void logi(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(TAG, str);
    }
}
