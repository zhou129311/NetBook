package com.xzhou.book.find;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.ThirdWebsiteHtmlParse;
import com.xzhou.book.net.HttpRequest;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.SPUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-5
 * Change List:
 */
public class ThirdViewModel extends AndroidViewModel {
    private static final String TAG = "ThirdViewModel";
    public final MutableLiveData<Entities.ThirdBookData> mBookData = new MutableLiveData<>();
    public final MutableLiveData<Entities.SupportBean> mBeanData = new MutableLiveData<>();
    public final MutableLiveData<String> mUrlData = new MutableLiveData<>();
    private final Map<String, Entities.SupportBean> mBeanMap = new HashMap<>();
    private final Map<String, HashMap<String, String>> mUrlParams = new HashMap<>();
    public String mRootUrl;
    private final ExecutorService mSinglePool = Executors.newSingleThreadExecutor();

    public ThirdViewModel(@NonNull Application application) {
        super(application);
    }

    public void reload() {
        if (TextUtils.isEmpty(mRootUrl)) {
            return;
        }
        startLoad(mRootUrl);
    }

    public void refreshUrl(String rootUrl) {
        startLoad(rootUrl);
    }

    public void refreshUrl(String rootUrl, String key, String value) {
        HashMap<String, String> paramsMap = mUrlParams.get(rootUrl);
        if (paramsMap == null) {
            paramsMap = new HashMap<>();
            mUrlParams.put(rootUrl, paramsMap);
        }
        Log.i(TAG, "refreshUrl :" + key + "=" + value);
        paramsMap.put(key, value);
        startLoad(rootUrl);
    }

    public void loadData(String name) {
        mSinglePool.execute(() -> {
            Entities.SupportBean supportBean = mBeanMap.get(name);
            if (supportBean != null) {
                mBeanData.postValue(supportBean); // update menu and title
                String url = supportBean.entry.get(supportBean.checkIndex).rootUrl;
                initPageParam(url);
                startLoad(url);
                return;
            }
            InputStream is = null;
            BufferedReader br = null;
            try {
                is = getApplication().getAssets().open(name + ".json");
                br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                supportBean = new Gson().fromJson(sb.toString(), Entities.SupportBean.TYPE);
                supportBean.checkIndex = SPUtils.get().getInt(supportBean.name, 0);
                mBeanMap.put(name, supportBean);
                for (Entities.SupportBeanEntry entry : supportBean.entry) {
                    HashMap<String, String> paramsMap = new HashMap<>();
                    mUrlParams.put(entry.rootUrl, paramsMap);
                    for (Entities.SupportBeanEntryParam param : entry.params) {
                        paramsMap.put(param.key, param.enums.get(0).value);
                    }
                    paramsMap.put(supportBean.pageKey, "1");
                }
                mBeanData.postValue(supportBean); // update menu and title
                startLoad(supportBean.entry.get(supportBean.checkIndex).rootUrl);
            } catch (IOException e) {
                Log.e(TAG, "loadData fail", e);
                mBookData.postValue(null);
            } finally {
                AppUtils.close(is);
                AppUtils.close(br);
            }
        });
    }

    public void initPageParam(String rootUrl) {
        HashMap<String, String> paramsMap = mUrlParams.get(rootUrl);
        if (paramsMap == null) {
            paramsMap = new HashMap<>();
            mUrlParams.put(rootUrl, paramsMap);
        }
        if (mBeanData.getValue() != null) {
            paramsMap.put(mBeanData.getValue().pageKey, "1");
        }
    }

    private void startLoad(String rootUrl) {
        mRootUrl = rootUrl;
        // parse url
        HashMap<String, String> paramsMap = mUrlParams.get(rootUrl);
        String url = HttpRequest.appendQueryUrl(paramsMap, rootUrl);
        mUrlData.postValue(url);
    }

    public void parseHtml(String url, String html) {
        mSinglePool.execute(() -> {
            Entities.ThirdBookData data = null;
            if (html != null) {
                String handlerHtml = handlerHtml(html);
                if (url.contains("readnovel")) {
                    data = ThirdWebsiteHtmlParse.xsydw(handlerHtml);
                } else if (url.contains("xxsy")) {
                    data = ThirdWebsiteHtmlParse.xxsy(handlerHtml);
                } else if (url.contains("hongxiu")) {
                    data = ThirdWebsiteHtmlParse.hxtx(handlerHtml);
                } else if (url.contains("xs8")) {
                    data = ThirdWebsiteHtmlParse.yqxsb(handlerHtml);
                } else if (url.contains("qidian")) {
                    data = ThirdWebsiteHtmlParse.qdzww(handlerHtml);
                }
            }
            mBookData.postValue(data);
        });
    }

    private String handlerHtml(String html) {
        String value = html;
        value = value.replace("\\u003C", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");
        value = value.replace("&amp;", "&");
        value = value.replace("\\\"", "\"");
        value = value.replace("\\n", "\n");
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
//        value = value.replace("\\t", "    ");
        value = "<html>" + value + "</html>";
        return value;
    }
}
