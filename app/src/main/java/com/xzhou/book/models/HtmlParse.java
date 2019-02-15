package com.xzhou.book.models;

import android.annotation.SuppressLint;

import com.xzhou.book.utils.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public abstract class HtmlParse {
    protected String TAG = "HtmlParse";

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

    protected String subFirstDiv(Elements content) {
        String text = content.toString();
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
        return text;
    }

    protected String replaceCommon(String text) {
        text = text.replace("\n", "");
        text = text.replace("<br>", "\n");
        text = text.replace("&nbsp;", "");
        text = text.replace(" ", "");
        text = text.replace("　", "");
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
            int link1 = getLinkIndex(o1.link);
            int link2 = getLinkIndex(o2.link);
            if (link1 > link2) {
                return 1;
            } else if (link1 == link2) {
                return 0;
            } else {
                return -1;
            }
        }
    };

    public static int getLinkIndex(String link) {
        String last1 = link.substring(link.lastIndexOf("/") + 1);
        int i = last1.lastIndexOf(".");
        if (i > 0) {
            return Integer.parseInt(last1.substring(0, i));
        }
        return 0;
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
