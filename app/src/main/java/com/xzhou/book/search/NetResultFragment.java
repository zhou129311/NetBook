package com.xzhou.book.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.JsoupSearch;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import butterknife.BindView;

import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_KEY;
import static com.xzhou.book.search.SearchActivity.EXTRA_SEARCH_TYPE;
import static com.xzhou.book.search.SearchActivity.SEARCH_TYPE_BAIDU;

/**
 * packageName：com.xzhou.book.search
 * ProjectName：NetBook
 * Description：
 * Author：zhouxian
 * Date：2019/12/21 19:04
 */
public class NetResultFragment extends BaseFragment<NetSearchContract.Presenter> implements NetSearchContract.View {
    private static final String TAG = "NetResultFragment";

    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private View mEmptyView;
    private View mLoadView;

    private String mKey;
    private int mSearchType;
    private Adapter mAdapter;
    private android.app.AlertDialog mLoadingDialog;
    private android.app.AlertDialog mParsingDialog;
    private String mSearchUrl;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_search_net_result;
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (TextUtils.isEmpty(mKey)) {
                mKey = bundle.getString(EXTRA_SEARCH_KEY, "");
            }
            mSearchType = bundle.getInt(EXTRA_SEARCH_TYPE, SEARCH_TYPE_BAIDU);
        }
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUserAgentString(JsoupSearch.UA);
        String dir = MyApp.getContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(dir);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mEmptyView = inflater.inflate(R.layout.common_empty_view, null);
        mLoadView = inflater.inflate(R.layout.baidu_search_loading_view, null);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUrl();
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


    private void loadUrl() {
        Log.i(TAG, "loadUrl = " + mKey);
        if (!TextUtils.isEmpty(mKey)) {
            if (mSearchType == SEARCH_TYPE_BAIDU) {
                String key;
                try {
                    key = URLEncoder.encode(mKey, "gb2312");
                } catch (UnsupportedEncodingException ignored) {
                    key = mKey;
                }
                mSearchUrl = "http://www.baidu.com/s?wd=" + key + "&cl=3";
            } else {
                mSearchUrl = "https://www.sogou.com/web?query=" + mKey;
            }
            Log.i(TAG, "mSearchUrl = " + mSearchUrl);
            mWebView.loadUrl(mSearchUrl);
        }
    }

    public void search(String key, int searchType, boolean force) {
        Log.i(TAG, "search key = " + key + ", mKey = " + mKey);
        if (!force && TextUtils.equals(key, mKey) && searchType == mSearchType) {
            return;
        }
        mSearchType = searchType;
        mKey = key;
        if (!isAdded() || isDetached()) {
            return;
        }
        loadUrl();
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (!isAdded() || isDetached()) {
                return;
            }
//            mProgress.setProgress(progress);
//            if (progress == 100) {
//                mProgress.setVisibility(View.GONE);
//                mSwipeContainer.setRefreshing(false);
//            } else {
//                mProgress.setVisibility(View.VISIBLE);
//            }
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            if (!request.getUrl().toString().contains(mHost)) { //过滤广告链接
//                return new WebResourceResponse(null, null, null);
//            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            Log.i(TAG, "onPageFinished:" + url);
//            view.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
            view.evaluateJavascript("document.getElementsByTagName('html')[0].innerHTML", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if (mSearchUrl != null && mSearchUrl.equals(url)) {
                        mPresenter.startSearch(mSearchType, value);
                    } else {
                        mPresenter.updateHtml(value);
                    }
                }
            });
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

    @Override
    public void onUrlLoad(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void onLoadingState(boolean loading) {
        if (!loading) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
            }
            mAdapter.setEmptyView(mEmptyView);
        } else {
            mWebView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            showLoadingDialog();
            mAdapter.setEmptyView(mLoadView);
        }
    }

    @Override
    public void onSearchProgress(int bookSize, int parseSize, String curHost) {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            Spanned str = Html.fromHtml("已搜索到<b><font color=#ff0000>" + bookSize
                    + "本</font></b>相关书籍<br />已解析<b><font color=#ff0000>"
                    + parseSize + "本</font></b>书籍<br />正在解析网站：" + curHost);
            mLoadingDialog.setMessage(str);
        }
    }

    @Override
    public void onDataChange(List<SearchModel.SearchBook> list) {
        mAdapter.setNewData(list);
    }

    @Override
    public void onDataAdd(List<SearchModel.SearchBook> list) {
        if (list != null) {
            if (mAdapter.getItemCount() == 0) {
                mAdapter.setNewData(list);
            } else {
                mAdapter.addData(list);
            }
        }
    }

    @Override
    public void onParseState(boolean parsing, boolean success, String message) {
        if (parsing) {
            showParsingDialog(message);
        } else {
            if (mParsingDialog != null) {
                mParsingDialog.dismiss();
                mParsingDialog = null;
            }
            if (success) {
                mAdapter.notifyDataSetChanged();
            }
            ToastUtils.showShortToast(message);
        }
    }

    @Override
    public void setPresenter(NetSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void showParsingDialog(String title) {
        if (mParsingDialog != null && mParsingDialog.isShowing()) {
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        mParsingDialog = builder.setTitle(title)
                .setMessage("正在解析中...")
                .setPositiveButton("结束解析", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mPresenter.stopParse();
                    }
                }).create();
        mParsingDialog.setCanceledOnTouchOutside(false);
        mParsingDialog.setCancelable(false);
        mParsingDialog.show();
    }

    private void showLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        mLoadingDialog = builder.setTitle("全网搜索耗时较长，请耐心等待...")
                .setMessage("正在全网搜索中")
                .setPositiveButton("结束搜索", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.cancel();
                        mLoadingDialog.setMessage("正在结束搜索...");
                        mAdapter.setEmptyView(mLoadView);
                    }
                }).create();
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    private class Adapter extends BaseQuickAdapter<SearchModel.SearchBook, CommonViewHolder> {
        private final String[] DIALOG_ITEM = new String[]{
                "加入书架", "尝试自动解析"
        };

        private Adapter() {
            super(R.layout.item_view_search_result);
        }

        @Override
        protected void convert(final CommonViewHolder holder, final SearchModel.SearchBook item) {
            String sub = (TextUtils.isEmpty(item.sourceName) ? item.sourceHost : item.sourceName + " | " + item.sourceHost);
            if (!TextUtils.isEmpty(item.author)) {
                sub = item.author + " | " + sub;
            }
            holder.setRoundImageUrl(R.id.book_image, item.image, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.bookName)
                    .setText(R.id.book_h2, sub)
                    .setGone(R.id.local_read_tv, SearchModel.hasSupportLocalRead(item.sourceHost));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookProvider.LocalBook localBook = new BookProvider.LocalBook(item);
                    if (SearchModel.hasSupportLocalRead(item.sourceHost)) {
                        ReadActivity.startActivity(getActivity(), localBook);
                    } else {
                        ReadWebActivity.startActivity(getActivity(), localBook, null);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean hasCache = BookProvider.hasCacheData(item.id);
                    if (hasCache && SearchModel.hasSupportLocalRead(item.sourceHost)) {
                        ToastUtils.showShortToast("已经加入书架了");
                        return true;
                    }
                    if (!hasCache) {
                        ItemDialog.Builder builder = new ItemDialog.Builder(mContext);
                        builder.setTitle(item.bookName).setItems(DIALOG_ITEM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (which == 0) {
                                    BookProvider.insertOrUpdate(new BookProvider.LocalBook(item), false);
                                } else {
                                    mPresenter.tryParseBook(item);
                                }
                            }
                        }).show();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("是否尝试自动解析《" + item.bookName + "》(" + item.sourceHost + ")?")
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mPresenter.tryParseBook(item);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                    return true;
                }
            });
        }
    }
}
