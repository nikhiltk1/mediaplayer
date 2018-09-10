package com.sample.nikhil.foregroundservice.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.adapters.FolderAdapter;
import com.sample.nikhil.foregroundservice.data.DataManager;

public class FolderListFragment extends Fragment {
    RecyclerView mRecyclerViewFolders;
    private FolderAdapter mFolderAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_folder_list, container, false);
        mRecyclerViewFolders = view.findViewById(R.id.recycler_folder);
        mFolderAdapter = new FolderAdapter(getContext(), DataManager.getInstance().getFolderList());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerViewFolders.setLayoutManager(mLayoutManager);
        mRecyclerViewFolders.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewFolders.setAdapter(mFolderAdapter);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager.getInstance().getFolders();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFolderAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
        return view;
    }
}