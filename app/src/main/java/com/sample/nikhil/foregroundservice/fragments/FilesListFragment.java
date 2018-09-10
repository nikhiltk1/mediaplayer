package com.sample.nikhil.foregroundservice.fragments;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.adapters.FilesAdapter;
import com.sample.nikhil.foregroundservice.data.DataManager;

import java.io.File;


public class FilesListFragment extends Fragment {
    ProgressBar progressbar;
    RelativeLayout rl;
    RecyclerView recyclerFiles;
    DataManager mDataManager;
    FilesAdapter filesAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_files_list, container, false);
        mDataManager = DataManager.getInstance();
        try {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressbar = rootView.findViewById(R.id.progressbar);
        rl = rootView.findViewById(R.id.rl);
        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        filesAdapter = new FilesAdapter(getContext(), DataManager.getInstance().getFiles());

        recyclerFiles = rootView.findViewById(R.id.recycler_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerFiles.setLayoutManager(mLayoutManager);
        recyclerFiles.setItemAnimator(new DefaultItemAnimator());
        recyclerFiles.setAdapter(filesAdapter);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDataManager.getPlayList(DataManager.EXTERNAL_CONTENT_URI);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        filesAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
        /*mDataManager.getPlayList(file, new DataManager.Mp3FileListener() {
            @Override
            public void onFilesRetrieved(List<MediaFile> mp3Files) {

                filesAdapter.setMp3Files(mp3Files);
                filesAdapter.notifyDataSetChanged();

            }
        });*/


        return rootView;
    }


}
