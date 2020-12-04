package com.xzhou.book.find;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.SPUtils;
import com.xzhou.book.widget.SingleCheckGroup;

import java.util.List;

import butterknife.BindView;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-4
 * Change List:
 */
public class ThirdWebsiteActivity extends BaseActivity {
    private static final String TAG = "ThirdWebsiteActivity";
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.filter_content_view)
    LinearLayout mContentView;
    @BindView(R.id.end_layout)
    ConstraintLayout mEndDraView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.radio_group)
    RadioGroup mRadioGroup;
    @BindView(R.id.radio_btn1)
    RadioButton mRadioBtn1;
    @BindView(R.id.radio_btn2)
    RadioButton mRadioBtn2;
    //    @BindView(R.id.web_view)
//    WebView mWebView;
    private ThirdViewModel mViewModel;
    //    private ListPopupWindow mListPopupWindow;
    private String mCheckedName;
    private Adapter mAdapter;
    private LayoutInflater mLayoutInflater;
    private View mEmptyView;

//    private final String[] WEBSITE_NAME = {
//            "起点中文网", "小说阅读网", "红袖添香", "潇湘书院", "言情小说吧"
//    };

    public static void startActivity(Activity context, String name) {
        Intent intent = new Intent(context, ThirdWebsiteActivity.class);
        intent.putExtra("website_name", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_website);
        mCheckedName = getIntent().getStringExtra("website_name");
        if (TextUtils.isEmpty(mCheckedName)) {
            finish();
        }
//        mCheckedName = SPUtils.get().getString("web_site_name", "起点中文网");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider
                .AndroidViewModelFactory(getApplication())).get(ThirdViewModel.class);
        mToolbar.setTitle(mCheckedName);
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mLayoutInflater = LayoutInflater.from(this);
        mEmptyView = mLayoutInflater.inflate(R.layout.common_load_error_view, null);
        mEmptyView.setOnClickListener(v -> {
            mEmptyView.setVisibility(View.GONE);
            mViewModel.reload();
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mEmptyView.setVisibility(View.GONE);
            mViewModel.reload();
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new LineItemDecoration());
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        mAdapter = new Adapter();
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.setEmptyView(mEmptyView);
        mAdapter.setOnLoadMoreListener(() -> {
            Entities.SupportBean supportBean = mViewModel.mBeanData.getValue();
            if (supportBean == null) {
                mAdapter.loadMoreFail();
                return;
            }
            Entities.ThirdBookData data = mViewModel.mBookData.getValue();
            if (data == null) {
                mAdapter.loadMoreFail();
                return;
            }
            String pageKey = supportBean.pageKey;
            int pageCurrent = data.pageCurrent;
            if (data.pageCurrent < data.pageCount) {
                pageCurrent += 1;
            }
            Log.i(TAG, "start loadMore");
            mViewModel.refreshUrl(mViewModel.mRootUrl, pageKey, String.valueOf(pageCurrent), true);
        }, mRecyclerView);
        mViewModel.mBeanData.observe(this, supportBean -> {
            if (supportBean == null || supportBean.entry.size() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mToolbar.setTitle("未知");
                mAdapter.setNewData(null);
            } else {
                mToolbar.setTitle(supportBean.name);
                Entities.SupportBeanEntry defaultEntry = supportBean.entry.get(0);
                if (supportBean.entry.size() < 2) {
                    mRadioBtn2.setVisibility(View.GONE);
                } else {
                    mRadioBtn2.setVisibility(View.VISIBLE);
                    mRadioBtn2.setText(supportBean.entry.get(1).gender);
                }
                mRadioBtn1.setText(defaultEntry.gender);
                if (supportBean.checkIndex == 0) {
                    mRadioBtn1.setChecked(true);
                    updateFilterContent(defaultEntry.rootUrl, defaultEntry.params);
                } else {
                    mRadioBtn2.setChecked(true);
                    updateFilterContent(supportBean.entry.get(1).rootUrl, supportBean.entry.get(1).params);
                }
            }
        });
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Entities.SupportBean supportBean = mViewModel.mBeanData.getValue();
            if (supportBean == null) {
                return;
            }
            int checkIndex = 0;
            if (checkedId == R.id.radio_btn2) {
                checkIndex = 1;
            }
            SPUtils.get().putInt(supportBean.name, checkIndex);
            Entities.SupportBeanEntry entry = supportBean.entry.get(checkIndex);
            updateFilterContent(entry.rootUrl, entry.params);
            mViewModel.refreshUrl(entry.rootUrl);
        });

        mViewModel.mBookData.observe(this, data -> {
            Log.i(TAG, "thirdBookData = " + data);
            if (data.isLoadMore()) {
                if (data.list != null) {
                    mAdapter.addData(data.list);
                }
                if (data.pageCurrent > data.pageCount) {
                    mAdapter.loadMoreFail();
                } else if (data.pageCurrent == data.pageCount) {
                    mAdapter.loadMoreEnd();
                } else {
                    mAdapter.loadMoreComplete();
                }
            } else {
                mAdapter.setNewData(data.list);
            }
            if (mAdapter.getData().size() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        });
        mViewModel.mRefreshData.observe(this, aBoolean -> {
            mSwipeRefreshLayout.setRefreshing(aBoolean);
            mAdapter.setEnableLoadMore(!aBoolean);
        });
        mViewModel.loadData(mCheckedName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_website, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.menu_website_source) {
            showSearchTypeWindow();
        } else */
        if (id == R.id.action_filtrate) {
            if (!mDrawerLayout.isDrawerOpen(mEndDraView)) {
                mDrawerLayout.openDrawer(mEndDraView);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mWebView != null) {
//            mWebView.stopLoading();
//            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//            mWebView.clearHistory();
//            mWebView.removeAllViews();
//            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
//            mWebView.destroy();
//        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mEndDraView)) {
            mDrawerLayout.closeDrawer(mEndDraView);
        } else {
            super.onBackPressed();
        }
    }

//    private void showSearchTypeWindow() {
//        if (mListPopupWindow == null) {
//            mListPopupWindow = new ListPopupWindow(this);
//            List<String> list = new ArrayList<>();
//            Collections.addAll(list, WEBSITE_NAME);
//            final FiltrateAdapter adapter = new FiltrateAdapter(this, list);
//            mListPopupWindow.setAdapter(adapter);
//            mListPopupWindow.setWidth((int) (AppUtils.getScreenWidth() / 2.6f));
//            mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//            mListPopupWindow.setAnchorView(findViewById(R.id.menu_website_source));
//            mListPopupWindow.setHorizontalOffset(AppUtils.dip2px(-16));//相对锚点偏移值，正值表示向右偏移
//            mListPopupWindow.setVerticalOffset(AppUtils.dip2px(-5));//相对锚点偏移值，正值表示向下偏移
//            mListPopupWindow.setDropDownGravity(Gravity.BOTTOM);
//            mListPopupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_dialog_common, null));
//            mListPopupWindow.setModal(true);//模态框，设置为true响应物理键
//            adapter.setChecked(list.indexOf(mCheckedName));
//            mListPopupWindow.setOnItemClickListener((adapterView, view, i, l) -> {
//                if (i == list.indexOf(mCheckedName)) {
//                    return;
//                }
//                adapter.setChecked(i);
//                mCheckedName = adapter.getItem(i);
//                SPUtils.get().putString("web_site_name", mCheckedName);
//                mListPopupWindow.dismiss();
//                mViewModel.loadData(mCheckedName);
//            });
//        }
//        mListPopupWindow.show();
//    }

    private void updateFilterContent(String rootUrl, List<Entities.SupportBeanEntryParam> params) {
        int childCount = mContentView.getChildCount();
        if (childCount > params.size()) {
            int count = childCount - params.size();
            mContentView.removeViews(childCount - count, count);
        }
        for (int i = 0; i < params.size(); i++) {
            Entities.SupportBeanEntryParam param = params.get(i);
            View view;
            if (mContentView.getChildCount() < (i + 1)) {
                view = mLayoutInflater.inflate(R.layout.item_view_param_group, null);
                mContentView.addView(view);
            } else {
                view = mContentView.getChildAt(i);
            }
            TextView title = view.findViewById(R.id.title_text);
            SingleCheckGroup singleCheckGroup = view.findViewById(R.id.multi_check_view);
            title.setText(param.name);
            singleCheckGroup.setTags(param.checkIndex, param.getTags());
            singleCheckGroup.setTagCheckedListener((index, value) -> {
                param.checkIndex = index;
                mDrawerLayout.closeDrawer(mEndDraView);
                mViewModel.initPageParam(rootUrl);
                mViewModel.refreshUrl(rootUrl, param.key, value, false);
            });
        }
    }

//    private final WebViewClient mWebViewClient = new WebViewClient() {
//
//        @Override
//        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            super.onReceivedError(view, request, error);
//            Log.e(TAG, "onReceivedError: " + error);
//        }
//
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            super.onPageStarted(view, url, favicon);
//            Log.i(TAG, "onPageStarted: " + url);
//        }
//
//        @Override
//        public void onPageFinished(WebView view, final String url) {
//            super.onPageFinished(view, url);
//            Log.i(TAG, "onPageFinished:" + url);
////            view.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
//            view.evaluateJavascript("document.getElementsByTagName('html')[0].innerHTML", value -> {
//                mViewModel.parseHtml(url, value);
//            });
//        }
//
//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            Log.i(TAG, "onReceivedSslError: " + error.toString());
//            if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
//                    || error.getPrimaryError() == SslError.SSL_EXPIRED
//                    || error.getPrimaryError() == SslError.SSL_INVALID
//                    || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
//                handler.proceed();
//            } else {
//                handler.cancel();
//            }
//            mViewModel.mBookData.postValue(null);
//        }
//    };

    private static class Adapter extends BaseQuickAdapter<SearchModel.SearchBook, CommonViewHolder> {

        public Adapter() {
            super(R.layout.item_view_search_result);
        }

        @Override
        protected void convert(CommonViewHolder holder, SearchModel.SearchBook item) {
            String sub = item.author + " | " + item.tag + "\n" + item.desc;
            holder.setRoundImageUrl(R.id.book_image, item.image, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.bookName)
                    .setText(R.id.book_h2, sub)
                    .setGone(R.id.local_read_tv, false)
                    .setGone(R.id.auto_parse_btn, false);
            holder.itemView.setOnClickListener(v -> {
                // click
                ThirdBookDetailActivity.startActivity(holder.itemView.getContext(), item);
            });
        }
    }
}
