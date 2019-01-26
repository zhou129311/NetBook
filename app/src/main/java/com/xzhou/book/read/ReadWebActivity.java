package com.xzhou.book.read;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.utils.Log;

import butterknife.BindView;

public class ReadWebActivity extends BaseActivity {

    @BindView(R.id.web_view)
    WebView mWebView;

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, ReadWebActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        String url = getIntent().getStringExtra("url");

        WebSettings webSettings = mWebView.getSettings();

        mWebView.clearCache(true);
        mWebView.clearHistory();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setUseWideViewPort(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.addJavascriptInterface(this, "android");
//        mWebView.requestFocus();
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i("onPageStarted: "+ url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("onPageFinished:" + url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.i("onReceivedSslError");
                handler.proceed();
//                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.i("onReceivedHttpError:" + errorResponse);
            }
        });
//        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mToolbar.setTitle(title);
            }
        });
        Log.i("loadUrl:" + url);
        mWebView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mWebView.reload();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
