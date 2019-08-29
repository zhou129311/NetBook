package com.xzhou.book.read;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.common.CheckDialog;
import com.xzhou.book.common.WebFragment;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.search.BaiduResultActivity;

public class ReadWebActivity extends BaseActivity {
    private static final String TAG = "ReadWebActivity";

    private WebFragment mWebFragment;
    private BookProvider.LocalBook mBaiduBook;
    private MenuItem mAddMenuItem;

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        Intent intent = new Intent(context, ReadWebActivity.class);
        intent.putExtra("baidu_book", book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaiduBook = getIntent().getParcelableExtra("baidu_book");
        if (mBaiduBook == null && savedInstanceState != null) {
            mBaiduBook = savedInstanceState.getParcelable("baidu_book");
        }
        if (mBaiduBook == null || !mBaiduBook.isBaiduBook) {
            finish();
            return;
        }
        setContentView(R.layout.activity_web_view);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag("web");
        if (fragment == null) {
            ft.add(R.id.web_content, createFragment(), "web");
            ft.commitAllowingStateLoss();
        } else {
            ft.show(fragment);
        }
        if (mBaiduBook.isBookshelf()) {
            BookProvider.updateReadTime(mBaiduBook);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBaiduBook != null && mBaiduBook.isBookshelf()) {
            mWebFragment.saveCurReadUrl(mBaiduBook._id);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelable("baidu_book", mBaiduBook);
        }
    }

    private Fragment createFragment() {
        mWebFragment = new WebFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", mBaiduBook.readUrl);
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
                BaiduResultActivity.startActivity(this, mBaiduBook.title);
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
}
