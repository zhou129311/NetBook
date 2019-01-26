package com.xzhou.book.bookshelf;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xzhou.book.R;
import com.xzhou.book.common.BaseFragment;
import com.xzhou.book.common.CheckDialog;
import com.xzhou.book.common.CommonViewHolder;
import com.xzhou.book.common.ItemDialog;
import com.xzhou.book.common.LineItemDecoration;
import com.xzhou.book.common.MyLinearLayoutManager;
import com.xzhou.book.db.BookProvider;
import com.xzhou.book.main.BookDetailActivity;
import com.xzhou.book.main.MainActivity;
import com.xzhou.book.read.ReadActivity;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.Log;
import com.xzhou.book.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookshelfFragment extends BaseFragment<BookshelfContract.Presenter> implements BookshelfContract.View {
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
        mRecyclerView.addItemDecoration(new LineItemDecoration());

        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeLayout.setOnRefreshListener(mRefreshListener);
        mSwipeLayout.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter.start()) {
            mPresenter.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (BookProvider.LocalBook book : mAdapter.getData()) {
            book.checkRemoveDownloadCallback();
        }
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
        String toast = getString(update ? R.string.update_success : R.string.update_none);
        if (error != null) {
            toast = error;
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

    @OnClick({ R.id.select_all_tv, R.id.delete_tv })
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
        builder.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                changeEditMode(false);
            }
        }).setPositiveButton(new CheckDialog.OnPositiveClickListener() {
            @Override
            public void onClick(DialogInterface dialog, boolean isChecked) {
                dialog.dismiss();
                changeEditMode(false);
                BookProvider.delete(list, isChecked);
            }
        }).show();
    }

    private class Adapter extends BaseQuickAdapter<BookProvider.LocalBook, CommonViewHolder> {

        private final String[] DIALOG_ITEM_UPTOP = new String[] {
                "置顶", "书籍详情", "缓存全本", "删除", "批量管理"
        };

        private final String[] DIALOG_ITEM_TOP = new String[] {
                "取消置顶", "书籍详情", "缓存全本", "删除", "批量管理"
        };

        Adapter() {
            super(R.layout.item_view_bookshelf_book, null);
        }

        @Override
        protected void convert(CommonViewHolder helper, final BookProvider.LocalBook item) {
            String sub = AppUtils.getDescriptionTimeFromTimeMills(item.updated);
            boolean showDownloadState = !TextUtils.isEmpty(item.downloadStatus);
            if (showDownloadState) {
                helper.setText(R.id.download_status, item.downloadStatus);
            }
            Log.d(TAG, "item.updated = " + item.updated + ",item.readTime = " + item.readTime);
            helper.setRoundImageUrl(R.id.book_image, item.cover, R.mipmap.ic_cover_default)
                    .setText(R.id.book_title, item.title)
                    .setGone(R.id.download_status, showDownloadState)
                    .setText(R.id.book_subhead, sub + " : " + item.lastChapter)
                    .setGone(R.id.book_updated_iv, item.updated > item.readTime && !item.isEdit)
                    .setVisible(R.id.book_checkbox, item.isEdit)
                    .setGone(R.id.book_top, item.hasTop);
            final CheckBox cb = helper.getView(R.id.book_checkbox);
            cb.setChecked(item.isChecked);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.isChecked = isChecked;
                    updateDeleteTv();
                }
            });
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasEdit()) {
                        cb.setChecked(!cb.isChecked());
                        return;
                    }
                    ReadActivity.startActivity(getRecyclerView().getContext(), item);
                }
            });
            helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (hasEdit() || mSwipeLayout.isRefreshing()) {
                        return true;
                    }
                    ItemDialog.Builder builder = new ItemDialog.Builder(mContext);
                    builder.setTitle(item.title).setItems(item.hasTop ? DIALOG_ITEM_TOP : DIALOG_ITEM_UPTOP, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                            case 0:
                                item.hasTop = !item.hasTop;
                                BookProvider.updateHasTop(item);
                                break;
                            case 1:
                                BookDetailActivity.startActivity(mContext, item._id);
                                break;
                            case 2:
                                mPresenter.download(item);
                                break;
                            case 3:
                                List<String> list = new ArrayList<>();
                                list.add(item._id);
                                showDeleteDialog(list);
                                break;
                            case 4:
                                changeEditMode(true);
                                break;
                            }
                        }
                    }).show();
                    return true;
                }
            });
        }
    }
}
