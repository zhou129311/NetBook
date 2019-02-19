package com.xzhou.book.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.datasource.BaiduSearch;
import com.xzhou.book.utils.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity {

    public static final String EXTRA_SEARCH_KEY = "search_key";

    private static final int TAB_HISTORY = 0;
    private static final int TAB_RESULT = 1;
    @BindView(R.id.search_et)
    EditText mSearchEt;
    @BindView(R.id.search_iv)
    ImageView mSearchIv;
    @BindView(R.id.clear_et_iv)
    ImageView mClearEtIv;
    @BindView(R.id.auto_complete_rv)
    RecyclerView mRecyclerView;

    private String mKey;
    private Fragment mCurFragment;
    private SparseArray<Fragment> mFragments;
    private AutoCompleteAdapter mAdapter;
    private SearchContract.Presenter mPresenter;

    public static void startActivity(Context context) {
        startActivity(context, null);
    }

    public static void startActivity(Context context, String key) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_KEY, key);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mKey = getIntent().getStringExtra(EXTRA_SEARCH_KEY);
        createFragment(mKey);
        if (TextUtils.isEmpty(mKey)) {
            showFragment(TAB_HISTORY);
        } else {
            showFragment(TAB_RESULT);
        }
        mSearchEt.requestFocus();
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
                mPresenter.autoComplete(text);
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
        getResultFragment().setOnAutoCompleteListener(new ResultFragment.OnAutoCompleteListener() {
            @Override
            public void onUpdate(List<String> list) {
                updateAutoCompletes(list);
            }
        });
        getHistoryFragment().setOnHistoryListener(new HistoryFragment.OnHistoryListener() {
            @Override
            public void onClick(String history) {
                search(history);
            }
        });
    }

    private void search(String key) {
        Log.i("search:" + key);
        mKey = key;
        showFragment(TAB_RESULT);
        mSearchEt.setText(key);
        mSearchEt.setSelection(key.length());
        getResultFragment().search(key);
        getHistoryFragment().addNewHistory(key);
    }

    private void showFragment(int tab) {
        if (mCurFragment != null && TextUtils.equals(getTagName(tab), mCurFragment.getTag())) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mCurFragment != null) {
            ft.hide(mCurFragment);
            mCurFragment = null;
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
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof HistoryFragment) {
                    mFragments.put(TAB_HISTORY, fragment);
                } else if (fragment instanceof ResultFragment) {
                    fragment.setArguments(bundle);
                    mFragments.put(TAB_RESULT, fragment);
                }
            }
        }
        Fragment fragment = mFragments.get(TAB_HISTORY);
        if (fragment == null) {
            mFragments.put(TAB_HISTORY, new HistoryFragment());
        }
        fragment = mFragments.get(TAB_RESULT);
        if (fragment == null) {
            fragment = new ResultFragment();
            fragment.setArguments(bundle);
            mFragments.put(TAB_RESULT, fragment);
        }
        mPresenter = new SearchPresenter((SearchContract.View) fragment);
    }

    private HistoryFragment getHistoryFragment() {
        return (HistoryFragment) mFragments.get(TAB_HISTORY);
    }

    private ResultFragment getResultFragment() {
        return (ResultFragment) mFragments.get(TAB_RESULT);
    }

    private String getTagName(int tab) {
        return "s_fragment_" + tab;
    }

    private void updateAutoCompletes(List<String> list) {
        if (list == null || list.size() <= 0 || mCurFragment instanceof ResultFragment) {
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

    @OnClick({ R.id.back_iv, R.id.search_iv, R.id.clear_et_iv })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.back_iv:
            onBackPressed();
            break;
        case R.id.search_iv:
            mKey = mSearchEt.getText().toString();
            if (!TextUtils.isEmpty(mKey)) {
                search(mKey);
            }
            break;
        case R.id.clear_et_iv:
            mSearchEt.setText("");
            break;
        }
    }

    private class AutoCompleteAdapter extends BaseQuickAdapter<String, CommonViewHolder> {

        AutoCompleteAdapter(@Nullable List<String> data) {
            super(R.layout.item_search_auto_complete, data);
        }

        @Override
        protected void convert(CommonViewHolder holder, final String item) {
            holder.setText(R.id.auto_complete_tv, item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    search(item);
                }
            });
        }
    }
}
