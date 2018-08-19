package com.sample.nikhil.foregroundservice;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Nikhil on 15-08-2018.
 */

public class MediaFile extends File {
    private MediaMetadataRetriever mediaMetadataRetriever;
    private boolean isFavourite;

    {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this.getPath());
        this.mediaMetadataRetriever = mmr;
    }

    public MediaFile(@NonNull String pathname) {
        super(pathname);
    }

    public MediaFile(File file){
        super(file.getPath());

    }

    public MediaMetadataRetriever getMediaMetadataRetriever() {
        return mediaMetadataRetriever;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaFile){
            return this.getAbsolutePath().equals(((MediaFile)obj).getAbsolutePath());
        }else {
            throw new RuntimeException("In Compatible type.");
        }

    }
}
