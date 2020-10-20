package com.xzhou.book.read;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CheckDialog;
import com.xzhou.book.common.WebFragment;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.AutoParseNetBook;
import com.xzhou.book.search.SearchActivity;
import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.utils.ToastUtils;

public class ReadWebActivity extends BaseActivity implements AutoParseNetBook.Callback {

    private WebFragment mWebFragment;
    private BookProvider.LocalBook mBaiduBook;
    private MenuItem mAddMenuItem;
    private String mCurUrl;
    private AlertDialog mParsingDialog;

    public static void startActivity(Context context, BookProvider.LocalBook book, String url) {
        Intent intent = new Intent(context, ReadWebActivity.class);
        intent.putExtra("baidu_book", book);
        intent.putExtra("cur_url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaiduBook = getIntent().getParcelableExtra("baidu_book");
        mCurUrl = getIntent().getStringExtra("cur_url");
        if (mBaiduBook == null && savedInstanceState != null) {
            mBaiduBook = savedInstanceState.getParcelable("baidu_book");
            mCurUrl = savedInstanceState.getString("cur_url");
        }
        if (mBaiduBook == null || !mBaiduBook.isBaiduBook) {
            finish();
            return;
        }
        setContentView(R.layout.activity_web_view);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mWebFragment = (WebFragment) fm.findFragmentByTag("web");
        if (mWebFragment == null) {
            ft.add(R.id.web_content, createFragment(), "web");
            ft.commitAllowingStateLoss();
        } else {
            ft.show(mWebFragment);
        }
        if (mBaiduBook.isBookshelf()) {
            BookProvider.updateReadTime(mBaiduBook);
        }
        AutoParseNetBook.addCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBaiduBook != null && mBaiduBook.isBookshelf() && mWebFragment != null) {
            mWebFragment.saveCurReadUrl(mBaiduBook._id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AutoParseNetBook.removeCallback(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelable("baidu_book", mBaiduBook);
            outState.putString("cur_url", mCurUrl);
        }
    }

    private WebFragment createFragment() {
        mWebFragment = new WebFragment();
        Bundle bundle = new Bundle();
        String url;
        if (!TextUtils.isEmpty(mCurUrl)) {
            url = mCurUrl;
        } else {
            url = AppSettings.getWebReadProgress(mBaiduBook._id);
            if (TextUtils.isEmpty(url)) {
                url = mBaiduBook.readUrl;
            }
        }
        bundle.putString("url", url);
        bundle.putString("bookId", mBaiduBook._id);
        mWebFragment.setArguments(bundle);
        return mWebFragment;
    }

    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read_web, menu);
        mAddMenuItem = menu.findItem(R.id.menu_add_bookshelf);
        if (mBaiduBook.isBookshelf()) {
            mAddMenuItem.setTitle(R.string.book_read_remove);
        } else {
            mAddMenuItem.setTitle(R.string.book_read_join);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem parse = menu.findItem(R.id.menu_parse);
        if (SearchModel.hasSupportLocalRead(mBaiduBook.curSourceHost)) {
            parse.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_bookshelf:
                if (mBaiduBook.isBookshelf()) {
                    showDeleteDialog(mBaiduBook);
                } else {
                    BookProvider.insertOrUpdate(mBaiduBook, false);
                    mAddMenuItem.setTitle(R.string.book_read_remove);
                }
                return true;
            case R.id.menu_search_baidu:
                SearchActivity.startActivity(this, mBaiduBook.title, SearchActivity.SEARCH_TYPE_BAIDU);
                return true;
            case R.id.menu_parse:
                AutoParseNetBook.tryParseBook(mBaiduBook.title, mBaiduBook.readUrl, mBaiduBook.curSourceHost);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog(final BookProvider.LocalBook book) {
        CheckDialog.Builder builder = new CheckDialog.Builder(mActivity);
        builder.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(new CheckDialog.OnPositiveClickListener() {
            @Override
            public void onClick(DialogInterface dialog, boolean isChecked) {
                dialog.dismiss();
                BookProvider.delete(book, isChecked);
                mAddMenuItem.setTitle(R.string.book_read_join);
            }
        }).show();
    }

    @Override
    protected boolean onNavBackClick() {
        if (mBaiduBook.isBookshelf()) {
            finish();
            return true;
        }
        showJoinDialog();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mWebFragment.onBackPressed()) {
            return;
        }
        if (!mBaiduBook.isBookshelf()) {
            showJoinDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showJoinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.book_read_add_book_title)
                .setMessage(R.string.book_read_add_book_msg)
                .setNegativeButton(R.string.book_read_not_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton(R.string.book_read_join, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        BookProvider.insertOrUpdate(mBaiduBook, false);
                        finish();
                    }
                });
        builder.show();
    }

    @Override
    public void onParseState(final boolean state, final boolean success, final String message) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (state) {
            showParsingDialog(message);
        } else {
            if (mParsingDialog != null) {
                mParsingDialog.dismiss();
                mParsingDialog = null;
            }
            if (success) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReadWebActivity.this);
                builder.setTitle(message)
                        .setMessage("是否跳转到本地书籍阅读？")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ReadActivity.startActivity(ReadWebActivity.this, mBaiduBook);
                                finish();
                            }
                        }).show();
            } else {
                ToastUtils.showShortToast(message);
            }
        }
    }

    private void showParsingDialog(String title) {
        if (mParsingDialog != null && mParsingDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mParsingDialog = builder.setTitle(title)
                .setMessage("正在解析中...")
                .setPositiveButton("结束解析", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AutoParseNetBook.stopParse();
                    }
                }).create();
        mParsingDialog.setCanceledOnTouchOutside(false);
        mParsingDialog.setCancelable(false);
        mParsingDialog.show();
    }
}
