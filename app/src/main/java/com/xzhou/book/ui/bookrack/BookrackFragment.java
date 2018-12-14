package com.xzhou.book.ui.bookrack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.ui.common.BaseFragment;

public class BookrackFragment extends BaseFragment {
    private static final String TAG = "BookrackFragment";

    private BookrackAdapter mAdapter;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_bookrack;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }
}
