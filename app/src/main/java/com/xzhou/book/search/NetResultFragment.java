package com.xzhou.book.search;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.MyApp;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.JsoupSearch;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;
import com.xzhou.book.widget.LoadingButton;

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
    //    private View mLoadView;
    private View mHeaderView;
    private TextView mHeaderTv;

    private String mKey;
    private int mSearchType;
    private Adapter mAdapter;
    private String mSearchUrl;
    private boolean mIsAddHeader;

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
//        mLoadView = inflater.inflate(R.layout.baidu_search_loading_view, null);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mHeaderView = inflater.inflate(R.layout.header_search_loading, null);
        mHeaderTv = mHeaderView.findViewById(R.id.search_load_tv);
        mHeaderView.findViewById(R.id.stop_search_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeaderTv.setText("正在结束搜索...");
                mPresenter.stopParse();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUrl();
    }

    public boolean onBackPressed() {
        final List<SearchModel.SearchBook> list = mAdapter.getData();
        if (list.size() > 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            Dialog dialog = builder.setTitle("是否保存该搜索结果？")
                    .setMessage("保存后再次搜索该书名时可以直接显示该结果。")
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AppSettings.saveSearchList(mKey, list);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    })
                    .create();
            dialog.show();
            return true;
        } else {
            mPresenter.cancel();
            return false;
        }
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
            if (mAdapter.getData().size() < 1) {
                List<SearchModel.SearchBook> old = AppSettings.getSearchList(mKey);
                if (old != null && old.size() > 0) {
                    mAdapter.setNewData(old);
                    return;
                }
            }
            String key;
            try {
                key = URLEncoder.encode(mKey, "gb2312");
            } catch (UnsupportedEncodingException ignored) {
                key = mKey;
            }
            if (mSearchType == SEARCH_TYPE_BAIDU) {
                mSearchUrl = "http://www.baidu.com/s?wd=" + key + "&cl=3";
            } else {
                mSearchUrl = "https://www.sogou.com/web?query=" + key;
            }
            Log.i(TAG, "mSearchUrl = " + mSearchUrl);
            if (mSearchUrl.equals(mWebView.getUrl())) {
                mWebView.reload();
            } else {
                mWebView.loadUrl(mSearchUrl);
            }
            showSearchLoading();
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
                        Log.i(TAG, "onReceiveValue: startSearch");
                    } else {
                        mPresenter.updateHtml(value);
                        Log.i(TAG, "onReceiveValue: updateHtml ");
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
            hideSearchLoading();
        }
    };

    @Override
    public void onUrlLoad(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void onLoadingState(boolean loading) {
        if (!loading) {
            mAdapter.setEmptyView(mEmptyView);
//            mRecyclerView.setVisibility(View.VISIBLE);
//            mWebView.setVisibility(View.GONE);
            hideSearchLoading();
        } else {
//            mWebView.setVisibility(View.VISIBLE);
//            mRecyclerView.setVisibility(View.GONE);
            mHeaderTv.setText("全网搜索耗时较长，请耐心等待，当前正在全网搜索...");
            showSearchLoading();
//            mAdapter.setEmptyView(mLoadView);
        }
    }

    @Override
    public void onSearchProgress(int bookSize, int parseSize, String curHost) {
        if (mHeaderView != null) {
            Spanned str = Html.fromHtml("已搜索到<b><font color=#ff0000>" + bookSize
                    + "本</font></b>相关书籍<br />已解析<b><font color=#ff0000>"
                    + parseSize + "本</font></b>书籍<br />正在解析网站：" + curHost);
            mHeaderTv.setText(str);
        }
    }

    @Override
    public void onDataChange(List<SearchModel.SearchBook> list) {
        mAdapter.setNewData(list);
    }

    @Override
    public void onDataAdd(List<SearchModel.SearchBook> list) {
        if (list != null) {
            if (mAdapter.getData().size() < 1) {
                mAdapter.setNewData(list);
            } else {
                mAdapter.addData(list);
            }
        }
    }

    @Override
    public void onParseState(SearchModel.SearchBook searchBook) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(NetSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void showSearchLoading() {
        if (mIsAddHeader) {
            return;
        }
        mIsAddHeader = true;
        mAdapter.addHeaderView(mHeaderView);
    }

    private void hideSearchLoading() {
        if (!mIsAddHeader) {
            return;
        }
        mIsAddHeader = false;
        mAdapter.removeHeaderView(mHeaderView);
    }

    private class Adapter extends BaseQuickAdapter<SearchModel.SearchBook, CommonViewHolder> {
        private final String[] DIALOG_ITEM = new String[]{
                "加入书架"/*, "尝试自动解析"*/
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
            boolean support = SearchModel.hasSupportLocalRead(item.sourceHost);
            holder.setRoundImageUrl(R.id.book_image, item.image, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.bookName)
                    .setText(R.id.book_h2, sub)
                    .setGone(R.id.local_read_tv, support);

            LoadingButton button = holder.getView(R.id.auto_parse_btn);
            button.setVisibility(support ? View.GONE : View.VISIBLE);
            button.initText("解析中...", item.parseText);
            button.setLoading(item.isParsing);
            button.setOnClickListener(v -> mPresenter.tryParseBook(item));
            holder.itemView.setOnClickListener(v -> {
                BookProvider.LocalBook localBook = new BookProvider.LocalBook(item);
                if (SearchModel.hasSupportLocalRead(item.sourceHost)) {
                    ReadActivity.startActivity(getActivity(), localBook);
                } else {
                    ReadWebActivity.startActivity(getActivity(), localBook, null);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                boolean hasCache = BookProvider.hasCacheData(item.id);
                if (hasCache) {
                    ToastUtils.showShortToast("已经加入书架了");
                    return true;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("加入书架")
                        .setMessage("是否将《" + item.bookName + "》加入书架")
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            dialog.dismiss();
                            BookProvider.insertOrUpdate(new BookProvider.LocalBook(item), false);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
                return true;
            });
        }
    }
}
