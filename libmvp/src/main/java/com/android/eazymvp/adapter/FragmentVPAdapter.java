package com.android.eazymvp.adapter;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.android.eazymvp.base.baseimpl.view.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentVPAdapter extends FragmentPagerAdapter {
    private List<String> titles;
    private List<Class<? extends BaseFragment>> fragments;
    private ArrayList<Bundle> mBundles;

    public FragmentVPAdapter(@NonNull FragmentManager fm, List<Class<? extends BaseFragment>> fragments) {
        this(fm, FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT, fragments);
    }

    public FragmentVPAdapter(@NonNull FragmentManager fm, List<Class<? extends BaseFragment>> fragments, ArrayList<Bundle> bundles) {
        this(fm, FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT, fragments);
        mBundles = bundles;
    }

    public FragmentVPAdapter(@NonNull FragmentManager fm, int behavior, List<Class<? extends BaseFragment>> fragments) {
        super(fm, behavior);
        this.fragments = fragments;
    }

    public FragmentVPAdapter(@NonNull FragmentManager fm, List<Class<? extends BaseFragment>> fragments, List<String> titles) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT);
        this.fragments = fragments;
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        try {
            BaseFragment baseFragment = fragments.get(position).newInstance();
            if (mBundles != null && mBundles.size() >= position) {
                baseFragment.setArguments(mBundles.get(position));
            }
            return baseFragment;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles == null ? super.getPageTitle(position) : titles.get(position);
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }
}
