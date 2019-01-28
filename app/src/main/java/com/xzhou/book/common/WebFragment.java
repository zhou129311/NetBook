package com.xzhou.book.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;

import java.lang.ref.WeakReference;

import butterknife.BindView;

public class WebFragment extends BaseFragment {
    private static final String TAG = "WebFragment";

    @BindView(R.id.pb_progress)
    ProgressBar mProgress;
    @BindView(R.id.fl_webview)
    WebView mWebView;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeContainer;

    private MyHandler mHandler;

    private boolean isFinish;

    private static class MyHandler extends Handler {
        WeakReference<WebFragment> mFragment;

        MyHandler(WebFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            WebFragment fragment = mFragment.get();
            if (fragment == null || !fragment.isAdded() || fragment.isDetached()) {
                return;
            }
            String js = getClearAdDivJs(MyApp.getContext());
            Log.d(TAG, "getClearAdDivJs:" + js);
            fragment.mWebView.loadUrl(js);
            if (!fragment.isFinish) {
                sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_webview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new MyHandler(this);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        String dir = getActivity().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(dir);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

        mSwipeContainer.setColorSchemeColors(AppUtils.getColor(R.color.colorPrimary));
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.loadUrl(mWebView.getUrl());
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String url = bundle.getString("url");
            loadUrl(url);
        }
    }

    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public void loadUrl(final String url) {
        if (url == null) {
            return;
        }
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(url);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int progress) {
            mProgress.setProgress(progress);
            if (progress == 100) {
                mProgress.setVisibility(View.GONE);
                mSwipeContainer.setRefreshing(false);
            } else {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Activity activity = getActivity();
            if (activity instanceof ReadWebActivity) {
                ((ReadWebActivity) getActivity()).setTitle(title);
            }
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, "onPageStarted: " + url);
            isFinish = false;
            mHandler.sendEmptyMessage(1);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i(TAG, "onPageFinished:" + url);
            isFinish = true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                    || error.getPrimaryError() == SslError.SSL_EXPIRED
                    || error.getPrimaryError() == SslError.SSL_INVALID
                    || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                handler.proceed();
            } else {
                handler.cancel();
            }
        }
    };

    private class MyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    public static String getClearAdDivJs(Context context) {
        StringBuilder js = new StringBuilder("javascript:");
        Resources res = context.getResources();
        String[] adDivs = res.getStringArray(R.array.adBlockDiv);
        for (int i = 0; i < adDivs.length; i++) {
            js.append("var adDiv").append(i).append("= document.getElementById('").append(adDivs[i])
                    .append("');if(adDiv").append(i).append(" != null)adDiv").append(i)
                    .append(".parentNode.removeChild(adDiv").append(i).append(");");
        }
        js.append("var script = document.getElementsByTag('script'); document.body.removeChild(script);");
        return js.toString();
    }
}
