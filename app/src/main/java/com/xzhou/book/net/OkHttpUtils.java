package com.xzhou.book.net;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xzhou.book.MyApp;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {
    private static OkHttpClient sGetClient;
    private static final int CACHE_MAXAGE = 60; // s
    private static final int CACHE_SIZE = 10 * 1024 * 1024;

    private static OkHttpClient getClient() {
        if (sGetClient == null) {
            File httpCacheDirectory = new File(FileUtils.getCachePath(MyApp.getContext()), "okhttps");
            Cache cache = new Cache(httpCacheDirectory, CACHE_SIZE);
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addNetworkInterceptor(interceptor)
                    .readTimeout(6, TimeUnit.SECONDS)
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .cache(cache)
                    .cookieJar(new LocalCookieJar());
            addHttpAuthority(builder);
            sGetClient = builder.build();
        }
        return sGetClient;
    }

    private static Interceptor interceptor = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            String cacheControl = request.cacheControl().toString();
            if (TextUtils.isEmpty(cacheControl)) {
                cacheControl = "public, max-age=" + CACHE_MAXAGE;
            }
            return response.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build();
        }
    };

    private static class LocalCookieJar implements CookieJar {
        List<Cookie> cookies;

        @Override
        public List<Cookie> loadForRequest(HttpUrl arg0) {
            if (cookies != null)
                return cookies;
            return new ArrayList<>();
        }

        @Override
        public void saveFromResponse(HttpUrl arg0, List<Cookie> cookies) {
            this.cookies = cookies;
        }
    }

    private static void addHttpAuthority(OkHttpClient.Builder builder) {
        try {
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{new X509TrustManager() {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }};

    public static Response getPcRel(String url) {
        Request req = new Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .get()
                .build();
//        ResponseBody body = null;
        try {
            Response response = getClient().newCall(req).execute();
//            body = response.body();
            return response;
        } catch (Exception e) {
            Log.e("get url = " + url + "\nerror:", e);
        } finally {
//            if (body != null) {
//                body.close();
//            }
        }
        return null;
    }

    public static Object getObject(HttpRequest request, Type typeOfT, HashMap<String, String> params) {
        return getObject(request, typeOfT, params, true);
    }

    public static Object getObject(final HttpRequest request, final Type typeOfT, final HashMap<String, String> params, boolean hasCache) {
        String url = HttpRequest.appendQueryUrl(params, request.getTargetUrl());

        CacheControl cacheControl;
        if (!AppUtils.isNetworkAvailable()) {
            cacheControl = CacheControl.FORCE_CACHE;
        } else {
            cacheControl = new CacheControl.Builder()
                    .maxAge(CACHE_MAXAGE, TimeUnit.SECONDS)
                    .build();
        }
        if (!hasCache) {
            cacheControl = CacheControl.FORCE_NETWORK;
        }
        Request req = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .get()
                .build();
        ResponseBody body = null;
        try {
            Response response = getClient().newCall(req).execute();
            body = response.body();
            String bodys = body.string();
            if (!url.contains("chapter2.zhuishushenqi.com")) {
                logi("get url = " + url + "\nresponse =" + bodys);
            }
            return new Gson().fromJson(bodys, typeOfT);
        } catch (Exception e) {
            Log.e("get url = " + url + "\nerror:", e);
        } finally {
            if (body != null) {
                body.close();
            }
        }
        return null;
    }

    public static Object post(final HttpRequest request, final Type typeOfT, final String content) {
        String url = request.getTargetUrl();
        Request req = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .removeHeader("X-User-Agent")
                .post(RequestBody.create(MediaType.parse("application/json"), content))
                .build();
        ResponseBody body = null;
        try {
            Response response = getClient().newCall(req).execute();
            body = response.body();
            String bodys = body.string();
            logi("post url = " + url + ", content = " + content + "\nresponse =" + bodys);
            Object result = null;
            if (typeOfT != null) {
                result = new Gson().fromJson(bodys, typeOfT);
            }
            return result;
        } catch (Exception e) {
            Log.e("post", e);
        } finally {
            if (body != null) {
                body.close();
            }
        }
        return null;
    }

    public static void logi(String str) {
        logi("Http", str);
    }

    public static void logi(String tag, String str) {
        int max_str_length = 2001;
        while (str.length() > max_str_length) {
            Log.i(tag, str.substring(0, max_str_length));
            str = str.substring(max_str_length);
        }
        Log.i(tag, str);
    }
}
