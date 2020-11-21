package com.xzhou.book.bookshelf;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.AlertDialog;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CheckDialog;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.main.MainActivity;
import com.xzhou.book.models.Entities;
import com.xzhou.book.models.SearchModel;
import com.xzhou.book.net.AutoParseNetBook;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.read.ReadWebActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookshelfFragment extends BaseFragment<BookshelfContract.Presenter> implements BookshelfContract.View, AutoParseNetBook.Callback {
    private static final String TAG = "BookshelfFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.edit_layout)
    LinearLayout mEditLayout;
    @BindView(R.id.select_all_tv)
    TextView mSelectAllTv;
    @BindView(R.id.delete_tv)
    TextView mDeleteTv;

    private Adapter mAdapter;
    private View mEmptyView;
    private AlertDialog mParsingDialog;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_bookshelf;
    }

    @Override
    public void setPresenter(BookshelfContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new Adapter();
        mAdapter.setEmptyView(getEmptyView());
        mAdapter.bindToRecyclerView(mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(70, 0));

        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeLayout.setOnRefreshListener(mRefreshListener);
        mSwipeLayout.setEnabled(false);
        AutoParseNetBook.addCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter.start()) {
            //mPresenter.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            book.checkRemoveDownloadCallback();
        }
        AutoParseNetBook.removeCallback(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mPresenter.refresh();
        }
    };

    private View getEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_bookshelf, null);
            mEmptyView.findViewById(R.id.add_book_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.setCurFragment(MainActivity.FRAGMENT_FIND);
                    }
                }
            });
        }
        return mEmptyView;
    }

    @Override
    public void onLoadingState(boolean loading) {
        mSwipeLayout.setRefreshing(loading);
    }

    @Override
    public void onDataChange(List<BookProvider.LocalBook> books) {
        if (books == null || books.size() < 1) {
            mSwipeLayout.setEnabled(false);
            return;
        }
        mAdapter.setNewData(books);
        mSwipeLayout.setEnabled(true);
    }

    @Override
    public void onBookshelfUpdated(boolean update, String error) {
        String toast;
        if (!TextUtils.isEmpty(error)) {
            toast = error;
        } else {
            toast = getString(update ? R.string.update_success : R.string.update_none);
        }
        ToastUtils.showShortToast(toast);
    }

    @Override
    public void onAdd(int position, BookProvider.LocalBook book) {
        if (book == null) {
            return;
        }
        mAdapter.addData(position, book);
        mSwipeLayout.setEnabled(true);
    }

    @Override
    public void onRemove(BookProvider.LocalBook book) {
        if (book == null) {
            return;
        }
        for (int i = 0, size = mAdapter.getData().size(); i < size; i++) {
            BookProvider.LocalBook old = mAdapter.getData().get(i);
            if (TextUtils.equals(old._id, book._id)) {
                mAdapter.remove(i);
                break;
            }
        }
        if (mAdapter.getData().size() <= 0) {
            mSwipeLayout.setEnabled(false);
        }
    }

    @Override
    public void onUpdateDownloadState(BookProvider.LocalBook localBook) {
        int index = mAdapter.getData().indexOf(localBook);
        if (index > -1) {
            mAdapter.refreshNotifyItemChanged(index);
        }
    }

    @Override
    public void onLogin(Entities.Login login) {
        if (login == null || login.user == null) {
            ToastUtils.showShortToast("登录失败");
//            return;
        }
//        Activity activity = getActivity();
//        if (activity instanceof MainActivity) {
//            ((MainActivity) activity).updateLogin(login);
//        }
    }

    @OnClick({R.id.select_all_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_all_tv:
                String text = mSelectAllTv.getText().toString();
                if (text.equals(getString(R.string.select_all))) {
                    changeSelectedAll(true);
                } else {
                    changeSelectedAll(false);
                }
                break;
            case R.id.delete_tv:
                List<String> list = getCheckedList();
                if (list.size() < 1) {
                    ToastUtils.showShortToast(R.string.has_not_selected_delete_book);
                } else {
                    showDeleteDialog(list);
                }
                break;
        }
    }

    public boolean hasEdit() {
        return mEditLayout.getVisibility() == View.VISIBLE;
    }

    public void cancelEdit() {
        changeEditMode(false);
    }

    private void changeEditMode(boolean isEdit) {
        if (isEdit) {
            mEditLayout.setVisibility(View.VISIBLE);
            mSwipeLayout.setEnabled(false);
        } else {
            mEditLayout.setVisibility(View.GONE);
            mSwipeLayout.setEnabled(true);
        }
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            book.isEdit = isEdit;
            book.isChecked = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void changeSelectedAll(boolean isSelect) {
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            book.isChecked = isSelect;
        }
        mAdapter.notifyDataSetChanged();
        updateDeleteTv();
    }

    private void updateDeleteTv() {
        int count = getCheckedCount();
        if (count > 0) {
            mDeleteTv.setText(getString(R.string.delete_count, count));
        } else {
            mDeleteTv.setText(R.string.delete);
        }
    }

    private int getCheckedCount() {
        int count = 0;
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            if (book.isChecked) {
                count++;
            }
        }
        if (count == mAdapter.getData().size()) {
            mSelectAllTv.setText(R.string.cancel_selected_all);
        } else {
            mSelectAllTv.setText(R.string.select_all);
        }
        return count;
    }

    private List<String> getCheckedList() {
        List<String> list = new ArrayList<>();
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            if (book.isChecked) {
                list.add(book._id);
            }
        }
        return list;
    }

    private void showDeleteDialog(final List<String> list) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        CheckDialog.Builder builder = new CheckDialog.Builder(activity);
        builder.setNegativeButton((dialog, which) -> {
            dialog.dismiss();
            changeEditMode(false);
        }).setPositiveButton((dialog, isChecked) -> {
            dialog.dismiss();
            changeEditMode(false);
            BookProvider.delete(list, isChecked);
        }).show();
    }

    @Override
    public void onParseState(boolean state, boolean success, String message) {
        if (state) {
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

    private void showParsingDialog(String title) {
        if (mParsingDialog != null && mParsingDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private class Adapter extends BaseQuickAdapter<BookProvider.LocalBook, CommonViewHolder> {

        private final String[] DIALOG_ITEMS = new String[]{
                "书籍详情", "缓存全本", "删除", "批量管理", "更新"
        };

        Adapter() {
            super(R.layout.item_view_bookshelf_book);
        }

        @Override
        protected void convert(CommonViewHolder helper, final BookProvider.LocalBook item) {
            String sub;
            if (item.isBaiduBook) {
                if (TextUtils.isEmpty(item.lastChapter)) {
                    sub = item.desc + "\n" + item.sourceId + " | " + item.curSourceHost;
                } else {
                    sub = "最新章节：" + item.lastChapter + "\n" + item.sourceId + " | " + item.curSourceHost;
                }
            } else {
                sub = AppUtils.getDescriptionTimeFromTimeMills(item.updated) + " : " + item.lastChapter;
            }
            boolean showDownloadState = !TextUtils.isEmpty(item.downloadStatus);
            if (showDownloadState) {
                helper.setText(R.id.download_status, item.downloadStatus);
            }
            helper.setRoundImageUrl(R.id.book_image, item.cover, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.title)
                    .setGone(R.id.download_status, showDownloadState)
                    .setText(R.id.book_subhead, sub)
                    .setGone(R.id.book_updated_iv, item.isShowRed && !item.isEdit)
                    .setVisible(R.id.book_checkbox, item.isEdit)
                    .setGone(R.id.book_top, item.hasTop);
            final CheckBox cb = helper.getView(R.id.book_checkbox);
            cb.setChecked(item.isChecked);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isChecked = isChecked;
                updateDeleteTv();
            });
            helper.itemView.setOnClickListener(v -> {
                if (hasEdit()) {
                    cb.setChecked(!cb.isChecked());
                    return;
                }
                if (doubleClick()) {
                    return;
                }
                if (item.isBaiduBook && !SearchModel.hasSupportLocalRead(item.curSourceHost)) {
                    ReadWebActivity.startActivity(getContext(), item, null);
                } else {
                    ReadActivity.startActivity(getRecyclerView().getContext(), item);
                }
            });
            helper.itemView.setOnLongClickListener(v -> {
                if (hasEdit() || mSwipeLayout.isRefreshing()) {
                    return true;
                }
                ItemDialog.Builder builder = new ItemDialog.Builder(mContext);
                final List<String> list = new ArrayList<>();
                String top_untop = item.hasTop ? "取消置顶" : "置顶";
                list.add(top_untop);
                Collections.addAll(list, DIALOG_ITEMS);
                if (item.isBaiduBook && !SearchModel.hasSupportLocalRead(item.curSourceHost)) {
                    list.add("解析本书籍");
                }
                builder.setTitle(item.title).setItems(list.toArray(new String[0]), (dialog, which) -> {
                    dialog.dismiss();
                    String s = list.get(which);
                    switch (s) {
                        case "取消置顶":
                        case "置顶":
                            item.hasTop = !item.hasTop;
                            BookProvider.updateHasTop(item);
                            break;
                        case "书籍详情":
                            if (item.isBaiduBook) {
                                ReadWebActivity.startActivity(getContext(), item, null);
                                return;
                            }
                            BookDetailActivity.startActivity(mContext, item._id);
                            break;
                        case "缓存全本":
                            if (item.isBaiduBook && !SearchModel.hasSupportLocalRead(item.curSourceHost)) {
                                ToastUtils.showShortToast("暂不支持缓存:" + item.curSourceHost);
                                return;
                            }
                            mPresenter.download(item);
                            break;
                        case "删除":
                            List<String> list1 = new ArrayList<>();
                            list1.add(item._id);
                            showDeleteDialog(list1);
                            break;
                        case "批量管理":
                            changeEditMode(true);
                            break;
                        case "更新":
                            if (item.isBaiduBook) {
                                mPresenter.updateNetBook(item);
                            } else {
                                ToastUtils.showShortToast("暂不支持更新追书书源:" + item.title);
                            }
                            break;
                        case "解析本书籍":
                            AutoParseNetBook.tryParseBook(item.title, item.readUrl, item.curSourceHost);
                            break;
                    }
                }).show();
                return true;
            });
        }
    }
}
