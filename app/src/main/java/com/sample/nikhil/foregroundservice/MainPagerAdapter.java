package com.sample.nikhil.foregroundservice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikhil on 11-08-2018.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList=new ArrayList<>();
    private final List<String> mTitleList=new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm,Context context) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }
    public void addFragment(Fragment fragment,String title){
        mFragmentList.add(fragment);
        mTitleList.add(title);
    }
}
