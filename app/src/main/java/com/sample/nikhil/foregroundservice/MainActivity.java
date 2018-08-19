package com.sample.nikhil.foregroundservice;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.sample.nikhil.foregroundservice.data.DataManager;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textview;
    Intent intent;
    private ViewPager mViewPager;
    private MainPagerAdapter mPagerAdapter;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        /*intent = new Intent(this, ForeGroundService.class);
        startService(intent);*/
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDataManager = DataManager.getInstance();
        mDataManager.setAudioManager((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager = findViewById(R.id.viewpager);
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        mPagerAdapter.addFragment(new FilesListFragment(), "");
        mPagerAdapter.addFragment(new FolderListFragment(), "");
        mPagerAdapter.addFragment(new ListFragment(), "");
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}