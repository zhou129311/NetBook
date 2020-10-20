package com.xzhou.book.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.FiltrateAdapter;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.common.TabActivity;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Constant;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity<SearchContract.Presenter> implements SearchContract.View {
    private static final String TAG = "SearchActivity";
    public static final String EXTRA_SEARCH_KEY = "search_key";
    public static final String EXTRA_SEARCH_TYPE = "search_type";

    public static final int SEARCH_TYPE_BAIDU = 0;
    public static final int SEARCH_TYPE_SOGOU = 1;
    public static final int SEARCH_TYPE_ZHUISHU = 2;

    private static final int TAB_HISTORY = 0;
    private static final int TAB_RESULT_ZHUI = 1;
    private static final int TAB_RESULT_NET = 2;
    @BindView(R.id.search_et)
    EditText mSearchEt;
    @BindView(R.id.search_iv)
    ImageView mSearchIv;
    @BindView(R.id.clear_et_iv)
    ImageView mClearEtIv;
    @BindView(R.id.auto_complete_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.search_source_tv)
    TextView mRelSourceTv;
    @BindView(R.id.search_toolbar)
    View mSearchToolBar;

    private String mKey;
    private Fragment mCurFragment;
    private SparseArray<Fragment> mFragments;
    private AutoCompleteAdapter mAdapter;
    private boolean mIsEnableAutoSuggest = true;
    private ListPopupWindow mListPopupWindow;
    private String[] mSearchTypes;
    private int mSearchType;
    private NetSearchContract.Presenter mNetPresenter;

    public static void startActivity(Context context) {
        startActivity(context, null);
    }

    public static void startActivity(Context context, String key) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_KEY, key);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String key, int searchType) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_KEY, key);
        intent.putExtra(EXTRA_SEARCH_TYPE, searchType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mKey = getIntent().getStringExtra(EXTRA_SEARCH_KEY);
        mSearchType = getIntent().getIntExtra(EXTRA_SEARCH_TYPE, -1);
        if (savedInstanceState != null && TextUtils.isEmpty(mKey)) {
            mKey = savedInstanceState.getString(EXTRA_SEARCH_KEY);
        }
        mSearchTypes = getResources().getStringArray(R.array.search_type);
        if (mSearchType == -1) {
            mSearchType = SPUtils.get().getInt(EXTRA_SEARCH_TYPE, SEARCH_TYPE_BAIDU);
        }
        Log.i(TAG, "mKey = " + mKey + " ,mSearchType = " + mSearchType);
        mRelSourceTv.setText(getString(R.string.search_result_hint, mSearchTypes[mSearchType]));
        createFragment(mKey);
        if (TextUtils.isEmpty(mKey)) {
            mSearchEt.requestFocus();
            showFragment(TAB_HISTORY);
        } else {
            showFragment(mSearchType == SEARCH_TYPE_ZHUISHU ? TAB_RESULT_ZHUI : TAB_RESULT_NET);
            mSearchEt.setText(mKey);
            getHistoryFragment().addNewHistory(mKey);
        }
        mSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchEt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mSearchIv.setEnabled(false);
                    mClearEtIv.setVisibility(View.GONE);
                    showFragment(TAB_HISTORY);
                } else {
                    mSearchIv.setEnabled(true);
                    mClearEtIv.setVisibility(View.VISIBLE);
                }
                if (mSearchType == SEARCH_TYPE_ZHUISHU) {
                    mPresenter.autoComplete(text);
                }
            }
        });
        mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mKey = mSearchEt.getText().toString();
                    if (!TextUtils.isEmpty(mKey)) {
                        search(mKey);
                    }
                    return true;
                }
                return false;
            }
        });
        getHistoryFragment().setOnHistoryListener(new HistoryFragment.OnHistoryListener() {
            @Override
            public void onClick(String history) {
                search(history);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(EXTRA_SEARCH_KEY, mKey);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState != null) {
            mKey = savedInstanceState.getString(EXTRA_SEARCH_KEY);
        }
    }

    @Override
    public void onBackPressed() {
        NetResultFragment fragment = getNetResultFragment();
        if (mCurFragment == fragment) {
            if (!fragment.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected SearchContract.Presenter createPresenter() {
        return new SearchPresenter(this);
    }

    public int getCurTabId() {
        return getZhuishuResultFragment().getCurTabId();
    }

    private void research() {
        search(mKey, true);
    }

    private void search(String key) {
        search(key, false);
    }

    private void search(String key, boolean force) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        Log.i(TAG, "search:" + key + ", mSearchType:" + mSearchType);
        mSearchEt.removeCallbacks(mEnableRun);
        mIsEnableAutoSuggest = false;
        mKey = key;
        showFragment(mSearchType == SEARCH_TYPE_ZHUISHU ? TAB_RESULT_ZHUI : TAB_RESULT_NET);
        mSearchEt.setText(key);
        mSearchEt.setSelection(key.length());
        if (mSearchType == SEARCH_TYPE_ZHUISHU) {
            getZhuishuResultFragment().search(key);
        } else {
            getNetResultFragment().search(key, mSearchType, force);
        }
        getHistoryFragment().addNewHistory(key);
        mSearchEt.postDelayed(mEnableRun, 2000);
        mSearchEt.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(getContentView().getWindowToken(), 0);
    }

    private Runnable mEnableRun = new Runnable() {
        @Override
        public void run() {
            mIsEnableAutoSuggest = true;
        }
    };

    private void showFragment(int tab) {
        if (mCurFragment != null && TextUtils.equals(getTagName(tab), mCurFragment.getTag())) {
            return;
        }

        Log.i(TAG, "showFragment:" + tab);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mCurFragment != null) {
            ft.hide(mCurFragment);
            mCurFragment = null;
        } else {
            List<Fragment> list = getSupportFragmentManager().getFragments();
            for (Fragment fragment : list) {
                ft.hide(fragment);
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getTagName(tab));
        if (fragment != null) {
            ft.show(fragment);
        } else {
            fragment = mFragments.get(tab);
            ft.add(R.id.content, fragment, getTagName(tab));
        }
        ft.commitAllowingStateLoss();
        mCurFragment = fragment;
    }

    private void createFragment(String key) {
        if (mFragments != null) {
            return;
        }
        mFragments = new SparseArray<>();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SEARCH_KEY, key);
        bundle.putInt(EXTRA_SEARCH_TYPE, mSearchType);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof HistoryFragment) {
                mFragments.put(TAB_HISTORY, fragment);
            } else if (fragment instanceof ResultFragment) {
                fragment.setArguments(bundle);
                mFragments.put(TAB_RESULT_ZHUI, fragment);
            } else if (fragment instanceof NetResultFragment) {
                fragment.setArguments(bundle);
                mNetPresenter = new NetSearchPresenter((NetSearchContract.View) fragment);
                mFragments.put(TAB_RESULT_NET, fragment);
            }
        }
        Fragment fragment = mFragments.get(TAB_HISTORY);
        if (fragment == null) {
            mFragments.put(TAB_HISTORY, new HistoryFragment());
        }
        fragment = mFragments.get(TAB_RESULT_ZHUI);
        if (fragment == null) {
            fragment = new ResultFragment();
            fragment.setArguments(bundle);
            mFragments.put(TAB_RESULT_ZHUI, fragment);
        }
        fragment = mFragments.get(TAB_RESULT_NET);
        if (fragment == null) {
            fragment = new NetResultFragment();
            mNetPresenter = new NetSearchPresenter((NetSearchContract.View) fragment);
            fragment.setArguments(bundle);
            mFragments.put(TAB_RESULT_NET, fragment);
        }
    }

    private HistoryFragment getHistoryFragment() {
        return (HistoryFragment) mFragments.get(TAB_HISTORY);
    }

    private ResultFragment getZhuishuResultFragment() {
        return (ResultFragment) mFragments.get(TAB_RESULT_ZHUI);
    }

    private NetResultFragment getNetResultFragment() {
        return (NetResultFragment) mFragments.get(TAB_RESULT_NET);
    }

    private String getTagName(int tab) {
        return "s_fragment_" + tab;
    }

    @OnClick({R.id.back_iv, R.id.search_iv, R.id.clear_et_iv, R.id.more_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_iv:
                onBackPressed();
                break;
            case R.id.search_iv:
                mKey = mSearchEt.getText().toString();
                if (!TextUtils.isEmpty(mKey)) {
                    search(mKey, true);
                }
                break;
            case R.id.more_iv:
                showSearchTypeWindow();
                break;
            case R.id.clear_et_iv:
                mSearchEt.setText("");
                mRecyclerView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onAutoComplete(List<Entities.Suggest> list) {
        if (list == null || list.size() <= 0 || !mIsEnableAutoSuggest) {
            mRecyclerView.setVisibility(View.GONE);
            if (mAdapter != null) {
                mAdapter.setNewData(null);
            }
            return;
        }

        mRecyclerView.setVisibility(View.VISIBLE);
        if (mAdapter == null) {
            mAdapter = new AutoCompleteAdapter(list);
            mAdapter.bindToRecyclerView(mRecyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new LineItemDecoration(true));
            mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        } else {
            mAdapter.setNewData(list);
        }
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
    }

    private void showSearchTypeWindow() {
        if (mListPopupWindow == null) {
            mListPopupWindow = new ListPopupWindow(this);
            List<String> list = new ArrayList<>();
            Collections.addAll(list, mSearchTypes);
            final FiltrateAdapter adapter = new FiltrateAdapter(this, list);
            mListPopupWindow.setAdapter(adapter);
            mListPopupWindow.setWidth((int) (AppUtils.getScreenWidth() / 2.6f));
            mListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mListPopupWindow.setAnchorView(findViewById(R.id.more_iv));
            mListPopupWindow.setHorizontalOffset(AppUtils.dip2px(-16));//相对锚点偏移值，正值表示向右偏移
            mListPopupWindow.setVerticalOffset(AppUtils.dip2px(-5));//相对锚点偏移值，正值表示向下偏移
            mListPopupWindow.setDropDownGravity(Gravity.BOTTOM);
            mListPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dialog_common));
            mListPopupWindow.setModal(true);//模态框，设置为true响应物理键
            adapter.setChecked(mSearchType);
            mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mSearchType = i;
                    adapter.setChecked(mSearchType);
                    mRelSourceTv.setText(getString(R.string.search_result_hint, mSearchTypes[i]));
                    SPUtils.get().putInt(EXTRA_SEARCH_TYPE, mSearchType);
                    research();
                    mListPopupWindow.dismiss();
                }
            });
        }
        mListPopupWindow.show();
    }

    private class AutoCompleteAdapter extends BaseQuickAdapter<Entities.Suggest, CommonViewHolder> {

        AutoCompleteAdapter(@Nullable List<Entities.Suggest> data) {
//            super(R.layout.item_search_auto_complete, data);
            super(R.layout.item_search_auto_suggest, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, final Entities.Suggest item) {
            holder.setImageResource(R.id.auto_suggest_img, item.getImgRes())
                    .setText(R.id.auto_suggest_text, item.text);
            TextView tag1 = holder.getView(R.id.auto_suggest_tag1);
            TextView tag2 = holder.getView(R.id.auto_suggest_tag2);
            if (item.isCat()) {
                tag1.setVisibility(View.VISIBLE);
                tag2.setVisibility(View.VISIBLE);
                tag1.setText(R.string.category);
                if ("male".equals(item.gender)) {
                    tag2.setText(R.string.male);
                } else {
                    tag2.setText(R.string.female);
                }
            } else if (item.isPicture()) {
                tag1.setVisibility(View.VISIBLE);
                tag1.setText(R.string.picture);
            } else if (item.isTag()) {
                tag1.setVisibility(View.VISIBLE);
                tag1.setText(R.string.tag);
            } else if (item.isAuthor()) {
                tag1.setVisibility(View.VISIBLE);
                tag1.setText(R.string.author);
            } else {
                tag1.setVisibility(View.GONE);
                tag2.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Entities.TabData data = null;
                    if (item.isTag()) {
                        data = new Entities.TabData();
                        data.title = item.text;
                        data.source = Constant.TabSource.SOURCE_TAG;
                        data.params = new String[]{item.text};
                    } else if (item.isAuthor()) {
                        data = new Entities.TabData();
                        data.title = item.text;
                        data.source = Constant.TabSource.SOURCE_AUTHOR;
                        data.params = new String[]{data.title};
                    } else if (item.isCat()) {
                        data = new Entities.TabData();
                        data.title = item.text;
                        data.source = Constant.TabSource.SOURCE_CATEGORY_SUB;
                        data.params = new String[]{item.major, item.gender};
                        if (item.minors != null && item.minors.size() > 0) {
                            List<String> filtrates = new ArrayList<>();
                            filtrates.add(item.major);
                            filtrates.addAll(item.minors);
                            data.filtrate = filtrates.toArray(new String[0]);
                            data.curFiltrate = filtrates.indexOf(item.text);
                        }
                    } else if (!TextUtils.isEmpty(item.id)) {
                        BookDetailActivity.startActivity(mActivity, item.id);
                    }
                    if (data != null) {
                        TabActivity.startActivity(mActivity, data);
                    }
                }
            });
        }
    }
}
