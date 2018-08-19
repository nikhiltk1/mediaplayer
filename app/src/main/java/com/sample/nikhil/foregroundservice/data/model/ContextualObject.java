package com.sample.nikhil.foregroundservice.data.model;

import android.content.Context;

/**
 * Created by Nikhil on 18-08-2018.
 */

public class ContextualObject {

    private Context mContext;

    protected ContextualObject(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
