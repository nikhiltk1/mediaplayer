package com.sample.nikhil.foregroundservice;

import android.app.Application;

import com.sample.nikhil.foregroundservice.data.DataManager;

/**
 * Created by Nikhil on 18-08-2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DataManager.init(getApplicationContext());
    }
}
