package com.xzhou.book.read;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xzhou.book.R;
import com.xzhou.book.common.BaseActivity;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.xzhou.book.read.ReadActivity.EXTRA_BOOK;

public class ReadCartoonActivity extends BaseActivity {
    public static final String TAG = "ReadCartoonActivity";
    @BindView(R.id.cartoon_view_pager)
    ViewPager mCartoonViewPager;
    @BindView(R.id.cartoon_abl_top_menu)
    AppBarLayout mCartoonAblTopMenu;
    @BindView(R.id.brightness_seek_bar)
    SeekBar mBrightnessSeekBar;
    @BindView(R.id.read_setting_bottom_ll_layout)
    LinearLayout mReadSettingBtmLlLayout;
    @BindView(R.id.read_bottom_bar)
    ConstraintLayout mReadBottomBar;

    private BookProvider.LocalBook mBook;
    private List<Entities.Chapters> mChaptersList;

    public static void startActivity(Context context, BookProvider.LocalBook book) {
        Intent intent = new Intent(context, ReadCartoonActivity.class);
        intent.putExtra(EXTRA_BOOK, book);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBook = getIntent().getParcelableExtra(EXTRA_BOOK);
        if (mBook == null && savedInstanceState != null) {
            mBook = savedInstanceState.getParcelable(EXTRA_BOOK);
        }
        if (mBook == null) {
            ToastUtils.showShortToast("出现错误，打开失败");
            finish();
            return;
        }
        setContentView(R.layout.activity_read_cartoon);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setTitle(mBook.title);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.reader_menu_bg_color));
    }

    @OnClick({ R.id.brightness_min, R.id.brightness_max, R.id.previous_chapter_tv, R.id.next_chapter_tv,
            R.id.previous_page_tv, R.id.next_page_tv, R.id.toc_view, R.id.light_view, R.id.download_view, R.id.more_setting_view })
    public void onViewClicked(View view) {
        switch (view.getId()) {
        case R.id.brightness_min:
            break;
        case R.id.brightness_max:
            break;
        case R.id.previous_chapter_tv:
            break;
        case R.id.next_chapter_tv:
            break;
        case R.id.previous_page_tv:
            break;
        case R.id.next_page_tv:
            break;
        case R.id.toc_view:
            break;
        case R.id.light_view:
            break;
        case R.id.download_view:
            break;
        case R.id.more_setting_view:
            break;
        }
    }
}
