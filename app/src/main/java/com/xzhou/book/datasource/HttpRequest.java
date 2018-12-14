package com.xzhou.book.datasource;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public String mUrl;
    public String[] mPath;

    public HttpRequest(String url) {
        mUrl = url;
    }

    public HttpRequest(String url, String... path) {
        mUrl = url;
        mPath = path;
    }

    public String getTargetUrl() {
        if (TextUtils.isEmpty(mUrl)) {
            throw new NullPointerException("mUrl Can't be empty");
        }

        StringBuilder sb;
        if (!mUrl.startsWith("http")) {
            sb = new StringBuilder(ZhuiShuSQApi.API_BASE_URL);
        } else {
            sb = new StringBuilder();
        }
        if (!TextUtils.isEmpty(mUrl)) {
            sb.append(mUrl);
        }

        if (mPath != null && mPath.length > 0) {
            for (String path : mPath) {
                if (!TextUtils.isEmpty(path)) {
                    String encode = null;
                    if(path.startsWith("http://")) {
                        try {
                            encode = URLEncoder.encode(path, "UTF-8");
                        } catch (UnsupportedEncodingException ignored) {
                        }
                    }
                    sb.append("/").append(encode != null ? encode : path);
                }
            }
        }
        return sb.toString();
    }

    public static String appendQueryUrl(HashMap<String, String> params, String url) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        boolean isFirstParam = true;
        StringBuilder sb = new StringBuilder(url);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!isFirstParam) {
                sb.append('&');
            } else {
                sb.append("?");
                isFirstParam = false;
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
