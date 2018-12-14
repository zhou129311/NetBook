package com.xzhou.book.ui.main;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;

import com.xzhou.book.ui.common.BaseFragment;
import com.xzhou.book.ui.bookrack.BookrackFragment;
import com.xzhou.book.ui.community.CommunityFragment;
import com.xzhou.book.ui.find.FindFragment;

import java.util.List;

public class PagerFactory {
    public static final int FRAGMENT_BOOKRACK = 0;
    public static final int FRAGMENT_COMMUNITY = FRAGMENT_BOOKRACK + 1;
    public static final int FRAGMENT_FIND = FRAGMENT_COMMUNITY + 1;
    public static final int SIZE = FRAGMENT_FIND + 1;

    private static SparseArrayCompat<BaseFragment> cachesFragment = new SparseArrayCompat<>();

    public static BaseFragment getFragment(int position, FragmentManager fm) {
        @SuppressLint("RestrictedApi") List<Fragment> fragmentList = fm.getFragments();
        if (fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof BookrackFragment) {
                    cachesFragment.put(FRAGMENT_BOOKRACK, (BookrackFragment) fragment);
                } else if (fragment instanceof CommunityFragment) {
                    cachesFragment.put(FRAGMENT_BOOKRACK, (CommunityFragment) fragment);
                } else if (fragment instanceof FindFragment) {
                    cachesFragment.put(FRAGMENT_BOOKRACK, (FindFragment) fragment);
                }
            }
        }
        BaseFragment fragment;
        BaseFragment tmpFragment = cachesFragment.get(position);
        if (tmpFragment != null) {
            fragment = tmpFragment;
            return fragment;
        }
        switch (position) {
        case FRAGMENT_BOOKRACK:
            fragment = new BookrackFragment();
            break;
        case FRAGMENT_COMMUNITY:
            fragment = new CommunityFragment();
            break;
        case FRAGMENT_FIND:
            fragment = new FindFragment();
            break;
        default:
            throw new IllegalArgumentException("getFragment position " + position + " undefined");
        }
        cachesFragment.put(position, fragment);
        return fragment;
    }

    static void releaseCache() {
        cachesFragment.clear();
    }

}
