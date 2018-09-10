package com.sample.nikhil.foregroundservice.activities;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.adapters.MainPagerAdapter;
import com.sample.nikhil.foregroundservice.data.DataManager;
import com.sample.nikhil.foregroundservice.fragments.FilesListFragment;
import com.sample.nikhil.foregroundservice.fragments.FolderListFragment;
import com.sample.nikhil.foregroundservice.fragments.ListFragment;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private MainPagerAdapter mPagerAdapter;
    private TabLayout mTabLayout;
    private DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mDataManager = DataManager.getInstance();
        mDataManager.setAudioManager((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        mViewPager = findViewById(R.id.viewpager);
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        mPagerAdapter.addFragment(new FilesListFragment(), "Files");
        mPagerAdapter.addFragment(new FolderListFragment(), "Folder");
        mPagerAdapter.addFragment(new ListFragment(), "Artists");
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}