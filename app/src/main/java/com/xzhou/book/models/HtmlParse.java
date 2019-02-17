package com.xzhou.book.models;

import android.annotation.SuppressLint;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public abstract class HtmlParse {
    protected String TAG = "HtmlParse";

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
            add("p");
            //add("fon");
            add("font");
            add("strong");
            add("script");
        }
    };

    public List<Entities.Chapters> parseChapters(String readUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(readUrl).timeout(10000).get();
            return parseChapters(readUrl, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract List<Entities.Chapters> parseChapters(String readUrl, Document document);

    public Entities.ChapterRead parseChapterRead(String chapterUrl) {
        try {
            trustEveryone();
            Document document = Jsoup.connect(chapterUrl).timeout(10000).get();
            return parseChapterRead(chapterUrl, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract Entities.ChapterRead parseChapterRead(String chapterUrl, Document document);

    protected List<Entities.Chapters> sortAndRemoveDuplicate(List<Entities.Chapters> list) {
        if (list.size() > 0) {
            try {
                Set<Entities.Chapters> s = new TreeSet<>(sComparator);
                s.addAll(list);
                return new ArrayList<>(s);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
        return null;
    }

    protected String formatContent(Elements content) {
        removeContentTag(content);

        String text = content.toString();
        Log.i(TAG, "formatContent:text = " + text);
        int divIndexStart = text.indexOf("<div");
        int divIndexEnd = text.indexOf(">");
        if (divIndexEnd > divIndexStart && divIndexStart >= 0) {
            text = text.substring(divIndexEnd + 1);
        }
        int fonIndexStart = text.indexOf("<fon");
        int fonIndexEnd = text.indexOf(">");
        if (fonIndexEnd > fonIndexStart && fonIndexStart >= 0) {
            text = text.substring(fonIndexEnd + 1);
        }
        int pIndexStart = text.indexOf("<p");
        int pIndexEnd = text.indexOf(">");
        if (pIndexEnd > pIndexStart && pIndexStart >= 0) {
            text = text.substring(pIndexEnd + 1);
        }
        text = text.replace("</div>", "");
        text = text.replace("</fon>", "");
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

    protected String replaceCommon(String text) {
        text = text.replace("\n", "");
        text = text.replace("<br>", "\n");
        text = text.replace("&nbsp;", "");
        text = text.replace(" ", "");
        text = text.replace("　", "");
        if (text.startsWith("\n")) {
            text = text.substring(1);
        }
        return text;
    }

    protected void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public static Comparator<Entities.Chapters> sComparator = new Comparator<Entities.Chapters>() {
        @Override
        public int compare(Entities.Chapters o1, Entities.Chapters o2) {
            String link1 = getLinkIndex(o1.link);
            String link2 = getLinkIndex(o2.link);
            return link1.compareTo(link2);
        }
    };

    public static String getLinkIndex(String link) {
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

    protected void logi(String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(TAG, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(TAG, str);
    }
}
