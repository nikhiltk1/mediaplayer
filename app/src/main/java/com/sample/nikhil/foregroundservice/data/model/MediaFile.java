package com.sample.nikhil.foregroundservice.data.model;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Nikhil on 15-08-2018.
 */

public class MediaFile extends File implements Serializable {
    private MediaMetadataRetriever mediaMetadataRetriever;
    private boolean isFavourite;

    {
        try {
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this.getPath());
            this.mediaMetadataRetriever = mmr;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MediaFile(@NonNull String pathname) {
        super(pathname);
    }

    public MediaFile(File file) {
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
        if (obj instanceof MediaFile) {
            return this.getAbsolutePath().equals(((MediaFile) obj).getAbsolutePath());
        } else {
            throw new RuntimeException("In Compatible type.");
        }

    }
}
