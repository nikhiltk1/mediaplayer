package com.sample.nikhil.foregroundservice.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.adapters.FilesAdapter;
import com.sample.nikhil.foregroundservice.data.DataManager;
import com.sample.nikhil.foregroundservice.data.model.MediaFile;

import java.io.File;
import java.util.ArrayList;

import static com.sample.nikhil.foregroundservice.utils.Constants.INTENT_STRING_FOLDER;

public class FolderActivity extends AppCompatActivity {
    private File mFolder;
    private RecyclerView mRecyclerFiles;
    FilesAdapter mFilesAdapter;
    private ArrayList<MediaFile> mMediaFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFolder = (File) getIntent().getSerializableExtra(INTENT_STRING_FOLDER);
        mRecyclerFiles = findViewById(R.id.recycler_files);
        mFilesAdapter = new FilesAdapter(FolderActivity.this, new ArrayList<MediaFile>());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerFiles.setLayoutManager(mLayoutManager);
        mRecyclerFiles.setItemAnimator(new DefaultItemAnimator());
        mRecyclerFiles.setAdapter(mFilesAdapter);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFilesAdapter.setFiles(DataManager.getInstance().retrieveMediaFromFolder(mFolder));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mFilesAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }).start();

    }
}
